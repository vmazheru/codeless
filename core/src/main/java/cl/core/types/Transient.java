package cl.core.types;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Fields and methods (setters and getters) marked this annotation
 * will not be processed by reflection utilities from {@link cl.core.util.Reflections} class.
 */
@Target({FIELD, METHOD})
@Retention(RUNTIME)
public @interface Transient {}