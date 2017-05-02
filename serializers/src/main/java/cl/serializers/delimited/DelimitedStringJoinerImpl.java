package cl.serializers.delimited;

import java.util.Optional;
import java.util.regex.Pattern;

import cl.core.configurable.ConfigurableObject;

public class DelimitedStringJoinerImpl extends ConfigurableObject<DelimitedStringJoiner> implements DelimitedStringJoiner {
    
    private Optional<Pattern> newLinePattern;
    
	@Override
	public String join(CharSequence ... values) {
	    
		char enc = get(encloser);
		char delim = get(delimiter);
		boolean isAlwaysEnclose = get(alwaysEnclose);
		boolean isTrim = get(trim);
		boolean isDelimWhiteSpace = Character.isWhitespace(delim);
		
		StringBuilder result = new StringBuilder();
		
		for (int i = 0; i < values.length; i++) {
		    CharSequence input = values[i];
		    boolean isNotLast = i != values.length - 1;

		    if (input == null) {
		        if (isNotLast) result.append(delim);
		        continue;
		    }
		    
			StringBuilder output = new StringBuilder();
			boolean encloseBecauseOfEnc   = false;
			boolean encloseBecauseOfDelim = false;
			boolean encloseBecauseOfTrim  = false;
			
			int[] chars = input.chars().toArray();
			for (int j = 0; j < chars.length; j++) {
				char ch = (char)chars[j];
                if (ch == enc) {
                    encloseBecauseOfEnc = true;
                    output.append(enc).append(enc);
                } else if (ch == delim) {
                    encloseBecauseOfDelim = true;
                    output.append(ch);
                } else {
                    output.append(ch);
                }
			}
			
			final CharSequence outputStr;
			if (isTrim) {
			    String outputS = output.toString().trim();
			    if (isDelimWhiteSpace && encloseBecauseOfDelim) {
			        encloseBecauseOfDelim = outputS.indexOf(delim) != -1;
			    }
			    outputStr = outputS;
			} else {
			    outputStr = output;
			    encloseBecauseOfTrim |= Character.isWhitespace(outputStr.charAt(0)) ||
			                            Character.isWhitespace(outputStr.charAt(outputStr.length()-1));
			}
			
			boolean enclose = isAlwaysEnclose ||
			        encloseBecauseOfDelim || encloseBecauseOfEnc || encloseBecauseOfTrim;
			
			if (enclose) {
			    result.append(enc);
			}
			result.append(outputStr);
			if (enclose) {
				result.append(enc);
			}
			if (isNotLast) {
			    result.append(delim);
			}
		}
		
		return newLinePattern
		        .map(p -> p.matcher(result).replaceAll(" "))
		        .orElse(result.toString());
	}

	@Override
	protected void build() {
        newLinePattern = get(checkForNewLines) ?
                Optional.of(Pattern.compile("\n|\r\n", Pattern.LITERAL)) : Optional.empty();
    }

}
