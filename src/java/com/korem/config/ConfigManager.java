package com.korem.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

/**
 *
 * @author jduchesne
 */
public class ConfigManager {

    public interface ConfigurationListener {

        public void changed(Map<String, String> values);
    }
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    private static final String CONFIG_PREFIX = "config.";
    private static final String CONFIG_INTERVAL = "/configMonitoringInterval";
    private static final String CONFIG_CONTEXT_NAME = "java:comp/env";
    private static final long MONITORING_INTERVAL_DEFAULT = 5000;
    private static final String LOG_MONITORING_INTERVAL = "Monitoring interval: %s milliseconds.";
    private static final String LOG_KEYVALUE_FORFILE = "Key: %s, Value: %s";
    private static final String LOG_LOADINGFILE = "Loading config file: %s";
    private final Map<String, ConfigFile> configFiles;
    private long monitoringInterval;
    private static ConfigManager instance;
    
    private static final boolean VALIDATION_GEN_ENABLED = false;
    private static final String VALIDATION_XML_TEMPLATE = "        <rule>\n" +
"            <name>%s</name>\n" +
"            <isMandatory>true</isMandatory>\n" +
"            <errorLevel>info</errorLevel>\n" +
"            <type>%s</type>\n" +
"        </rule>";
    
    private static final Map<String, String> VALIDATION_TYPES = new HashMap<String, String>();
    static {
        VALIDATION_TYPES.put("Set<String>", "STRING");
        VALIDATION_TYPES.put("String", "STRING");
        VALIDATION_TYPES.put("Double", "DOUBLE");
        VALIDATION_TYPES.put("Integer", "INTEGER");
        VALIDATION_TYPES.put("String[]", "STRING");
        VALIDATION_TYPES.put("Map<String, String>", "STRING");
    }

    public synchronized static ConfigManager get() {
        if (instance == null) {
            instance = new ConfigManager();
            instance.initAll();
        }
        return instance;
    }

    private ConfigManager() {
        configFiles = new HashMap<String, ConfigFile>();
    }

    private void initAll() {
        loadConfigFiles();
        startMonitoring();
    }

    private void logInfo(String format, Object... args) {
        LOGGER.info(String.format(format, args));
    }

    private void logError(String format, Object... args) {
        LOGGER.severe(String.format(format, args));
    }

    private void logDebug(String format, Object... args) {
        LOGGER.log(Level.FINER, String.format(format, args));
    }

    private void setMonitoringInterval(Context context) {
        try {
            monitoringInterval = (Long) context.lookup(CONFIG_CONTEXT_NAME + CONFIG_INTERVAL);
        } catch (Exception e) {
            monitoringInterval = MONITORING_INTERVAL_DEFAULT;
        }
        logInfo(LOG_MONITORING_INTERVAL, monitoringInterval);
    }

    private void loadConfigFiles() {
        try {
            Context initCtx = new InitialContext();
            setMonitoringInterval(initCtx);
            NamingEnumeration<Binding> bindings = initCtx.listBindings(CONFIG_CONTEXT_NAME);
            while (bindings.hasMore()) {
                Binding binding = bindings.next();
                if (binding.getObject() instanceof String && binding.getName().startsWith(CONFIG_PREFIX)) {
                    loadConfigFile(binding.getName().substring(CONFIG_PREFIX.length()), (String) binding.getObject());
                }
            }
        } catch (Exception e) {
            logError(e.getMessage(), e);
        }
    }

    private ConfigFile loadConfigFile(ConfigFile configFile) throws IOException {
        FileReader reader = new FileReader(configFile.file);
        Properties properties = new Properties();
        try {
            properties.load(reader);
            configFile.originalProperties = (Map<String, String>) properties.clone();
        } catch (Exception e) {
            logError(e.getMessage(), e);
        } finally {
            reader.close();
        }
        return normalizePropsKeys(configFile, properties);

    }

    private ConfigFile loadProperties(String path) throws IOException {
        Properties properties = new Properties();
        File file = new File(path);
        ConfigFile configFile = new ConfigFile(file, properties);
        return loadConfigFile(configFile);
    }
    
    public Map<String, String> getProperties(String name) {
        ConfigFile configFile = configFiles.get(name);
        if (configFile == null) {
            return null;
        }
        return configFile.originalProperties;
    }

    private void loadConfigFile(String name, String path) throws IOException {
        configFiles.put(name, loadProperties(path));
    }

    public <T> T getConfig(File file, Class<T> spec) throws IOException {
        return tryWrap(loadProperties(file.getAbsolutePath()), spec);
    }
    
    private <T> T tryWrap(ConfigFile configFile, Class<T> spec) {
        if (configFile.proxy == null) {
            configFile.spec = spec;
            wrap(configFile, spec);
        }
        return (T) configFile.proxy;
    }
    
    public <T> T getConfig(String name, Class<T> spec) {
        ConfigFile configFile = configFiles.get(name);
        if (configFile != null) {
            synchronized (configFile) {
                return tryWrap(configFile, spec);
            }
        }
        return null;
    }
    /**
     * 
     * @param name
     * @return 
     */
    public File getConfig(String name) {
        return configFiles.get(name).file;
    }

    public void listenForChanges(String name, String filePath, ConfigurationListener l) {
        ConfigFile configFile = configFiles.get(name);
        if (configFile != null) {
            configFile.addListener(l);
        } else if (filePath != null) {
            try {
                loadConfigFile(name, filePath);
                listenForChanges(name, null, l);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Configuration file not found.", ex);
            }
        } else {
            LOGGER.log(Level.SEVERE, "Configuration file path is missing.");
        }
    }

    private <T> void normalizeValues(Properties properties, Class<T> spec) {
        for (Method method : spec.getMethods()) {
            if (method.getDeclaringClass() != Object.class) {
                String methodName = method.getName();
                Object value = normalizeValue(properties.getProperty(methodName), method.getReturnType());
                if (value != null) {
                    properties.put(methodName, value);
                }
            }
        }
    }

    private <T> void wrap(final ConfigFile configFile, Class<T> spec) {
        normalizeValues(configFile.properties, spec);
        configFile.proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{spec},
                new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                if (method.getDeclaringClass() != Object.class) {
                    synchronized (configFile) {
                        return configFile.properties.get(method.getName());
                    }
                } else {
                    return method.invoke(proxy, args);
                }
            }
        });
    }

    private Object normalizeValue(String value, Class<?> returnType) {
        try {
            if (returnType == Double.class || returnType == double.class) {
                if (value != null && ((String) value).length() > 0) {
                    return new Double((String) value);
                }
            } else if (returnType == Integer.class || returnType == int.class) {
                if (value != null && ((String) value).length() > 0) {
                    return new Integer((String) value);
                }
            } else if (returnType == Set.class) {
                return createSet((String) value);
            } else if (returnType == Map.class) {
                return createMap((String) value);
            } else if (returnType.isArray()) {
                return createArray((String) value);
            } else if (returnType == Boolean.class) {
                return Boolean.parseBoolean(value);
            }
        } catch (Exception e) {
            logError(e.getMessage(), e);
        }
        return value;
    }

    private String[] createArray(String allValues) {
        if (allValues != null && ((String) allValues).length() > 0) {
            return allValues.split(",");
        }
        return new String[0];
    }

    private Map<String, String> createMap(String allValues) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        if (allValues != null && ((String) allValues).length() > 0) {
            String[] entries = allValues.split("[|]");
            for (String entry : entries) {
                String[] keyValuePair = entry.split("[:]", 2);
                map.put(keyValuePair[0], keyValuePair[1]);
            }
        }
        return map;
    }

    private Set createSet(String allValues) {
        Set<String> set = new HashSet<String>();
        if (allValues != null && ((String) allValues).length() > 0) {
            String[] values = createArray(allValues);
            Collections.addAll(set, values);
        }
        return set;
    }

    private ConfigFile normalizePropsKeys(ConfigFile configFile, Properties properties) {
        Iterator<Map.Entry<Object, Object>> ite = properties.entrySet().iterator();
        Collection<Map.Entry<String, Object>> changedEntries = new LinkedList<Map.Entry<String, Object>>();
        logDebug(LOG_LOADINGFILE, configFile.file);
        while (ite.hasNext()) {
            Map.Entry<Object, Object> entry = ite.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            if (keyNeedNormalize(key)) {
                changedEntries.add(new SimpleEntry<String, Object>(key = normalizeKey(key), value));
                ite.remove();
            }
            logDebug(LOG_KEYVALUE_FORFILE, key, value);
        }
        for (Map.Entry<String, Object> entry : changedEntries) {
            properties.put(entry.getKey(), entry.getValue());
        }
        configFile.properties = properties;
        return configFile;
    }

    private boolean keyNeedNormalize(String key) {
        return key.contains(".");
    }

    private String normalizeKey(String key) {
        String[] keyParts = key.split("[.]");
        StringBuilder normalizedKey = new StringBuilder(keyParts[0]);
        for (int i = 1; i < keyParts.length; ++i) {
            String keyPart = keyParts[i];
            normalizedKey.append(Character.toUpperCase(keyPart.charAt(0)));
            if (keyPart.length() > 1) {
                normalizedKey.append(keyPart.substring(1));
            }
        }
        return normalizedKey.toString();
    }

    private void updateConfigFiles() throws IOException {
        for (ConfigFile configFile : configFiles.values()) {
            updateConfigFile(configFile);
        }
    }

    private void updateConfigFile(ConfigFile configFile) throws IOException {
        synchronized (configFile) {
            if (configFile.hasChanged()) {
                loadConfigFile(configFile);
                configFile.notifyUpdate();
                if (configFile.proxy != null) {
                    normalizeValues(configFile.properties, configFile.spec);
                }
                configFile.loaded();
            }
        }
    }

    private void startMonitoring() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    sleep();
                    try {
                        updateConfigFiles();
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    private void sleep() {
        try {
            Thread.sleep(monitoringInterval);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    private static class ConfigFile {

        private File file;
        private Properties properties;
        private Map<String, String> originalProperties;
        private Object proxy;
        private Class<?> spec;
        private long previousModifiedTime;
        private List<ConfigurationListener> listeners;

        private ConfigFile(File file, Properties properties) {
            this.file = file;
            this.properties = properties;
            this.listeners = new ArrayList<ConfigurationListener>();
            previousModifiedTime = file.lastModified();
        }

        private boolean hasChanged() {
            return file.lastModified() != previousModifiedTime;
        }

        private void loaded() {
            previousModifiedTime = file.lastModified();
        }

        private void addListener(ConfigurationListener l) {
            this.listeners.add(l);
            if (this.originalProperties != null) {
                l.changed(this.originalProperties);
            }
        }

        private void notifyUpdate() {
            for (ConfigurationListener l : this.listeners) {
                l.changed(this.originalProperties);
            }
        }
    }

    static class GenerateConfigJavaInterface {

        private ConfigManager configManager;
        private static final String JAVA_FILE_EXTENSION = ".java";
        private static final String PACKAGE_TEMPLATE = "package {package};\n\n"
                + "import java.util.Set;\n\n"
                + "import java.util.Map;\n\n";
        private static final String INTERFACE_JAVA_DOC_TEMPLATE =
                "/**\n"
                + " * {configFileName} interface.\n"
                + " * @author Auto generator\n"
                + " */\n";
        private static final String METHOD_JAVA_DOC_TEMPLATE =
                "    /**\n"
                + "     * Return the property {propertyName} of the {configFileName} file\n"
                + "     * @return {dataType}\n"
                + "     */\n";
        private static final String INTERFACE_CODE_TEMPLATE =
                "public interface {configFileName} {\n\n"
                + "{methods}"
                + "}";
        private static final String METHOD_CODE_TEMPLATE = "    {dataType} {methodName}();\n\n";

        public GenerateConfigJavaInterface(ConfigManager configManager) {
            this.configManager = configManager;
        }

        public void main(String[] args) throws Exception {
            if (args == null || args.length <= 2) {
                throw new IllegalArgumentException("Wrong arguments list: packageName configFilesPathList.");
            }
            String packageName = PACKAGE_TEMPLATE.replace("{package}", args[0]);
            String folderBase = args[1] + File.separator + args[0].replace(".", File.separator);
            File interfacesFolder = new File(folderBase);
            for (int i = 2; i < args.length; i++) {
                String filePath = args[i];
                Properties properties = this.getProperties(filePath);
                if (properties.keySet().isEmpty()) {
                    throw new MissingResourceException("Empty Properties file.", filePath, "All");
                }

                StringBuilder sb = new StringBuilder(packageName);
                String interfaceName = this.getInterfaceName(filePath);
                sb.append(INTERFACE_JAVA_DOC_TEMPLATE.replace("{configFileName}", interfaceName));
                String interfaceText = INTERFACE_CODE_TEMPLATE.replace("{configFileName}", interfaceName);
                StringBuilder methods = new StringBuilder();
                    
                if (VALIDATION_GEN_ENABLED) {
                    System.out.println(String.format("Validation rules for %s will follow...", filePath));
                }
                
                for (Object keyObj : properties.keySet()) {
                    String key = (String) keyObj;
                    if (this.generateInterfaceMethod(key)) {
                        methods.append(this.getMethodName(key, properties.getProperty(key), filePath));
                        if (VALIDATION_GEN_ENABLED) {
                            writeValidationXml(key, properties.getProperty(key), filePath);
                        }
                    }
                }
                
                if (VALIDATION_GEN_ENABLED) {
                    System.out.println("---------------------------------------------------------");
                }
                
                interfaceText = interfaceText.replace("{methods}", methods.toString());
                sb.append(interfaceText);
                if (!interfacesFolder.exists()) {
                    interfacesFolder.mkdirs();
                }
                String interfaceFilePath = folderBase + File.separator + interfaceName + JAVA_FILE_EXTENSION;
                File interfaceFile = new File(interfaceFilePath);
                FileWriter fw = new FileWriter(interfaceFile);
                try {
                    fw.write(sb.toString());
                } finally {
                    fw.close();
                }
            }
        }

        private String getInterfaceName(String filePath) {
            if (filePath.lastIndexOf(".") <= 1) {
                throw new IllegalArgumentException("Wrong path: " + filePath);
            }
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.lastIndexOf("."));
            return capitalize(fileName);
        }

        private String capitalize(String original) {
            if (original == null || original.length() == 0) {
                return original;
            }
            return Character.toUpperCase(original.charAt(0)) + original.substring(1);
        }

        private boolean generateInterfaceMethod(String key) {
            return !key.contains("_");
        }

        private String getMethodName(String key, String value, String filePath) {
            String methodName = this.configManager.normalizeKey(key);
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
            String javaDoc = METHOD_JAVA_DOC_TEMPLATE.replace("{propertyName}", key);
            javaDoc = javaDoc.replace("{configFileName}", fileName);
            String methodSpec = METHOD_CODE_TEMPLATE.replace("{methodName}", methodName);
            methodSpec = this.replaceDataType(key, methodSpec, value);
            javaDoc = this.replaceDataType(key, javaDoc, value);
            return javaDoc + methodSpec;
        }

        private String findDataType(String configKey, String value) {
            if (configKey.endsWith(".set")) {
                return "Set<String>";
            }
            if (configKey.endsWith(".array")) {
                return "String[]";
            }
            if (configKey.endsWith(".map")) {
                return "Map<String, String>";
            }
            if (value.equalsIgnoreCase(Boolean.TRUE.toString()) ||
                    value.equalsIgnoreCase(Boolean.FALSE.toString())) {
                return "Boolean";
            }
            try {
                Integer.parseInt(value);
                return "Integer";
            } catch (NumberFormatException e1) {
                try {
                    Double.parseDouble(value);
                    return "Double";
                } catch (NumberFormatException e2) {
                    return "String";
                }
            }
        }
        
        private String replaceDataType(String configKey, String methodSpec, String value) {
            String t = findDataType(configKey, value);
            
            return methodSpec.replace("{dataType}", t);
        }

        private Properties getProperties(String filePath) throws IOException {
            File file = new File(filePath);
            Properties properties;
            FileReader fr = new FileReader(file);
            try {
                properties = new Properties();
                properties.load(fr);
            } finally {
                fr.close();
            }
            return properties;
        }

        private void writeValidationXml(String key, String value, String filePath) {
            String dataType = findDataType(key, value);
            String validationType = VALIDATION_TYPES.get(dataType);
            if (validationType != null) {
                String rule = String.format(VALIDATION_XML_TEMPLATE, key, validationType);
                System.out.println(rule);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ConfigManager configManager = new ConfigManager();
        GenerateConfigJavaInterface generator = new ConfigManager.GenerateConfigJavaInterface(configManager);
        generator.main(args);
    }
}
