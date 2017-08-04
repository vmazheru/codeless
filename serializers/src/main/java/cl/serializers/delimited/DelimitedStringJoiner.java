package cl.serializers.delimited;

import java.util.Collection;
import java.util.stream.Stream;

import cl.core.configurable.Configurable;
import cl.core.configurable.Key;

/**
 * Objects of this can join objects (as their string representations) into delimited strings (CSV, tab-delimited, space-delimited etc).
 *
 * <p>Objects of this type are configurable and the following configuration keys are defined:
 * <ul>
 * 	<li>{@code delimiter} is a character which will be used to join the objects. The default value is comma.</li>
 *  <li>{@code encloser} is a character which will be used to enclose the values whenever necessary. The default value is a double quote</li>
 *  <li>{@code trim} of type boolean, denotes whether the values need to be trimmed or not. The default value is FALSE</li>
 *  <li>{@code alwaysEnclose} of type boolean, may be used when enclosing all values is desired. The default value is FALSE</li>
 *  <li>{@code checkForNewLines} of type boolean, if set, will replace new lines with spaces. The default value is FALSE</li>
 * </ul>
 */
public interface DelimitedStringJoiner extends Configurable<DelimitedStringJoiner> {

	/**
	 * Set the character which will be used for delimiting values. The default value is comma.
	 */
	public Key<Character> delimiter = new Key<>(() -> ',');
	
	/**
	 * Set the character which will be used for enclosing values. The default value is double quote.
	 */
    public Key<Character> encloser  = new Key<>(() -> '"');
    
    /**
     * Set trimming of values on or off.  The default value is FALSE (no trimming).
     */
    public Key<Boolean> trim = new Key<>(() -> Boolean.FALSE);
    
    /**
     * Make all values always enclosed.  The default value is FALSE (no enclosing when it is not necessary).
     */
    public Key<Boolean> alwaysEnclose = new Key<>(() -> Boolean.FALSE);
    
    /**
     * Replace new lines with spaces, if set. The default value is FALSE (no new line replacements).
     */
    public Key<Boolean> checkForNewLines = new Key<>(() -> Boolean.FALSE);
	
    /**
     * Concatenate (join) given values into one string.
     */
	String join(CharSequence ... values);
	
	/**
	 * Concatenate (join) given values into one string. {@code toString() } will be called on each given object
	 * in order to convert it to a string before concatenation.
	 */
	default String join(Object ... values) {
		return join(Stream.of(values));
	}
	
	/**
	 * Concatenate (join) given values into one string. {@code toString() } will be called on each given object
	 * in order to convert it to a string before concatenation.  The values will be concatenated in the order of
	 * iteration.
	 */
	default String join(Collection<?> values) {
		return join(values.stream());
	}
	
	/**
	 * Concatenate (join) a stream of values into one string. {@code toString() } will be called on each given object
	 * in order to convert it to a string before concatenation.
	 */
	default String join(Stream<?> values) {
		return join(values.map(v -> v != null ? v.toString() : null).toArray(CharSequence[]::new));
	}
	
	/**
	 * Create a new unlocked object with default configuration settings.  This method effectively produces a CSV
	 * joiner with default configuration settings.
	 */
	static DelimitedStringJoiner get() {
	    return new DelimitedStringJoinerImpl();
	}
	
	/**
	 * Create a new, optionally unlocked CSV joiner. 
	 */
	static DelimitedStringJoiner csv(boolean lockConfiguration) {
		DelimitedStringJoiner joiner = get();
	    if (lockConfiguration) joiner.locked();
	    return joiner;
	}

	/**
	 * Create a locked CSV joiner.
	 */
	static DelimitedStringJoiner csv() {
	    return get().locked();
	}
	
	/**
	 * Create a locked CSV joiner with {@code trim} set to TRUE.
	 */
	static DelimitedStringJoiner csvTrimming() {
	    return get().with(trim, Boolean.TRUE).locked();
	}
	
	/**
	 * Create a locked CSV joiner with {@code alwaysEnclose} set to TRUE.
	 */
	static DelimitedStringJoiner csvEnclosing() {
        return get().with(alwaysEnclose, Boolean.TRUE).locked();
    }
	
	/**
	 * Create a locked CSV joiner with {@code trim} set to TRUE and {@code alwaysEnclose} set to TRUE.
	 */
	static DelimitedStringJoiner csvTrimmingAndEnclosing() {
        return get().with(trim, Boolean.TRUE).with(alwaysEnclose, Boolean.TRUE).locked();
    }
	
	/**
	 * Create a new, optionally unlocked pipe ('|') joiner. 
	 */
	static DelimitedStringJoiner pipe(boolean lockConfiguration) {
	    DelimitedStringJoiner joiner = get().with(delimiter, '|');
	    if (lockConfiguration) joiner.locked();
	    return joiner;
	}
	
	/**
	 * Create a locked pipe ('|') joiner. 
	 */
	static DelimitedStringJoiner pipe() {
        return pipe(true);
    }
    
	/**
	 * Create a locked pipe ('|') joiner with {@code trim} set to TRUE. 
	 */
    static DelimitedStringJoiner pipeTrimming() {
        return pipe(false).with(trim, Boolean.TRUE).locked();
    }
    
    /**
	 * Create a locked pipe ('|') joiner with {@code alwaysEnclose} set to TRUE. 
	 */
    static DelimitedStringJoiner pipeEnclosing() {
        return pipe(false).with(alwaysEnclose, Boolean.TRUE).locked();
    }
    
    /**
	 * Create a locked pipe ('|') joiner with {@code trim} set to TRUE and {@code alwaysEnclose} set to TRUE.
	 */
    static DelimitedStringJoiner pipeTrimmingAndEnclosing() {
        return pipe(false).with(trim, Boolean.TRUE).with(alwaysEnclose, Boolean.TRUE).locked();
    }
    
    /**
	 * Create a new, optionally unlocked TAB ('\t') joiner. 
	 */
    static DelimitedStringJoiner tab(boolean lockConfiguration) {
        DelimitedStringJoiner joiner = get().with(delimiter, '\t');
        if (lockConfiguration) joiner.locked();
        return joiner;
    }
    
    /**
	 * Create a locked TAB ('\t') joiner. 
	 */
    static DelimitedStringJoiner tab() {
        return tab(true);
    }
    
    /**
	 * Create a locked TAB ('\t') joiner with {@code trim} set to TRUE. 
	 */
    static DelimitedStringJoiner tabTrimming() {
        return tab(false).with(trim, Boolean.TRUE).locked();
    }
    
    /**
	 * Create a locked TAB ('\t') joiner with {@code alwaysEnclose} set to TRUE. 
	 */
    static DelimitedStringJoiner tabEnclosing() {
        return tab(false).with(alwaysEnclose, Boolean.TRUE).locked();
    }
    
    /**
	 * Create a locked TAB ('\t') joiner with {@code trim} set to TRUE and {@code alwaysEnclose} set to TRUE.
	 */
    static DelimitedStringJoiner tabTrimmingAndEnclosing() {
        return tab(false).with(trim, Boolean.TRUE).with(alwaysEnclose, Boolean.TRUE).locked();
    }
    
    /**
	 * Create a new, optionally unlocked space (' ') joiner. 
	 */
    static DelimitedStringJoiner space(boolean lockConfiguration) {
        DelimitedStringJoiner joiner = get().with(delimiter, ' ');
        if (lockConfiguration) joiner.locked();
        return joiner;
    }
    
    /**
	 * Create a locked space (' ') joiner. 
	 */
    static DelimitedStringJoiner space() {
        return space(true);
    }
    
    /**
	 * Create a locked space (' ') joiner with {@code trim} set to TRUE. 
	 */
    static DelimitedStringJoiner spaceTrimming() {
        return space(false).with(trim, Boolean.TRUE).locked();
    }
    
    /**
	 * Create a locked space (' ') joiner with {@code alwaysEnclose} set to TRUE. 
	 */
    static DelimitedStringJoiner spaceEnclosing() {
        return space(false).with(alwaysEnclose, Boolean.TRUE).locked();
    }
    
    
    /**
	 * Create a locked space (' ') joiner with {@code trim} set to TRUE and {@code alwaysEnclose} set to TRUE.
	 */
    static DelimitedStringJoiner spaceTrimmingAndEnclosing() {
        return space(false).with(trim, Boolean.TRUE).with(alwaysEnclose, Boolean.TRUE).locked();
    }

}
