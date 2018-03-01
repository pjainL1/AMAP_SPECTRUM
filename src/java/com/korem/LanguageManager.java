package com.korem;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;


public class LanguageManager extends AbstractLanguageManager {
    private static final String K_LANGUAGE = "language";
    
    private HttpServletRequest request;
    private HttpSession session;

    public LanguageManager(ServletContext servletContext, HttpServletRequest req) {
        this(servletContext, req, null);
    }

    public LanguageManager(ServletContext servletContext, HttpServletRequest req, String subPath) {
        request = req;
        session = req.getSession();
        loadLanguages(getLocaleFolder(servletContext, subPath));
        setLanguage();
    }
    
    private Path getLocaleFolder(ServletContext servletContext, String subPath) {
        return Paths.get(servletContext.getRealPath(
                (subPath == null)
                ? getPathLanguages()
                : (String.format("%s/%s", subPath, getPathLanguages()))));
    }
    
    protected final void setLanguage() {
        String language = request.getParameter(K_LANGUAGE);
        if (StringUtils.isNotEmpty(language)) {
            setLanguage(language);
        } else {
            language = getLanguageName();
            if (StringUtils.isEmpty(language)) {
                String requestLanguage = request.getLocale().getLanguage();
                if (getLocalLanguages().containsKey(requestLanguage)) {
                    setLanguage(requestLanguage);
                } else {
                    setLanguage(getDefaultLanguage());
                }
            }
        }
    }
    
    @Override
    protected void setLanguage(String language) {
        session.setAttribute(K_LANGUAGE, language);
    }
    
    @Override
    public String getLanguageName() {
        return (String) session.getAttribute(K_LANGUAGE);
    }
    
}
