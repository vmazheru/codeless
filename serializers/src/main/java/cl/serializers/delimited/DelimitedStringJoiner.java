package cl.serializers.delimited;

import java.util.stream.Stream;

import cl.core.configurable.Configurable;
import cl.core.configurable.Key;

public interface DelimitedStringJoiner extends Configurable<DelimitedStringJoiner> {
		
	public Key<Character> delimiter = new Key<>(() -> ',');
    public Key<Character> encloser  = new Key<>(() -> '"');
    public Key<Boolean> trim = new Key<>(() -> Boolean.FALSE);
    public Key<Boolean> alwaysEnclose = new Key<>(() -> Boolean.FALSE);
    public Key<Boolean> checkForNewLines = new Key<>(() -> Boolean.FALSE);
	
	String join(CharSequence ... values);
	
	default String join(Object ... values) {
		return join(Stream.of(values)
		        .map(v -> v != null ? v.toString() : null)
		        .toArray(CharSequence[]::new));
	}
	
	static DelimitedStringJoiner get() {
	    return new DelimitedStringJoinerImpl();
	}
	
	static DelimitedStringJoiner csv() {
	    return get().locked();
	}
	
	static DelimitedStringJoiner csvTrimming() {
	    return get().with(trim, Boolean.TRUE).locked();
	}
	
	static DelimitedStringJoiner csvEnclosing() {
        return get().with(alwaysEnclose, Boolean.TRUE).locked();
    }
	
	static DelimitedStringJoiner csvTrimmingAndEnclosing() {
        return get().with(trim, Boolean.TRUE).with(alwaysEnclose, Boolean.TRUE).locked();
    }
	
	static DelimitedStringJoiner pipe(boolean lockConfiguration) {
	    DelimitedStringJoiner joiner = get().with(delimiter, '|');
	    if (lockConfiguration) joiner.locked();
	    return joiner;
	}
	
	static DelimitedStringJoiner pipe() {
        return pipe(true);
    }
    
    static DelimitedStringJoiner pipeTrimming() {
        return pipe(false).with(trim, Boolean.TRUE).locked();
    }
    
    static DelimitedStringJoiner pipeEnclosing() {
        return pipe(false).with(alwaysEnclose, Boolean.TRUE).locked();
    }
    
    static DelimitedStringJoiner pipeTrimmingAndEnclosing() {
        return pipe(false).with(trim, Boolean.TRUE).with(alwaysEnclose, Boolean.TRUE).locked();
    }
    
    static DelimitedStringJoiner tab(boolean lockConfiguration) {
        DelimitedStringJoiner joiner = get().with(delimiter, '\t');
        if (lockConfiguration) joiner.locked();
        return joiner;
    }
    
    static DelimitedStringJoiner tab() {
        return tab(true);
    }
    
    static DelimitedStringJoiner tabTrimming() {
        return tab(false).with(trim, Boolean.TRUE).locked();
    }
    
    static DelimitedStringJoiner tabEnclosing() {
        return tab(false).with(alwaysEnclose, Boolean.TRUE).locked();
    }
    
    static DelimitedStringJoiner tabTrimmingAndEnclosing() {
        return tab(false).with(trim, Boolean.TRUE).with(alwaysEnclose, Boolean.TRUE).locked();
    }
    
    static DelimitedStringJoiner space(boolean lockConfiguration) {
        DelimitedStringJoiner joiner = get().with(delimiter, ' ');
        if (lockConfiguration) joiner.locked();
        return joiner;
    }
    
    static DelimitedStringJoiner space() {
        return space(true);
    }
    
    static DelimitedStringJoiner spaceEnclosing() {
        return space(false).with(alwaysEnclose, Boolean.TRUE).locked();
    }
    
    static DelimitedStringJoiner spaceTrimming() {
        return space(false).with(trim, Boolean.TRUE).locked();
    }
    
    static DelimitedStringJoiner spaceTrimmingAndEnclosing() {
        return space(false).with(trim, Boolean.TRUE).with(alwaysEnclose, Boolean.TRUE).locked();
    }

}
