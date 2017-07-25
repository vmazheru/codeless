package cl.serializers.delimited;

import static cl.core.decorator.exception.ExceptionDecorators.safely;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import cl.core.configurable.ConfigurableObject;
import cl.core.util.Reflections;

class DelimitedStringParserImpl<T> extends ConfigurableObject<DelimitedStringParser<T>> implements DelimitedStringParser<T> {

    private final Supplier<T> objectFactory;
    private final Map<Integer, String> indexToProperty;
    private final Map<String, Function<String, Object>> valueParsers;
    
    private boolean useSetters;
    private Consumer<PropertySetException> onPropertyError;
    
    DelimitedStringParserImpl(
            Supplier<T> objectFactory,
            Map<Integer, String> indexToProperty,
            Map<String, Function<String, Object>> valueParsers) {
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
                    Function<String, Object> parser = valueParsers.get(property);
                    try {
                        if (parser != null) {
                            Object value = parser.apply(valueStr);
                            if (useSetters) {
                                Reflections.set(property, value, object);
                            } else {
                                Reflections.setField(property, value, object);
                            }
                        } else {
                            if (useSetters) {
                                // we did try to find a parser for this property
                                // in the build(), and if there is no parser
                                // we cannot call the setter
                            } else {
                                Reflections.trySetField(property, valueStr, object);
                            }
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
        Class<?> klass = obj.getClass();
        
        indexToProperty.values().forEach(p -> {
            if (useSetters && !valueParsers.containsKey(p)) {
                // try to find value parsers for missing properties
                // by looking for appropriate setters
                Function<String, Object> parser = safely(() -> 
                    Reflections.findStringParserForSetter(p, klass));
                if (parser != null) {
                    valueParsers.put(p, parser);
                }
            } else {
                // when not using setters, just verify that a field with the
                // given name exists.
                Reflections.getField(p, obj);
            }
        });
    }
}
