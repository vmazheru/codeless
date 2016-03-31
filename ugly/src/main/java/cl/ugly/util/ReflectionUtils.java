package cl.ugly.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

public final class ReflectionUtils {

    private ReflectionUtils(){}
    
    public static <T> Comparator<T> getterComparator(String fieldName, boolean isReversed) {
        String methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        Comparator<T> c = new Comparator<T>() {
            @Override public int compare(T o1, T o2) {
                Object result1 = callMethod(methodName, o1);
                Object result2 = callMethod(methodName, o2);
                
                // nulls ALWAYS last behavior
                if (result1 == null && result2 == null) return 0;
                else if (result1 == null && result2 != null) return isReversed ? -1 : 1;
                else if (result1 != null && result2 == null) return isReversed ? 1 : -1;
                else if (result1 instanceof Comparable) {
                    @SuppressWarnings({ "rawtypes", "unchecked" })
                    int result = ((Comparable)result1).compareTo(result2);
                    return result;
                }
                return 0;
            }
        };
        return isReversed ? c.reversed() : c;
    }
    
    public static Object callGetter(String fieldName, Object o) {
        String methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        return callMethod(methodName, o);
    }
    
    public static Object callMethod(String methodName, Object o) {
        try {
            Class<?> klass = o.getClass();
            Method method = klass.getMethod(methodName);
            return method.invoke(o);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(String fieldName, Object o) {
        try {
            Class<?> objClass = o.getClass();
            Field field = objClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T)field.get(o);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
}
