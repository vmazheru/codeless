package cl.serializers.writers;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;

import cl.core.util.Reflections;
import cl.core.util.Sets;
import cl.serializers.SerializerConfiguration;
import cl.serializers.delimited.DelimitedStringJoiner;
import cl.serializers.delimited.DelimitedStringSerializer;
import cl.serializers.delimited.DelimitedStringSplitter;

public class DelimitedStringWriter<T> extends TextWriter<T> {
    
    private final Class<T> klass;
    private DelimitedStringJoiner joiner;
    private DelimitedStringSerializer<T> serializer;
    
    private DelimitedStringWriter(File file, Class<T> klass) {
        super(file);
        this.klass = klass;
    }
    
    private DelimitedStringWriter(OutputStream outputStream, Class<T> klass) {
        super(outputStream);
        this.klass = klass;
    }
    
    public static <T> ObjectWriter<T> toFile(File file, Class<T> klass, boolean lockConfiguration) {
        ObjectWriter<T> w = new DelimitedStringWriter<>(file, klass);
        if (lockConfiguration) w.locked();
        return w;
    }
    
    public static <T> ObjectWriter<T> toFile(File file, Class<T> klass) {
        return toFile(file, klass, true);
    }
    
    public static <T> ObjectWriter<T> toOutputStream(
            OutputStream outputStream, Class<T>klass, boolean lockConfiguration) {
        ObjectWriter<T> w = new DelimitedStringWriter<>(outputStream, klass);
        if (lockConfiguration) w.locked();
        return w;
    }
    
    public static <T> ObjectWriter<T> toOutputStream(OutputStream outputStream, Class<T> klass) {
        return toOutputStream(outputStream, klass, true);
    }
    
    /**
     * @see cl.serializers.writers.ObjectWriter#clone(java.io.File)
     */
    @Override
    public ObjectWriter<T> clone(File file) {
        return DelimitedStringWriter.<T>toFile(file, klass, false).withConfigurationFrom(this).locked();
    }
    
    /**
     * @see cl.serializers.writers.ObjectWriter#clone(java.io.OutputStream)
     */
    @Override
    public ObjectWriter<T> clone(OutputStream outputStream) {
        return DelimitedStringWriter.<T>toOutputStream(outputStream, klass, false)
                .withConfigurationFrom(this).locked();
    }
    
    @Override
    protected String toString(T obj) {
        return joiner.join(serializer.serialize(obj));
    }

    @Override
    protected void build() {
        super.build();
        
        DelimitedStringJoiner joiner = get(SerializerConfiguration.delimitedStringJoiner);
        DelimitedStringSplitter splitter = get(SerializerConfiguration.delimitedStringSplitter);
        boolean useGetters = get(SerializerConfiguration.useGetters);
        boolean exactProperties = get(SerializerConfiguration.exactProperties);
        Function<String, String> columnToProperty = get(SerializerConfiguration.columnToProperty);
        Collection<String> headerLines = get(SerializerConfiguration.headerLines);
        Predicate<String> propertyChecker = useGetters ?
                p -> Reflections.getterExists(p, klass) :
                p -> Reflections.fieldExists(p, klass);
                
        Map<Integer, String> indexToProperty = 
                new TreeMap<>(get(SerializerConfiguration.columnIndexToProperty));
        
        Set<String> propertiesFromObject = new LinkedHashSet<>(Arrays.asList(
                useGetters ? 
                        Reflections.getPropertiesFromGetters(klass) :
                        Reflections.getPropertiesFromFields(klass)
        ));
        
        indexToProperty.entrySet().stream().forEach(e -> {
            if (!propertiesFromObject.contains(e.getValue())) {
                throw new RuntimeException("Property '" + e.getValue() + 
                        "' cannot be found in an object of class " + klass.getName()); 
            }
        });
        
        if (!headerLines.isEmpty()) {
            boolean firstHeaderLine = headerLines.isEmpty() ? false : true;
            for (String header : headerLines) {
                if (firstHeaderLine) {
                    firstHeaderLine = false;
                    
                    String[] columnNames = splitter.split(header);
                    List<String> validColumnNames = new ArrayList<>();
                    
                    for (int j = 0; j < columnNames.length; j++) {
                        String columnName = columnNames[j];
                        if (indexToProperty.containsKey(j)) {
                            validColumnNames.add(columnName);    
                        } else {
                            String property = columnToProperty.apply(columnName);
                            if (propertyChecker.test(property)) {
                                indexToProperty.put(j, property);
                                validColumnNames.add(columnName);
                            }
                        }
                    }
                    
                    // write modified header (may not have all the columns from the original header)
                    writer.println(joiner.join(validColumnNames.toArray(new String[validColumnNames.size()])));
                } else {
                    writer.println(header);
                }
            }
        } else if (exactProperties) { // no header and exact properties == TRUE
            if (get(SerializerConfiguration.generateHeaderIfAbsent)) {
                List<String> columnNames = new ArrayList<>();
                Function<String, String> propertyToColumn = get(SerializerConfiguration.propertyToColumn);
                
                for (Map.Entry<Integer, String> e : indexToProperty.entrySet()) {
                    columnNames.add(propertyToColumn.apply(e.getValue()));
                }
                
                writer.println(joiner.join(columnNames.toArray(new String[columnNames.size()])));
            }
        } else {  // no header and exact properties == FALSE
            if (get(SerializerConfiguration.generateHeaderIfAbsent)) {
                
                List<String> columnNames = new ArrayList<>();
                Function<String, String> propertyToColumn = get(SerializerConfiguration.propertyToColumn);
                
                for (Map.Entry<Integer, String> e : indexToProperty.entrySet()) {
                    columnNames.add(propertyToColumn.apply(e.getValue()));
                }
                
                Set<String> missedProperties = Sets.difference(
                        propertiesFromObject, indexToProperty.values());
                
                for (String p : missedProperties) columnNames.add(p);
            }
        }
        
        serializer = DelimitedStringSerializer.<T>get(
                indexToProperty, get(SerializerConfiguration.valueSerializers), false)
                    .with(DelimitedStringSerializer.useGetters, useGetters)
                    .with(DelimitedStringSerializer.exactProperties, true)
                    .locked();
        
        this.joiner = joiner;
    }
 
}
