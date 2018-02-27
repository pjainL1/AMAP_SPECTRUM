package com.korem;

import com.lo.config.Confs;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jduchesne, jphoude
 */
public abstract class AbstractLanguageManager {

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.LogManager.getLogger(AbstractLanguageManager.class);
    
    private static final String K_FILENAME = "filename";
    private static final String K_ISDEFAULT = "isDefault";
    private static final String K_NAME = "language";
    private static final String C_JSON_START = " = {";
    private static final String PATH_LANGUAGES = "js/locales/";
    private static final String LANGUAGE_EXLUDE = "locale.js";
    
    private static final Object MUTEX = new Object();
    private static Map<String, JSONObject> languages;
    private static String defaultLanguage;
    private static Map<String, JSONObject> workingLanguages;
    private String workingDefaultLanguage;
    private String languageNames;
    private Map<String, JSONObject> localLanguages;
    
   
    protected abstract void setLanguage(String language);
    public abstract String getLanguageName();

    private JSONObject getLanguage(String language) {
        return (JSONObject) localLanguages.get(language);
    }

    protected void loadLanguages(Path localeFolder) {
        if (languages == null) {
            synchronized (MUTEX) {
                if (languages == null) {
                    loadLanguagesWithPath(localeFolder);
                    watchFolder(localeFolder);
                }
            }
        }
        localLanguages = languages;
    }

    private void loadLanguage(Path languageFile) {
        String fileContent;
        try {
            fileContent = StringUtils.join(Files.readAllLines(languageFile, Charset.forName(Confs.STATIC_CONFIG.charset())), "");
        } catch (IOException e) {
            LOGGER.warn(String.format("Error loading language file %s, skipping", languageFile.toAbsolutePath().toString()));
            return;
        }
        int index = fileContent.indexOf(C_JSON_START) + C_JSON_START.length() - 1;
        JSONObject language = JSONObject.fromObject(
                fileContent.substring(index));

        String languageName = language.getString(K_NAME);

        language.put(K_FILENAME, languageFile.getFileName().toString());
        workingLanguages.put(languageName, language);

        if (workingDefaultLanguage == null || language.containsKey(K_ISDEFAULT) && language.getBoolean(K_ISDEFAULT)) {
            workingDefaultLanguage = languageName;
        }
    }

    public String getLanguageNames() {
        return languageNames;
    }

    private void setLanguageNames() {
        JSONBuilder builder = new JSONStringer().array();
        for (String languageName : workingLanguages.keySet()) {
            builder.array().value(languageName).endArray();
        }
        languageNames = builder.endArray().toString();
    }

    public String get(String... keys) {
        try {
            Object entries = getLanguage(getLanguageName());
            for (String key : keys) {
                if (((JSONObject) entries).containsKey(key)) {
                    entries = ((JSONObject) entries).get(key);
                }
            }
            return (String) entries;
        } catch (Exception e) {
            return "???";
        }
    }

    public String get(String key) {
        return get(key.split("[.]"));
    }

    private void loadLanguagesWithPath(Path localeFolder) {
        workingLanguages = new HashMap<>();
        workingDefaultLanguage = null;
        try {
            Files.walkFileTree(localeFolder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.getFileName().toString().equals(LANGUAGE_EXLUDE)) {
                        loadLanguage(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            setLanguageNames();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        languages = workingLanguages;
        defaultLanguage = workingDefaultLanguage;
    }

    private void watchFolder(final Path languageFolder) {
        try {
            final WatchService watchService = FileSystems.getDefault().newWatchService();
            languageFolder.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    do {
                        try {
                            WatchKey key = watchService.take();
                            synchronized (MUTEX) {
                                loadLanguages(languageFolder);
                            }
                            for (WatchEvent event : key.pollEvents()) {
                                LOGGER.info(event.context().toString());
                                LOGGER.info(event.kind().toString());
                            }
                            key.reset();
                        } catch (InterruptedException ex) {
                            LOGGER.error(ex.getMessage(), ex);
                        }
                    } while (true);
                }
            }).start();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public static String getDefaultLanguage() {
        return defaultLanguage;
    }

    public Map<String, JSONObject> getLocalLanguages() {
        return localLanguages;
    }
    
    protected String getPathLanguages() {
        return PATH_LANGUAGES;
    }
}
