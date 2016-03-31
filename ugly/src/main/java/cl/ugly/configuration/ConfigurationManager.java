package cl.ugly.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {
    
    private static final String PROPERTIES_FILE = "sem.dedup.properties";
    
    private static Properties props;
    private static ConfigurationManager instance;
    
    public static synchronized ConfigurationManager getInstance() {
        if(instance == null) instance = new ConfigurationManager();
        return instance;
    }
    
    public Integer getInt(String key) {
        Object value = props.get(key);
        if(value != null) {
            return new Integer(value.toString());
        }
        return null;
    }

    public Integer getInt(String key, int defaultValue) {
        Integer value = getInt(key);
        return (value != null) ? value : defaultValue;
    }
    
    public String getString(String key) {
        return (String)props.get(key);
    }

    public String[] getStringArray(String key) {
        String arrayStr = getString(key);
        if(arrayStr != null) {
            String[] values = arrayStr.split(",");
            for(int i = 0; i < values.length; i++) {
                values[i] = values[i].trim();
            }
            return values;
        }
        return null;
    }
    
    private ConfigurationManager() {
        props = loadProperties(PROPERTIES_FILE);
    }
    
    private Properties loadProperties(String fileName) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName)) {
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }    

}
