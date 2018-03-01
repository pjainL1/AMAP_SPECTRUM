package com.lo.web;

import com.lo.config.Confs;
import com.lo.hosting.watchdog.InboxWatchdog;
import com.lo.hosting.watchdog.Watchdog;
import java.io.BufferedOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Appender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.lo.Pwd;
import com.lo.hosting.watchdog.InboxWatchdog;
import com.lo.hosting.watchdog.Watchdog;
import com.loyalty.app.marketing.security.LoyaltyEncryptor;

/**
 *
 * @author ydumais
 */
public class ForceLoadAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (!InboxWatchdog.isUsed()) {
            Logger log = Logger.getLogger("com.lo.hosting");
            BufferedOutputStream os = new BufferedOutputStream(
                    response.getOutputStream());
            Appender appender = new WriterAppender(new HTMLLayout(), os);
            log.addAppender(appender);
            try {
                String password = request.getParameter("password");
                boolean validPassword = false;

                //config password is encrypted with AES. attempting to encrypt the parameter to compare the encrypted values
                String encryptedPasswordWithBrackets = Pwd.getInstance().getValue("amap.forceLoadPassword");
                if (!Confs.CONFIG.koremInternalAccessEnabled()) {
                    String encryptedPassword = encryptedPasswordWithBrackets.substring(encryptedPasswordWithBrackets.indexOf("DECRYPT{") + 8, encryptedPasswordWithBrackets.indexOf("}"));

                    LoyaltyEncryptor lencryptor = new LoyaltyEncryptor(LoyaltyEncryptor.EncryptionAlgorithms.AES);
                    validPassword = lencryptor.encryptString(password).equals(encryptedPassword);
                } else {
                    validPassword = password.equals(encryptedPasswordWithBrackets);
                }

                if (!validPassword) {
                    log.fatal("You do not know the secret phrase, refusing to load data.");
                } else {
                    log.info("Force loading data requested.");
                    Watchdog watchdog = new InboxWatchdog();
                    watchdog.run();
                }
				// }catch(Exception e){
                // log.error(e);
            } finally {
                log.removeAppender(appender);
            }
        } else {
            Logger log = Logger.getLogger("com.lo.hosting");
            log.info("Already loading");
        }
        return null;
    }
}
