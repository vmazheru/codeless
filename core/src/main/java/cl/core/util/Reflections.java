package cl.core.util;


import static cl.core.decorator.exception.ExceptionDecorators.safely;
import static cl.core.decorator.exception.ExceptionDecorators.uncheck;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;

import cl.core.function.FunctionWithException;
import cl.core.function.SupplierWithException;

/**
 * Reflection utilities.
 *  
 * <p>Any checked exceptions thrown by reflection API will be
 * wrapped in run-time exceptions in all methods of this class.
 */
@SuppressWarnings("unchecked")
public final class Reflections {
    
    private Reflections(){}

    /**
     * Execute a setter.
     * 
     * @param propertyName   A property name to set. This is not a method name, but a lower-cased property name.
     *                       (For example, it's not "setFirstName" but "firstName") 
     * @param propertyValue  A property value
     * @param object         An object, whose property we're setting.
     */
    public static void set(String propertyName, Object propertyValue, Object object) {
        call("set" + Strings.capitalize(propertyName), object, propertyValue);
    }
    
    /**
     * Execute a getter.
     * 
     * @param propertyName A property name. This is not a method name, but a lower-cased property name.
     * @param object       An object on which the method is being called.
     * @return             A property value
     */
    public static <T> T get(String propertyName, Object object) {
        return call("get" + Strings.capitalize(propertyName), object);
    }
    
    /**
     * Call a method which takes no arguments on a given object.
     * 
     * @param method A method name
     * @param object An object on which the method is being called
     * @return       The result of the method call
     */
    public static <T> T call(String method, Object object) {
        return (T)call(
                () -> object.getClass().getDeclaredMethod(method),
                m -> m.invoke(object));
    }
    
    /**
     * Call a method which takes one argument on a given object.
     * 
     * @param method A method name
     * @param object An object on which the method is being called
     * @param arg    A method argument
     * @return       The result of the method call
     */
    public static <T> T call(String method, Object object, Object arg) {
        return (T)call(
                () -> object.getClass().getDeclaredMethod(method, arg.getClass()),
                m -> m.invoke(object, arg));
    }
    
    /**
     * Call a method which takes two arguments on a given object.
     * 
     * @param method A method name
     * @param object An object on which the method is being called
     * @param arg1   The first method argument
     * @param arg2   The second method argument
     * @return       The result of the method call
     */
    public static <T> T call(String method, Object object, Object arg1, Object arg2) {
        return (T)call(
                () -> object.getClass().getDeclaredMethod(method, 
                            arg1.getClass(), arg2.getClass()),
                m -> m.invoke(object, arg1, arg2));
    }
    
    /**
     * Call a method which takes three arguments on a given object.
     * 
     * @param method A method name
     * @param object An object on which the method is being called
     * @param arg1   The first method argument
     * @param arg2   The second method argument
     * @param arg3   The third method argument
     * @return       The result of the method call
     */
    public static <T> T call(String method, Object object, 
            Object arg1, Object arg2, Object arg3) {
        return (T)call(
                () -> object.getClass().getDeclaredMethod(method, 
                            arg1.getClass(), arg2.getClass(), arg3.getClass()),
                m -> m.invoke(object, arg1, arg2, arg3));
    }
    
    /**
     * Call a method which take variable number of arguments on a given object.
     * 
     * @param method A method name    
     * @param object An object on which the method is being called
     * @param args   Method arguments. Note that, the arguments are of the same type
     * @return       The result of the method call
     */
    @SafeVarargs
    public static <T,P> T call(String method, Object object, P ... args) {
        return (T)call(() -> object.getClass().getDeclaredMethod(method, args.getClass()),
                m -> m.invoke(object, new Object[] {args}));
    }

    /**
     * Get a value directly from an object field.
     * 
     * @param fieldName Field name
     * @param object    An object whose value we're getting
     * @return          The field value
     */
    public static <T> T getField(String fieldName, Object object) {
        return (T)uncheck(() -> getFld(fieldName, object).get(object));
    }

    /**
     * Check if a field with given name exists in the object.
     */
    public static boolean fieldExists(String fieldName, Object object) {
        return null != safely(() -> getFld(fieldName, object), NoSuchFieldException.class);
    }
    
    /**
     * Set a field value directly.
     * 
     * @param fieldName  Field name 
     * @param fieldValue Filed value
     * @param object     An object whose value we're setting
     */
    public static void setField(String fieldName, Object fieldValue, Object object) {
        uncheck(() -> getFld(fieldName, object).set(object, fieldValue));
    }
    
    /**
     * Set a field directly without knowledge of the field type. The method will
     * try to parse the given string value according to the field type discovered
     * via reflection.
     * 
     * <p>
     * The method supports all Java primitive types and the wrapper types, as well as
     * String, BigInteger, BigDecimal, Date, LocalDate, LocalDateTime, LocalTime,
     * and ZonedDateTime types.
     * 
     * @param fieldName  Field name
     * @param fieldValue Field value string representation
     * @param object     An object whose value we're setting
     * 
     * @throws UnsupportedOperationException if the type of the field is not supported
     * by this method
     */
    public static void trySetField(String fieldName, String fieldValue, Object object) {
        if (fieldValue == null || fieldValue.isEmpty()) return;
        
        Object o = object;
        String v = fieldValue;

        uncheck(() -> {
            Field field = getFld(fieldName, object);
            Class<?> klass = field.getType();

            if (klass == String.class) field.set(o, v);
            
            else if (klass == int.class) field.setInt(o, Integer.parseInt(v));
            else if (klass == Integer.class) field.set(o, Integer.valueOf(v));
            
            else if (klass == double.class) field.setDouble(o, Double.parseDouble(v));
            else if (klass == Double.class) field.set(o, Double.valueOf(v));
            
            else if (klass == long.class) field.setLong(o, Long.parseLong(v));
            else if (klass == Long.class) field.set(o, Long.valueOf(v));
            
            else if (klass == float.class) field.setFloat(o, Float.parseFloat(v));
            else if (klass == Float.class) field.set(o, Float.valueOf(v));
            
            else if (klass == boolean.class) field.setBoolean(o, Boolean.parseBoolean(v));
            else if (klass == Boolean.class) field.set(o, Boolean.valueOf(v));
            
            else if (klass == BigDecimal.class) field.set(o, new BigDecimal(v));
            else if (klass == BigInteger.class) field.set(o, new BigInteger(v));
            
            else if (klass == LocalDateTime.class) field.set(o, LocalDateTime.parse(v));
            else if (klass == LocalDate.class) field.set(o, LocalDate.parse(v));
            else if (klass == LocalTime.class) field.set(o, LocalTime.parse(v));
            else if (klass == Date.class) field.set(o, Dates.DATE_TO_STRING_FORMAT.parse(v));
            else if (klass == ZonedDateTime.class) field.set(o, ZonedDateTime.parse(v));
            
            else if (klass == char.class) field.setChar(o, v.charAt(0));
            else if (klass == Character.class) field.set(o, Character.valueOf(v.charAt(0)));
            
            else if (klass == byte.class) field.setByte(o, Byte.parseByte(v));
            else if (klass == Byte.class) field.set(o, Byte.valueOf(v));
            
            else if (klass == short.class) field.setShort(o, Short.parseShort(v));
            else if (klass == Short.class) field.set(o, Short.valueOf(v));

            else throw new UnsupportedOperationException(
                    "Cannot set value '" + fieldValue + "' to object of type " + klass);
        });
    }    
    
    /**
     * Create a new instance of the given class without throwing a compile-time
     * exception (but it will throw run rime exception in case of instantiation problem).
     */
    public static <T> T newInstance(Class<T> klass) {
        return uncheck(() -> {
            Constructor<T> c = klass.getConstructor();
            c.setAccessible(true);
            return c.newInstance();
        });
    }
    
    private static Field getFld(String fieldName, Object object) throws NoSuchFieldException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
    
    private static <T> T call(
            SupplierWithException<Method> methodSupplier, 
            FunctionWithException<Method, T> methodToResultFunction) {
        return uncheck(() -> methodToResultFunction.apply(methodSupplier.get()));
    }
    
}
