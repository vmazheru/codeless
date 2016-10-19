package cl.core.util;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.lang.reflect.Method;

/**
 * Reflection utilities.
 *  
 * <p>Any checked exceptions thrown by reflection API will be
 * wrapped in run-time exceptions in all methods of this class.
 */
public final class Reflections {
    
    private Reflections(){}

    /**
     * Execute a setter.
     * 
     * @param propertyName   A property name to set. This is not a method name, but lower-cased property name.
     *                       (For example, it's not "setFirstName" but "firstName") 
     * @param propertyValue  A property value
     * @param object         An object, whose property we're setting.
     */
    public static void set(String propertyName, Object propertyValue, Object object) {
        uncheck(() -> {
            Method setter = object.getClass().getMethod(
                    "set" + Strings.capitalize(propertyName), propertyValue.getClass());
            setter.invoke(object, propertyValue);
        });
    }
}
