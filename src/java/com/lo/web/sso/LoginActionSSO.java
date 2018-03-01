package com.lo.web.sso;

import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.lo.ContextParams;
import com.lo.config.Confs;
import com.lo.db.om.User;
import com.lo.hosting.watchdog.InboxWatchdog;
import com.lo.ldap.AMAPAuthorization;
import com.lo.ldap.Authorization;
import com.spinn3r.log5j.Logger;

/**
 *
 * @author ydumais
 */
public class LoginActionSSO extends Action {
    
    private static class ReturnInfo {
        private String forward;
        private String error;
        
        private ReturnInfo(String forward, String error) {
            this.forward = forward;
            this.error = error;
        }

        public String getForward() {
            return forward;
        }

        public String getError() {
            return error;
        }
    }

    private static final Logger log = Logger.getLogger();
    private static final String DEFAULT = "default";
    private static final String ERROR = "error";
    private static final String ADMIN = "admin";
    private static final String LOAD = "loadingProcess";
    private static final String INIT = "init";
    private static final String ERROR_INVALID_ACCESS = "6000";
    private static final String ERROR_LDAP_ACCESS = "6001";
    public static final String SESSION_TIMEOUT = "maxInactiveInterval";
    private static ResourceBundle lang = ResourceBundle
            .getBundle("loLocalString");

    private static final String DEFAULT_LANG = "amap.default.lang";
    private static final String AVAILABLE_LANG = "amap.available.lang";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        log.debug("login");

        cleanup(request.getSession());

        // if (request.getParameter("sponsors")!=null &&
        // request.getParameter("sponsors"))
        // error message while loading process
        if (InboxWatchdog.isUsed()) {
            return mapping.findForward(LOAD);
        }

        String errorType = "";
        String forward = DEFAULT;

        // there, we check whether the user is already logged in or not
        HttpServletRequest hreq = (HttpServletRequest) request;
        HttpSession session = hreq.getSession();
        ContextParams cp = ContextParams.get(session);

        /**
         * to push session timeout interval to client side
         */
        session.setAttribute(SESSION_TIMEOUT, session.getMaxInactiveInterval());

        String consumerKey = request.getParameter("consumerKey");
        String token = request.getParameter("token");
        String uid = request.getParameter("uid");
        String domain = request.getParameter("domain");
        String languagePref = request.getParameter("langPref");

        if (languagePref != null && languagePref.contains("-")) {
            languagePref = languagePref.substring(0, languagePref.indexOf('-'));
        }

        String defaultLang = Config.getInstance().getValue(DEFAULT_LANG);
        String availableLang = Config.getInstance().getValue(AVAILABLE_LANG);

        if (languagePref != null) {
            request.setAttribute(
                    "language",
                    (!(languagePref.toLowerCase().matches(".*(" + availableLang
                            + ").*")) ? defaultLang : languagePref));
        } else {
            languagePref = defaultLang;
        }

        User user = null;
        
        if (Confs.CONFIG.koremInternalAccessEnabled()) {
            ReturnInfo authInfo = authorize(request, cp, session);
            forward = authInfo.getForward();
            if (authInfo.getError() != null) {
                errorType = authInfo.getError();
            }
        } else if (consumerKey != null && token != null) {
            if (cp != null) {
                User u = cp.getUser();
                if (u != null) {
                    // user already logged
                    return mapping.findForward(forward);
                }
            }

            SsoAuthentication sso = new SsoAuthentication(consumerKey, token,
                    uid, domain);
            if (sso.getWebService().isValid()) {
                log.debug("token valid");
                ReturnInfo authInfo = authorize(request, cp, session);
                forward = authInfo.getForward();
                if (authInfo.getError() != null) {
                    errorType = authInfo.getError();
                }
            } else {
                log.debug("token error " + sso.getWebService().getCode());

                errorType = sso.getWebService().getCode();

                request.setAttribute("errortype", errorType);

                if (sso.getWebService().getMessage() != null) {

                    String ssoError = "";
                    if (errorType.equals("1000")) {
                        ssoError = lang.getString("secure.ldap.token.expired");
                    } else if (errorType.equals("1001")) {
                        ssoError = lang.getString("secure.ldap.token.notfound");
                    } else if (errorType.equals("2000")) {
                        ssoError = lang
                                .getString("secure.ldap.token.consumernotfound");
                    } else {
                        ssoError = sso.getWebService().getMessage();
                    }

                    request.setAttribute("errormessage", ssoError);
                }
                forward = ERROR;
            }
        } else {
            // consumerKey and token not found , invalid access to AMAP
            errorType = ERROR_INVALID_ACCESS;
            forward = ERROR;
        }

        request.setAttribute("errortype", errorType);
        return mapping.findForward(forward);
    }

    private void cleanup(HttpSession session) {
        session.removeAttribute(ContextParams.SESSION_ATTRIBUTE_NAME);
        session.invalidate();
    }
    
    private ReturnInfo authorize(HttpServletRequest request, ContextParams cp, HttpSession session) {
        // process to AUTHORIZATION
        // return the User authenticated and authorized
        Authorization auth = new AMAPAuthorization();
        User user = auth.grantAuthorization(request.getParameterMap());

        if (user != null) {
            cp.setUser(user);
            cp.set(session);

            if (user.isAdmin()) {
                return new ReturnInfo(ADMIN, null);
            }
        } else {
            // unauthorize
            auth.getErrorMessageKey();
            request.setAttribute("errorMsgKey",
                    auth.getErrorMessageKey());
            return new ReturnInfo(ERROR, ERROR_LDAP_ACCESS);
        }
        
        return new ReturnInfo(DEFAULT, null);
    }
}
