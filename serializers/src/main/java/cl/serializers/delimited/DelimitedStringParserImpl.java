package cl.serializers.delimited;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import cl.core.configurable.ConfigurableObject;
import cl.core.function.FunctionWithException;
import cl.core.util.Reflections;

class DelimitedStringParserImpl<T> extends ConfigurableObject<DelimitedStringParser<T>> implements DelimitedStringParser<T> {

    private final Supplier<T> objectFactory;
    private final Map<Integer, String> indexToProperty;
    private final Map<String, FunctionWithException<String, Object>> valueParsers;
    
    private boolean useSetters;
    private Consumer<PropertySetException> onPropertyError;
    
    DelimitedStringParserImpl(
            Supplier<T> objectFactory,
            Map<Integer, String> indexToProperty,
            Map<String, FunctionWithException<String, Object>> valueParsers) {
        this.objectFactory = objectFactory;
        this.indexToProperty = indexToProperty;
        this.valueParsers = new HashMap<>(valueParsers);
    }
    
    @Override
    public T parse(String[] values) {
        requireLock();
        
        if (values == null) return null;
        
        T object = objectFactory.get();
        
        for (int i = 0; i < values.length; i++) {
            String property = indexToProperty.get(i);
            if (property != null) {
                String valueStr = values[i];
                if (valueStr != null) {
                    FunctionWithException<String, Object> parser = valueParsers.get(property);
                    try {
                        if (parser != null) {
                            Object value = parser.apply(valueStr);
                            if (useSetters) {
                                Reflections.set(property, value, object);
                            } else {
                                Reflections.setField(property, value, object);
                            }
                        } else {
                            // we do not check 'useSetters' here,
                            // because it is guaranteed by the constructor
                            // that if it is even possible to call a setter,
                            // the value parser will be present.
                            Reflections.trySetField(property, valueStr, object);                            
                        }
                    } catch (Exception e) {
                        onPropertyError.accept(new PropertySetException(property, valueStr, object, e)); 
                    }
                }
            }
        }        
        
        return object;
    }
    
    @Override
    protected void build() {
        super.build();
        useSetters = get(DelimitedStringParser.useSetters);
        onPropertyError = get(DelimitedStringParser.onPropertySetError);
        
        validatePropertyNamesAndValueParsers();
    }

    /*
     * Verify that property names correctly identify object fields.
     * Also verify that there is a value parser whenever a string value can't be
     * set by using a "setter".
     */
    private void validatePropertyNamesAndValueParsers() {
        T obj = objectFactory.get();
        
        indexToProperty.values().forEach(p -> {
            if (useSetters && !valueParsers.containsKey(p)) {
                // if there is no value parser for this property when using setters,
                // see if we can set it as a String object.
                // If yes, add a value parser which takes a string and returns a string
                // If no, throw an error, for we don't know how to set this property.
                Reflections.set(p, "", obj);
                valueParsers.put(p, s -> s);
            } else {
                // when not using setters, just verify that a field with the
                // given name exists.
                Reflections.getField(p, obj);
            }
        });
    }
}
