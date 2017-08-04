package cl.serializers.delimited;

import java.util.Optional;

import cl.core.configurable.Configurable;
import cl.core.configurable.Key;

/**
 *  Objects of this type can split delimited strings (comma-separated, tab-separated, etc).
 *  <p>
 *  Objects of this type are configurable and the following configuration keys are defined:
 *  <ul>
 *    <li>
 *    	{@code delimiter} set the character which is used to split the string by (for example, comma for CSV). Comma is
 *      the default value.
 *    </li>
 *    <li>
 *    	{@code encloser} set the character which may be used to enclose values. Typically double quote is used for that.
 *      Double quote is the default value.
 *    </li>
 *    <li>
 *    	{@code trim} of type Boolean specifies whether the values should be trimmed. If set alone, it will not trim
 *      values when they are enclosed.  For example, it'll trim value 'two' in {@code 'one,   two, three'} but not in
 *      {@code one, "   two  ", three'}.
 *      The default value is {@code false} (no trimming).
 *    </li>
 *    <li>
 *    	{@code trimEnclosed} of type Boolean specifies whether the values should be trimmed if they are the leading/trailing
 *      spaces appear in the string which is enclosed. For example, it will trim value 'two' in {@code one, "   two  ", three'}.
 *      The default value is {@code false} (no trimming). 
 *    </li>
 *    <li>
 *    	{@code alwaysEnclosed} of type Boolean specifies whether the values in the string are guaranteed to be always inclosed.
 *      If this is set, parsing of such lines may be more efficient.
 *    </li>
 *    <li>
 *      {@code numValues} is an optional integer value, which if known may slightly improve performance. At least, 
 *      it may be some value which is definitely larger than the expected number of values in the string.
 *    </li>
 *  </ul>
 */
public interface DelimitedStringSplitter extends Configurable<DelimitedStringSplitter> {
    
	/**
     * Set the character which is used to split the string by. Comma is the default value.
     */
    public Key<Character> delimiter = new Key<>(() -> ',');
    
    /**
     * Set the character which may be used to enclose values. Double quote is the default value.
     */
    public Key<Character> encloser  = new Key<>(() -> '"');
    
    /**
     * Specify whether to trim values or not. The default value is {@code false}.
     */
    public Key<Boolean> trim = new Key<>(() -> Boolean.FALSE);
    
    /**
     * Specify whether to trim enclosed values or not.  The default value is {@code false}.
     */
    public Key<Boolean> trimEnclosed = new Key<>(() -> Boolean.FALSE);
    
    /**
     * Specify whether the values in the string are guaranteed to be always inclosed.
     */
    public Key<Boolean> alwaysEnclosed = new Key<>(() -> Boolean.FALSE);
    
    /**
     * If exact number of fields is known, it may slightly improve performance.
     */
    public Key<Optional<Integer>> numValues = new Key<>(() -> Optional.empty());
    
    
    /**
     * Split the given string according to the configuration settings.
     */
    String[] split(String s);

    /**
     * This is the most general way of getting a splitter instance.  The configuration for this instance will not
     * be locked, and the client will have to set all the necessary configuration settings and then lock the 
     * configuration.  This method may be used for any "exotic" use cases you may encounter. For typical use cases,
     * use more specialized factory methods defined in this interface.
     * 
     * <p>
     * In essence, this method will return an unlocked CSV splitter.
     */
    static DelimitedStringSplitter get() {
    	return new DelimitedStringSplitterImpl();
    }
    
    /**
     * Get a locked CSV splitter with default configuration settings.
     */
    static DelimitedStringSplitter csv() {
        return get().locked();
    }
    
    /**
     * Get a locked CSV splitter with {@code trim} configuration setting set to {@code true}.
     */
    static DelimitedStringSplitter csvTrimming() {
        return get().with(trim, Boolean.TRUE).locked();
    }

    /**
     * Get a locked CSV splitter with {@code trim} and {@code trimEnclosed} configuration settings set to {@code true}.
     */
    static DelimitedStringSplitter csvTrimmingAll() {
    	return DelimitedStringSplitter.get().with(trim, true).with(trimEnclosed, true).locked();
    }
    
    /**
     * Get a locked pipe splitter.
     */
    static DelimitedStringSplitter pipe() {
        return pipe(true);
    }
    
    /**
     * Get a locked pipe splitter with {@code trim} configuration setting set to {@code true}.
     */
    static DelimitedStringSplitter pipeTrimming() {
        return pipe(false).with(trim, Boolean.TRUE).locked();
    }
    
    /**
     * Get a locked pipe splitter with {@code trim} and {@code trimEnclosed} configuration settings set to {@code true}.
     */
    static DelimitedStringSplitter pipeTrimmingAll() {
    	return DelimitedStringSplitter.pipe(false).with(trim, true).with(trimEnclosed, true).locked();
    }
    
    /**
     * Get an unlocked pipe splitter.
     */
    static DelimitedStringSplitter pipe(boolean lockConfiguration) {
        DelimitedStringSplitter splitter = get().with(delimiter, '|');
        if (lockConfiguration) splitter.locked();
        return splitter;
    }
    
    /**
     * Get a locked TAB splitter.
     */
    static DelimitedStringSplitter tab() {
    	return tab(true);
    }
    
    /**
     * Get a locked TAB splitter with {@code trim} configuration setting set to {@code true}.
     */
    static DelimitedStringSplitter tabTrimming() {
        return tab(false).with(trim, Boolean.TRUE).locked();
    }
    
    /**
     * Get a locked TAB splitter with {@code trim} and {@code trimEnclosed} configuration settings set to {@code true}.
     */
    static DelimitedStringSplitter tabTrimmingAll() {
    	return DelimitedStringSplitter.tab(false).with(trim, true).with(trimEnclosed, true).locked();
    }
    
    /**
     * Get an unlocked TAB splitter.
     */
    static DelimitedStringSplitter tab(boolean lockConfiguration) {
        DelimitedStringSplitter splitter = get().with(delimiter, '\t');
        if (lockConfiguration) splitter.locked();
        return splitter;
    }
    
    /**
     * Get a locked splitter, which will split by spaces.
     */
    static DelimitedStringSplitter space() {
    	return space(true);
    }
    
    /**
     * Get an unlocked splitter, which will split by spaces.
     */
    static DelimitedStringSplitter space(boolean lockConfiguration) {
    	DelimitedStringSplitter splitter = get().with(delimiter, ' ');
        if (lockConfiguration) splitter.locked();
        return splitter;
    }
    
}
