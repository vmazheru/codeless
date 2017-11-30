package cl.core.util;

import static cl.core.decorator.exception.ExceptionDecorators.safely;
import static cl.core.decorator.exception.ExceptionDecorators.uncheck;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import cl.core.ds.Pair;
import cl.core.function.FunctionWithException;
import cl.core.function.SupplierWithException;
import cl.core.function.stringparser.StringParsers;
import cl.core.function.stringparser.StringParsers.StringParser;
import cl.core.types.Transient;

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

    /**
     * Execute a setter.
     * 
     * @param propertyName   A property name to set. This is not a method name, but a lower-cased property name.
     *                       (For example, it's not "setFirstName" but "firstName") 
     * @param propertyValue  A property value
     * @param object         An object, whose property we're setting.
     */
    public static void set(String propertyName, Object propertyValue, Object object) {
        call(getSetterName(propertyName), object, propertyValue);
    }
    
    /**
     * Execute a getter.
     * 
     * @param propertyName A property name. This is not a method name, but a lower-cased property name.
     * @param object       An object on which the method is being called.
     * @return             A property value
     */
    public static <T> T get(String propertyName, Object object) {
        return call(getGetterName(propertyName), object);
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
                () -> object.getClass().getMethod(method),
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
                () -> object.getClass().getMethod(method, arg.getClass()),
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
                () -> object.getClass().getMethod(method, 
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
                () -> object.getClass().getMethod(method, 
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
        return (T)call(() -> object.getClass().getMethod(method, args.getClass()),
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
        return (T)uncheck(() -> getFld(fieldName, object.getClass()).get(object));
    }

    /**
     * Check if a field with the given name exists in the object.
     */
    public static boolean fieldExists(String fieldName, Object object) {
        return fieldExists(fieldName, object.getClass());
    }
    
    /**
     * Check if a field with the given name exists in the class.
     */
    public static boolean fieldExists(String fieldName, Class<?> klass) {
        return null != safely(() -> getFld(fieldName, klass), NoSuchFieldException.class);
    }
    
    /**
     * Set a field value directly.
     * 
     * @param fieldName  Field name 
     * @param fieldValue Filed value
     * @param object     An object whose value we're setting
     */
    public static void setField(String fieldName, Object fieldValue, Object object) {
        uncheck(() -> getFld(fieldName, object.getClass()).set(object, fieldValue));
    }
    
    /**
     * Set a field directly without knowledge of the field type. The method will
     * try to parse the given string value according to the field type discovered
     * via reflection.
     * 
     * <p>
     * This method will use {@link cl.core.function.stringparser.StringParsers}
     * class to parse strings into objects.
     *
     * @param fieldName  Field name
     * @param fieldValue Field value string representation
     * @param object     An object whose value we're setting
     * 
     * @throws UnsupportedOperationException if the type of the field is not supported
     * by this method
     * 
     * @see cl.core.function.stringparser.StringParsers
     */
    public static void trySetField(String fieldName, String fieldValue, Object object) {
        if (fieldValue == null || fieldValue.isEmpty()) return;
        
        Object o = object;
        String v = fieldValue;

        uncheck(() -> {
            Field field = getFld(fieldName, o.getClass());
            Class<?> klass = field.getType();
            if (klass.isPrimitive()) {
                if (klass == int.class) field.setInt(o, Integer.parseInt(v));
                else if (klass == double.class) field.setDouble(o, Double.parseDouble(v));
                else if (klass == long.class) field.setLong(o, Long.parseLong(v));
                else if (klass == float.class) field.setFloat(o, Float.parseFloat(v));
                else if (klass == boolean.class) field.setBoolean(o, Boolean.parseBoolean(v));
                else if (klass == char.class) field.setChar(o, v.charAt(0));
                else if (klass == byte.class) field.setByte(o, Byte.parseByte(v));
                else if (klass == short.class) field.setShort(o, Short.parseShort(v));
            }  else if (klass.isEnum()) {
                @SuppressWarnings("rawtypes")
                Class enumClass = klass;
                field.set(o, Enum.valueOf(enumClass, v));
            } else {
                StringParser<?> p = StringParsers.get(klass);
                if (p != null) {
                    field.set(o, p.parse(v));
                } else {
                    throw new UnsupportedOperationException(
                            "Cannot parse string value '" + v + "' to instance of class " + klass);
                }
            }
        });
    }
    
    /**
     * Find a setter method (with one parameter only) by property name amongst the object's methods,
     * figure out the method parameter type, parse a given String input to object of that type,
     * and, finally, call the setter.
     * 
     * @param property property name in Camel case
     * @param value    a string value which will be parsed into an object which matches the setter
     *                 parameter type
     * @param object   an object whose setter we are calling
     * 
     * @throws UnsupportedOperationException if a method is not found by the property name
     *                                       or no string parser found for the method parameter type
     */
    public static void findAndCallSetter(String property, String value, Object object) {
        Pair<Method, Function<String, Object>> methodAndParser =
                getSetterWithParser(property, object.getClass());
        uncheck(() -> methodAndParser._1().invoke(object, methodAndParser._2().apply(value)));
    }
    
    /**
     * Find a setter method (with one parameter only) by property name, and
     * return a string parser for this method's parameter type
     * 
     * @param property property name in Camel case
     * @param klass    object class
     * 
     * @throws UnsupportedOperationException if a method is not found by the property name
     *                                       or no string parser found for the method parameter type
     */
    public static Function<String, Object> findStringParserForSetter(String property, Class<?> klass) {
        return getSetterWithParser(property, klass)._2();
    }

    /**
     * Return true if a getter exists for the given property in the given object.
     */
    public static boolean getterExists(String property, Object object) {
        return getterExists(property, object.getClass());
    }
    
    /**
     * Return true if a getter exists for the given property in the given class.
     */
    public static boolean getterExists(String property, Class<?> klass) {
        return findGetter(property, klass).isPresent();
    }
    
    /**
     * Return true if a setter exists for the given property in the given object.
     */
    public static boolean setterExists(String property, Object object) {
        return setterExists(property, object.getClass());
    }
    
    /**
     * Return true if a setter exists for the given property in the given class.
     */
    public static boolean setterExists(String property, Class<?> klass) {
        return findSetter(property, klass).isPresent();
    }

    /**
     * Return an array of field names derived from fields. This will not include static and transient fields.
     */
    public static String[] getPropertiesFromFields(Class<?> klass) {
        return getFields(klass).map(Field::getName).toArray(String[]::new);
    }
    
    /**
     * Return an array of field names derived from getters. This will not include
     * static, abstract, and {@link cl.core.types.Transient} methods.
     */
    public static String[] getPropertiesFromGetters(Class<?> klass) {
        return getGetters(klass).map(m -> Strings.pascalToCamel(m.getName().substring(3)))
            .toArray(String[]::new);
    }
    
    /**
     * Return an array of field names derived from setters. This will not include
     * static, abstract, and {@link cl.core.types.Transient} methods.
     */
    public static String[] getPropertiesFromSetters(Class<?> klass) {
        return getSetters(klass).map(m -> Strings.pascalToCamel(m.getName().substring(3)))
            .toArray(String[]::new);
    }
    
    private static Stream<Field> getFields(Class<?> klass) {
        Predicate<Field> isSerializableField = f ->
            !Modifier.isStatic(f.getModifiers()) && 
            !Modifier.isTransient(f.getModifiers()) && 
            f.getAnnotation(Transient.class) == null &&
            f.getName().charAt(0) != '$' &&  // reference to the outer object in inner classes in Scala
            !f.getName().startsWith("this"); // reference to the outer object in inteer classes in Java
            
        return Stream.of(klass.getDeclaredFields()).filter(isSerializableField);
    }
    
    private static Stream<Method> getGetters(Class<?> klass) {
            return getNonTransientOrStaticMethods(klass)
                    .filter(m -> m.getParameterCount() == 0 && m.getName().startsWith("get") && 
                    !"getClass".equals(m.getName()));
    }
    
    private static Stream<Method> getSetters(Class<?> klass) {
        return getNonTransientOrStaticMethods(klass)
                .filter(m -> m.getParameterCount() == 1 && m.getName().startsWith("set"));
    }
    
    private static Field getFld(String fieldName, Class<?> klass) throws NoSuchFieldException {
        Field field = klass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
    
    private static <T> T call(
            SupplierWithException<Method> methodSupplier, 
            FunctionWithException<Method, T> methodToResultFunction) {
        return uncheck(() -> methodToResultFunction.apply(methodSupplier.get()));
    }
    
    private static String getSetterName(String propertyName) {
        return "set" + Strings.capitalize(propertyName);
    }
    
    private static String getGetterName(String propertyName) {
        return "get" + Strings.capitalize(propertyName);
    }
    
    private static Optional<Method> findGetter(String property, Class<?> klass) {
        String getterName = getGetterName(property);
        return getNonTransientOrStaticMethods(klass)
                .filter(m -> m.getParameterCount() == 0 && m.getName().equals(getterName))
                .findFirst();
    }
    
    private static Optional<Method> findSetter(String property, Class<?> klass) {
        String setterName = getSetterName(property);
        return getNonTransientOrStaticMethods(klass)
            .filter(m -> m.getParameterCount() == 1 && m.getName().equals(setterName))
            .findFirst();
    }
    
    private static Stream<Method> getNonTransientOrStaticMethods(Class<?> klass) {
        return Stream.of(klass.getMethods()).filter(isNotTransientAbstractOrStatic);
    }
    
    private static Predicate<Method> isNotTransientAbstractOrStatic = m ->
        !Modifier.isStatic(m.getModifiers()) && 
        !Modifier.isAbstract(m.getModifiers()) &&
        m.getAnnotation(Transient.class) == null;
    
    private static Pair<Method, Function<String, Object>> getSetterWithParser(String property, Class<?> klass) {
        return
            findSetter(property, klass).map(m -> {
                Class<?> parameterType = m.getParameterTypes()[0];
                StringParser<?> p = null;
                if (parameterType.isPrimitive()) {
                    if (parameterType == int.class) p = StringParsers.intParser;
                    else if (parameterType == double.class) p = StringParsers.doubleParser;
                    else if (parameterType == long.class) p = StringParsers.longParser;
                    else if (parameterType == float.class) p = StringParsers.floatParser;
                    else if (parameterType == boolean.class) p = StringParsers.booleanParser;
                    else if (parameterType == char.class) p = StringParsers.charParser;
                    else if (parameterType == byte.class) p = StringParsers.byteParser;
                    else if (parameterType == short.class) p = StringParsers.shortParser; 
                } else if (parameterType.isEnum()) {
                    @SuppressWarnings("rawtypes")
                    Class enumClass = parameterType;
                    p = s -> Enum.valueOf(enumClass, s);
                } else {
                    p = StringParsers.get(parameterType);
                }
                
                if (p == null) {
                    throw new UnsupportedOperationException(
                            "Cannot find string parser for method's '" + getSetterName(property) + 
                            "' parameter type '" + parameterType + "'");
                }
                
                return new Pair<>(m, p.toFunction());
            }).orElseThrow(() -> new UnsupportedOperationException("No method found by name " + getSetterName(property)));
    }
    
}
