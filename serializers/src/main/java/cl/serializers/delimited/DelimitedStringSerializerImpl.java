package cl.serializers.delimited;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import cl.core.configurable.ConfigurableObject;
import cl.core.util.Reflections;

class DelimitedStringSerializerImpl<T> extends ConfigurableObject<DelimitedStringSerializer<T>> implements DelimitedStringSerializer<T> {
    
    private final SortedMap<Integer, String> indexToProperty;
    private final Map<String, Function<Object, String>> valueSerializers;
    
    private boolean useGetters;
    private boolean exactProperties;
    
    private Function<Object, String>[] serializers;
    
    DelimitedStringSerializerImpl(
            Map<Integer, String> indexToProperty,
            Map<String, Function<Object, String>> valueSerializers) {

        // we will iterate this map in the reversed key order later
        SortedMap<Integer, String> idxToProperty = new TreeMap<>(Comparator.reverseOrder());
        idxToProperty.putAll(indexToProperty);
        
        this.indexToProperty = idxToProperty;
        this.valueSerializers = new HashMap<>(valueSerializers);
    }

    @Override
    public String[] serialize(T obj) {
        requireLock();
        
        if (obj == null) return null;

        if (serializers == null) {
            initSerializers(obj.getClass());
        }
        
        return Stream.of(serializers).map(s -> s.apply(obj)).toArray(String[]::new);
    }
    
    @Override
    protected void build() {
        super.build();
        useGetters = get(DelimitedStringSerializer.useGetters);
        exactProperties = get(DelimitedStringSerializer.exactProperties);
    }

    // Initialize functions which serialize object properties.
    // The number and order of these serializers is defined by:
    //   1) indexToProperty map (which defines mandatory indexes for certain properties)
    //   2) exactProperties configuration setting, which defines whether the serializers
    //      should be given only to properties in the "indexToProperty" map or not
    //   3) the order of object properties as returned by the reflection call
    private void initSerializers(Class<?> klass) {
        
        // get properties from the object in the order returned by the
        // reflection call. Use linked hash set to preserve the order of iteration
        Set<String> propertiesFromObject = new LinkedHashSet<>(Arrays.asList(
                useGetters ? 
                        Reflections.getPropertiesFromGetters(klass) :
                        Reflections.getPropertiesFromFields(klass)
        ));

        // remove all properties given in the "indexToProperty" map which do not
        // exist in this object (bugs in the code)
        Map<Integer, String> validatedIndexToProperty = indexToProperty.entrySet().stream()
            .filter(e -> propertiesFromObject.contains(e.getValue()))
            .collect(toMap(e -> e.getKey(), e -> e.getValue()));
        
        // remove all valid properties from "indexToProperty" map from the
        // object properties.  This will make these two sets disjoint.
        // or remove all properties from it if "exactProperties" is true
        if (exactProperties) {
            propertiesFromObject.clear();    
        } else {
            propertiesFromObject.removeAll(validatedIndexToProperty.values());
        }

        // create an array which will contain properties in the required order
        String[] finalProperties = new String[validatedIndexToProperty.size() + propertiesFromObject.size()];
        
        // put properties from "indexToProperty" map under required indexes to the final array
        // if, for some reason, a property index is larger than the size of the array
        // put them in the end
        int end = finalProperties.length - 1;
        for (Map.Entry<Integer, String> e : validatedIndexToProperty.entrySet()) {
            int index = e.getKey();
            if (index < finalProperties.length) {
                finalProperties[index] = e.getValue();
            } else {
                finalProperties[end--] = e.getValue();
            }
        }
        
        // now, fill the missing places with properties which was not specified
        // in the "indexToProperty" map
        int idx = 0;
        for (String prop : propertiesFromObject) {
            while (finalProperties[idx] != null) { idx++; } // skip the positions which have been taken
            finalProperties[idx++] = prop;
        }
        
        // now that we figure out the properties, lets build serializers
        // for these properties and put them in array in the same order
        
        @SuppressWarnings("unchecked")
        Function<Object, String>[] serializers = new Function[finalProperties.length];
        
        // this function defines type of reflection call we make (we either use getters of field)
        // this function takes a property name, an object itself, and returns a value
        // for this property
        BiFunction<String, Object, Object> getPropertyValue = useGetters ? 
                Reflections::get :
                Reflections::getField;
        
        // for each property...
        for (int i = 0; i < finalProperties.length; i++) {
            String property = finalProperties[i];

            // create a function which converts the given property to a string
            // by either passing it to the explicitly given serializer
            // or, if no such serializer exists, by calling toString()
            // this function accepts the object returned by the reflection call (field or getter value)
            // and converts it to a string
            Function<Object, String> valueSerializer = valueSerializers.get(property);
            Function<Object, String> vSerializer = value -> {
                if (value == null) return "";
                return valueSerializer != null ? valueSerializer.apply(value) : value.toString();
            };
            
            // finally, create a function for this specific property
            // which accepts the whole object and returns a string
            // and place it in the final array
            Function<Object, String> finalSerializer = object -> {
                        Object value = getPropertyValue.apply(property, object);
                        return vSerializer.apply(value);
                    };
            
            serializers[i] = finalSerializer;
        }
        
        this.serializers = serializers;
    }

}
