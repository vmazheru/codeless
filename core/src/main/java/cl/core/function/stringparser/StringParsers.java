package cl.core.function.stringparser;

import static cl.core.decorator.exception.ExceptionDecorators.uncheck;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import cl.core.util.Dates;

/**
 * This class contains functions which convert (parse) strings to objects.
 * This class also provides a way to globally register parsers for custom types.
 */
public final class StringParsers {
    
    private StringParsers(){}
    
    /**
     * Register a new parser for the given type.  Note, that the new parser
     * will replace the existing parser for that type if any globally, that is
     * for the entire application.
     */
    public static <T> void register(Class<T> klass, StringParser<T> parser) {
        synchronized (parsers) {
            parsers.put(klass, parser);
        }
    }
    
    /**
     * Remove (unregister) a parser for the given type.
     */
    public static <T> void unregister(Class<T> klass) {
        synchronized (parsers) {
            parsers.remove(klass);
        }
    }
    
    /**
     * Get a parser for the given type.
     */
    public static <T> StringParser<T> get(Class<T> klass) {
        synchronized (parsers) {
            @SuppressWarnings("unchecked")
            StringParser<T> p = (StringParser<T>)parsers.get(klass);
            return p;
        }
    }
    
    /**
     * A function which takes a {@code String} and returns an object of some
     * different type.
     *
     * @param <T> the type of object returned by this function
     */
    @FunctionalInterface
    public interface StringParser<T> {
        T parse(String s);
        
        /**
         * Represent this parser as a {@code Function} which returns an {@code Object}.
         */
        default Function<String, Object> toFunction() {
            return s -> parse(s);
        }
    }
    
    /**
     * Parse a {@code String} to a {@code Byte}.
     * This parser delegates to {@link Byte#valueOf(String)} method.
     */
    public static StringParser<Byte> byteParser = Byte::valueOf;
    
    /**
     * Parse a {@code String} to a {@code Short}.
     * This parser delegates to {@link Short#valueOf(String)} method.
     */
    public static StringParser<Short> shortParser = Short::valueOf;
    
    /**
     * Parse a {@code String} to an {@code Integer}.
     * This parser delegates to {@link Integer#valueOf(String)} method.
     */
    public static StringParser<Integer> intParser = Integer::valueOf;
    
    /**
     * Parse a {@code String} to a {@code Long}.
     * This parser delegates to {@link Long#valueOf(String)} method.
     */
    public static StringParser<Long> longParser = Long::valueOf;
    
    /**
     * Parse a {@code String} to a {@code Float}.
     * This parser delegates to {@link Float#valueOf(String)} method.
     */
    public static StringParser<Float> floatParser = Float::valueOf;
    
    /**
     * Parse a {@code String} to a {@code Double}.
     * This parser delegates to {@link Double#valueOf(String)} method.
     */
    public static StringParser<Double> doubleParser = Double::valueOf;
    
    /**
     * Parse a {@code String} to a {@code Character}.
     * It returns the first character of the string.
     */
    public static StringParser<Character> charParser = s -> Character.valueOf(s.charAt(0));
    
    /**
     * Parse a {@code String} to a {@code Boolean}.
     * This parser delegates to {@link Boolean#valueOf(String)} method.
     */
    public static StringParser<Boolean> booleanParser = Boolean::valueOf;
    
    /**
     * Parse a {@code String} to a {@code BigInteger}
     * This parser delegates to {@link java.math.BigInteger} constructor.
     */
    public static StringParser<BigInteger> bigIntegerParser = BigInteger::new;
    
    /**
     * Parse a {@code String} to a {@code BigDecimal}
     * This parser delegates to {@link java.math.BigDecimal} constructor.
     */
    public static StringParser<BigDecimal> bigDecimalParser = BigDecimal::new;
    
    /**
     * Parse a {@code String} to a {@code LocalDateTime} object.
     * This parser delegates to {@link java.time.LocalDateTime#parse(CharSequence)} method.
     */
    public static StringParser<LocalDateTime> localDateTimeParser = LocalDateTime::parse;
    
    /**
     * Parse a {@code String} to a {@code LocalDate} object.
     * This parser delegates to {@link java.time.LocalDate#parse(CharSequence)} method.
     */
    public static StringParser<LocalDate> localDateParser = LocalDate::parse;
    
    /**
     * Parse a {@code String} to a {@code LocalTime} object.
     * This parser delegates to {@link java.time.LocalTime#parse(CharSequence)} method.
     */
    public static StringParser<LocalTime> localTimeParser = LocalTime::parse;
    
    /**
     * Parse a {@code String} to a {@code ZonedDateTime} object.
     * This parser delegates to {@link java.time.ZonedDateTime#parse(CharSequence)} method.
     */
    public static StringParser<ZonedDateTime> zonedDateTimeParser = ZonedDateTime::parse;
    
    /**
     * Parse a {@code String} to a {@code Date} object.
     * This parser accepts strings in the format used by the {@code Date#toString()}
     * method. Note, that this parser does not read milliseconds since the
     * {@code Date#toString()} method does not dump them.
     * 
     * @see cl.core.util.Dates#DATE_TO_STRING_FORMAT for the format description.
     */
    public static StringParser<Date> dateParser = s -> uncheck(() -> Dates.DATE_TO_STRING_FORMAT.parse(s));
    
    /**
     * Parse a {@code String} as is. This parser just returns the input.
     */
    public static StringParser<String> stringParser = s -> s;
    
    private static Map<Class<?>, StringParser<?>> parsers = new HashMap<>();
    static {
        parsers.put(Byte.class, byteParser);
        parsers.put(Short.class, shortParser);
        parsers.put(Integer.class, intParser);
        parsers.put(Long.class, longParser);
        parsers.put(Float.class, floatParser);
        parsers.put(Double.class, doubleParser);
        parsers.put(Character.class, charParser);
        parsers.put(Boolean.class, booleanParser);
        parsers.put(BigInteger.class, bigIntegerParser);
        parsers.put(BigDecimal.class, bigDecimalParser);
        parsers.put(LocalDateTime.class, localDateTimeParser);
        parsers.put(LocalDate.class, localDateParser);
        parsers.put(LocalTime.class, localTimeParser);
        parsers.put(ZonedDateTime.class, zonedDateTimeParser);
        parsers.put(Date.class, dateParser);
        parsers.put(String.class, stringParser);
    }

}
