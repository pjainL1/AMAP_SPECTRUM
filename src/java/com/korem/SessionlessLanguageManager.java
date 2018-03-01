
package com.korem;

import java.io.File;
import java.nio.file.Path;


public class SessionlessLanguageManager extends AbstractLanguageManager {
    private static final String PATH_TO_ROOT = "../../../../"; // path to the "web" folder from the class directory.
    
    private String currentLanguage;
    
    public SessionlessLanguageManager(String language) {
        this(language, null);
    }
    
    public SessionlessLanguageManager(String language, String subPath) {
        loadLanguages(getLocaleFolder(subPath));
        setLanguage(language);
    }

    @Override
    protected final void setLanguage(String language) {
        currentLanguage = language;
    }

    @Override
    public String getLanguageName() {
        return currentLanguage;
    }
    
    private Path getLocaleFolder(String subPath) {
        String classFileName = String.format("%s.class", AbstractLanguageManager.class.getSimpleName());
        File classFile = new File(getClass().getResource(classFileName).getFile());
        File baseDir = new File(classFile.getParentFile(), PATH_TO_ROOT);
        if (subPath != null) {
            baseDir = new File(baseDir, subPath);
        }
        File localeDir = new File(baseDir, getPathLanguages());
        
        return localeDir.toPath();
    }
}
