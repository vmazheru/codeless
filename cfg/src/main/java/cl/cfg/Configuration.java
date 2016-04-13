package cl.cfg;

/**
 * <p>The {@code Configuration} object is responsible for loading properties from
 * files located either by their absolute path or in the classpath.
 * 
 * <p>The configuration object is a singleton loaded for the first time whenever the
 * {@code Configuration.getConfiguration} method is called.
 * 
 * <p>The procedure of loading properties goes as follows:
 * <ol>
 * <li>
 *   The file named 'cl.cfg.txt' is searched in the classpath.  If found,
 *   teach line of it is treated as a property file name. For each line,
 *   the loader will try to find a file in the classpath, and load properties
 *   from it.  If no such file found, the {@code ConfigurationException} is thrown.<br/>
 *   If file 'cl.cfg.txt' is not found itself, nothing gets loaded and no exception is thrown.<br/>
 *   The 'cl.cfg.txt' file can be used whenever the application is required to load its attributes
 *   from multiple property files. The lines placed in this file may be just simple file names
 *   (in which case the files are loaded from the classpath) or absolute paths.
 * </li>
 * <li>
 *   Next, a file named 'cl.cfg.properties' is searched in the classpath.  If found, the properties
 *   from this file are loaded. If not, no exception is thrown.  This file should be in Java property
 *   file format.
 * </li>
 * <li>
 *   Next, the previous step gets repeated for a file named 'cl.cfg.xml'. The difference is, that
 *   this file should be in Java XML property file format.
 * </li>
 * <li>
 *   If the configuration object is loaded by calling {@code Configuration.getConfiguration(String fileName)}
 *   method, as the last step, the properties get loaded from the given file.  The file name may be a
 *   simple file name, in which case the file is loaded from the classpath, or it may be an absolute path.<br/>
 *   A {@code ConfigurationException} is thrown if the file can't be found.
 * </li>
 * </ol>
 * 
 * <p>Note, that because the properties might be loaded from multiple files as part of the automatic
 * loading procedure or on demand, the conflicting properties will be overwritten, that is the
 * last loaded value wins. This feature can be used to change property values at application run time.
 * 
 * <p>Configuration exception is thrown whenever the file name is explicitly specified either in the
 * listing ('cl.cfg.txt') file or as a parameter to {@code Configuration.getConfiguration} method.
 * On the other hand, if any of the "predefined" files are not found in the classpath, nothing happens.
 *
 *<p>Once the configuration is loaded, the values may be accessed by their respective keys by calling
 * numerous getXXX methods, which come in two flavors. Methods which do not supply default values 
 * throw {@code ConfigurationException} whenever the keys are not found.  On the other hand, the methods
 * which do supply default values will just return these default values.
 */
public interface Configuration {
    
    /**
     * Return a property value as a String.
     * @throws ConfigurationException if the property cannot be found.
     */
    String getString(String key);
    
    /**
     * Return a property value as an integer.
     * @throws ConfigurationException if the property cannot be found or the value cannot be parsed to an integer.
     */
    int getInt(String key);
    
    /**
     * Return a property value as a double.
     * @throws ConfigurationException if the property cannot be found or the value cannot be parsed to a double.
     */
    double getDouble(String key);
    
    /**
     * Return a property value as a boolean.
     * @throws ConfigurationException if the property cannot be found or the value cannot be parsed to a boolean.
     */    
    boolean getBoolean(String key);
    
    /**
     * Return a property value as an array of strings.  Comma is used to delimit the strings in the property file.
     * @throws ConfigurationException if the property cannot be found or the value cannot be parsed to an array of strings.
     */
    String[] getStringArray(String key);
    
    /**
     * Return a property value as an array of integers.  Comma is used to delimit the integers in the property file.
     * @throws ConfigurationException if the property cannot be found or the value cannot be parsed to an array of integers.
     */
    int[] getIntArray(String key);
    
    /**
     * Return a property value as an array of doubles.  Comma is used to delimit the doubles in the property file.
     * @throws ConfigurationException if the property cannot be found or the value cannot be parsed to an array of doubles.
     */
    double[] getDoubleArray(String key);
    
    /**
     * Return a property value as a string. If no property is found, return the default value.
     */
    String getString(String key, String defaultValue);
    
    /**
     * Return a property value as an integer. If no property is found or the value cannot be parsed to an integer,
     * return the default value.
     */
    int getInt(String key, int defaultValue);
    
    /**
     * Return a property value as a double. If no property is found or the value cannot be parsed to a double,
     * return the default value.
     */
    double getDouble(String key, double defaultValue);
    
    /**
     * Return a property value as a boolean. If no property is found or the value cannot be parsed to a boolean,
     * return the default value.
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Return a property value as a string array. If no property is found, return the default value.
     */
    String[] getStringArray(String key, String[] defaultValue);
    
    /**
     * Return a property value as an integer array. If no property is found or the value cannot be parsed to an integer array,
     * return the default value.
     */
    int[] getIntArray(String key, int[] defaultValue);
    
    /**
     * Return a property value as a double array. If no property is found or the value cannot be parsed to a double array,
     * return the default value.
     */
    double[] getDoubleArray(String key, double[] defaultValue);
    
    static Configuration getConfiguration() {
        return CfgImpl.instance();
    }

    static Configuration getConfiguration(String fileName) {
        return CfgImpl.load(fileName);
    }
    
    @SuppressWarnings("serial")
    public static class ConfigurationException extends RuntimeException {
        public ConfigurationException(String message) {
            super(message);
        }
    }
    
}
