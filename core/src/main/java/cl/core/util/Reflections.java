package cl.core.util;

import static cl.core.decorator.exception.ExceptionDecorators.uncheck;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
     * Set a field value directly.
     * 
     * @param fieldName  Field name 
     * @param fieldValue Filed value
     * @param object     An object whose value we're setting
     */
    public static void setField(String fieldName, Object fieldValue, Object object) {
        uncheck(() -> getFld(fieldName, object).set(object, fieldValue));
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
