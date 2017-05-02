package cl.serializers.delimited;

import java.util.Optional;
import java.util.regex.Pattern;

import cl.core.configurable.ConfigurableObject;

class DelimitedStringJoinerImpl extends ConfigurableObject<DelimitedStringJoiner> implements DelimitedStringJoiner {

	// will be not empty when new lines removal is required
    private Optional<Pattern> newLinePattern;
    
	@Override
	public String join(CharSequence ... values) {
		requireLock();
	    
		// get configuration settings
		char enc = get(encloser);
		char delim = get(delimiter);
		boolean isAlwaysEnclose = get(alwaysEnclose);
		boolean isTrim = get(trim);
		
		// set this flag for a case when the delimiter is a whitespace (for example, tab or space)
		boolean isDelimWhiteSpace = Character.isWhitespace(delim);
		
		StringBuilder result = new StringBuilder();
		
		for (int i = 0; i < values.length; i++) {
		    CharSequence input = values[i];
		    boolean isNotLast = i != values.length - 1;

		    // null values will become empty strings: append the delimiter and continue with the next value
		    if (input == null) {
		        if (isNotLast) result.append(delim);
		        continue;
		    }
		    
		    // this will contain characters for this value
			StringBuilder output = new StringBuilder();
			
			// keep track of the reason on while the value has to be enclosed
			boolean encloseBecauseOfEnc   = false;  // because there is an encloser character in the value
			boolean encloseBecauseOfDelim = false;  // because there is a delimiter in the value
			boolean encloseBecauseOfLeadingTrailingSpaces = false;  // because of leading/trailing spaces

			// for each character in the input value...
			int[] chars = input.chars().toArray();
			for (int j = 0; j < chars.length; j++) {
				char ch = (char)chars[j];
				// if it is an encloser, make sure the value is enclosed in the end, by setting a boolean flag,
				// and also escape the encloser by outputting it twice
                if (ch == enc) {
                    encloseBecauseOfEnc = true;
                    output.append(enc).append(enc);
                // if it is a delimiter, make suer the value is enclosed in the end, and just append it
                } else if (ch == delim) {
                    encloseBecauseOfDelim = true;
                    output.append(ch);
                // else this is a "normal" character, so append it
                } else {
                    output.append(ch);
                }
			}
			
			// handle 'trim' configuration setting
			final CharSequence outputStr;
			if (isTrim) {
				// if trimming feature is set, trim the value
			    String outputS = output.toString().trim();
			    
			    // in case when the delimiter is a whitespace,
			    // trimming may remove the reason for which we have decided to enclose the value earlier.
			    // let's check if there are any delimiters in the trimmed value and reset the flag
			    // Avoid calling indexOf() if isAlwaysEnclose is set to true, for we're going to enclose anyways
			    if (isDelimWhiteSpace) {
			        encloseBecauseOfDelim = isAlwaysEnclose || outputS.indexOf(delim) != -1;
			    }
			    outputStr = outputS;
			} else {
				// if no trimming is requested, check if there are leading/trailing white spaces in the value,
				// and if yes, enclose the value.
				// Avoid calling isWhitespace() if isAlwaysEnclose is set to true, for we're going to enclose anyways
			    encloseBecauseOfLeadingTrailingSpaces = isAlwaysEnclose || 
			    										Character.isWhitespace(output.charAt(0)) ||
			                                            Character.isWhitespace(output.charAt(output.length()-1));
			    outputStr = output;
			}
			
			// do we need to enclose the value?
			boolean enclose = isAlwaysEnclose ||
			                  encloseBecauseOfDelim ||
			                  encloseBecauseOfEnc ||
			                  encloseBecauseOfLeadingTrailingSpaces;
			
			// append the value to the final string
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
		
		// handle checkForNewLines configuration setting: replace new lines with spaces in the entire final string
		return newLinePattern
		        .map(p -> p.matcher(result).replaceAll(" "))
		        .orElse(result.toString());
	}

	@Override
	protected void build() {
        newLinePattern = get(checkForNewLines) ? Optional.of(Pattern.compile("\\R")) : Optional.empty();
    }

}
