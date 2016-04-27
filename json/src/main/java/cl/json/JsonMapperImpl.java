package cl.json;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import cl.core.configurable.ConfigurableObject;
import cl.core.function.SupplierWithException;


/**
 * JSON mapper implementation, which uses Jackson library.
 */
final class JsonMapperImpl extends ConfigurableObject<JsonMapper> implements JsonMapper {
    
    private ObjectMapper objectMapper;

    JsonMapperImpl(){}

    @Override
    public String toJson(Object o) {
        return withLockCheck(() -> objectMapper.writeValueAsString(o));
    }    
    
    @Override
    public <T> T fromJson(String json, Class<T> klass) {
        return withLockCheck(() -> objectMapper.readValue(json, klass));
    }

    @Override
    protected void build() {
        objectMapper = new ObjectMapper();
        
        // get configurable property values
        Visibility v          = get(visibility);
        boolean failOnUnknown = get(failOnUnknownProperties);
        boolean pretty        = get(prettyPrinting);
        boolean wrapRoot      = get(wrapRootValue);
        boolean unwrapArr     = get(unwrapSingleElementArrays);
                
        JsonAutoDetect.Visibility methodVisibility = v == Visibility.METHOD ? JsonAutoDetect.Visibility.ANY : JsonAutoDetect.Visibility.NONE;
        JsonAutoDetect.Visibility fieldVisibility  = v != Visibility.METHOD ? JsonAutoDetect.Visibility.ANY : JsonAutoDetect.Visibility.NONE;
        
        // build it
        objectMapper.setVisibilityChecker(
                objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                    .withFieldVisibility(fieldVisibility)
                    .withGetterVisibility(methodVisibility)
                    .withSetterVisibility(methodVisibility)
                    .withIsGetterVisibility(methodVisibility)
                    .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        
        objectMapper
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknown)
            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, pretty)
            .configure(SerializationFeature.WRAP_ROOT_VALUE, wrapRoot)
            .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, wrapRoot)
            .configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, unwrapArr)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, unwrapArr)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .registerModule(new JSR310Module()) // register serialization of Java 8 time API classes
            .registerModule(                    // override serialization for time classes to be more readable then defined in the JSR310 module
                new SimpleModule()
                    .addSerializer(LocalDateTime.class, ToStringSerializer.instance)
                    .addSerializer(LocalDate.class, ToStringSerializer.instance)
                    .addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE)
                    .addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE));
    }
    
    private <R> R withLockCheck(SupplierWithException<R> f) {
        requireLock();
        return uncheck(JsonMapperException.class, f);
    }

}
