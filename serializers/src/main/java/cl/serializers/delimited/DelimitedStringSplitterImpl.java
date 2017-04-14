package cl.serializers.delimited;

import java.util.ArrayList;
import java.util.List;

import cl.core.configurable.ConfigurableObject;

/**
 * An implementation of the {@link DelimitedStringSplitter} interfaces.
 */
class DelimitedStringSplitterImpl extends ConfigurableObject<DelimitedStringSplitter> implements DelimitedStringSplitter {

    @Override
    public String[] split(String str) {
    	requireLock();
        if (str == null) return new String[]{};
        
        // extract parameters
        char enc = get(encloser);
        char delim = get(delimiter);
        boolean isTrim = get(trim);
        boolean isTrimEnclosed = get(trimEnclosed);
        boolean isAlwaysEnclosed = get(alwaysEnclosed);
        int numElems = get(numValues).orElse(10);
        
        // trim the entire string if requested
        String s = isTrim ? str.trim() : str;
        
        char[] chars = s.toCharArray();
        int last = chars.length - 1;
        List<String> values = new ArrayList<>(numElems);
        StringBuilder value = new StringBuilder();
        boolean inEnclosed = false;
        boolean wasEnclosed = false;
        
        for(int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            
            if (ch == delim) {
            	// We encountered a delimiter. 
            	// If we are in the enclosed section, then the delimiter should be treated
            	// as a regular character, so we append it.
            	// If we are not in the enclosed section, than we are done with a delimited value, 
            	// so we optionally trim it and append the value to the list, and allocate a new value buffer.
                if (inEnclosed) {
                    value.append(ch);
                } else {
                    values.add(trimIfRequired(value.toString(), enc, wasEnclosed, isTrim, isTrimEnclosed));
                    wasEnclosed = false;
                    value = new StringBuilder();
                }
            } else if (ch == enc) {
            	// We encountered an encloser.  The following may happen here:
            	
            	// 1) The encloser came either at the beginning of the entire string or after a delimiter. That means
            	// we are at the beginning of the value, which may or may not be enclosed.
            	// We need to figure out weather this is enclosed value, or the encloser is just a character, which
            	// needs to be put in the result.  We assume, that if the value ends with encloser, too, than this
            	// character is an encloser.  For that, we scan forward, until we reach the next delimiter.
            	
            	// 2) The encloser came right before the delimiter or it is the last in the string AND the value is enclosed.
            	// We do not do anything, since this is the closing encloser, we just reset the control variables.
            	
            	// 3) In any other case, we would want to pass the encloser to output.  We also need to "escape"
            	// doubled enclosers. 
                if (i == 0 || chars[i-1] == delim) {
                    if (i != last) {
                    	if (isAlwaysEnclosed) {
                    		inEnclosed = true;
                    	} else {
	                        for (int j = i+1; j < chars.length; j++) {
	                            if (chars[j] == delim && chars[j-1] == enc || j == last && chars[j] == enc) {
	                                inEnclosed = true;
	                                break;
	                            }
	                        }
                    	}
                    } else {
                    	// this is a rare case when the last character in the input is an encloser
                    	// and this encloser is the only character in the value
                    	if (!inEnclosed) value.append(ch);
                    }
                } else if (inEnclosed && (i == last || chars[i+1] == delim)) {
                    inEnclosed = false;
                    wasEnclosed = true;
                } else {
					if (i != last) {
						// escape doubled enclosers
						int numEnclosers = 1;
						
						for (i = i + 1; i < last; i++) {
							if (chars[i] == enc) {
								numEnclosers++;
							} else {
								break;
							}
						}
						i--;

						int numEnclosersEscaped = numEnclosers / 2 + numEnclosers % 2;
						for (int j = 0; j < numEnclosersEscaped; j++) {
							value.append(enc);
						}
					} else {
						value.append(ch);
					}
                }
            } else {
                value.append(ch);
            }
        }

        values.add(trimIfRequired(value.toString(), enc, wasEnclosed, isTrim, isTrimEnclosed));
        
        return values.toArray(new String[values.size()]);
    }
    
    private static String trimIfRequired(String s, char enc, boolean enclosed, boolean trim, boolean trimEnclosed) {
    	String finalValue = s;
    	if (enclosed && trimEnclosed) {
    		finalValue = s.trim();
    	} else if (!enclosed && trim) {
    		finalValue = s.trim();
    	}
    	
    	if (trimEnclosed) {
    		int last = finalValue.length() - 1;
    		if (last >= 0) {
	    		boolean inEncloser = finalValue.charAt(0) == enc && finalValue.charAt(last) == enc;
	    		if (inEncloser) {
	    			finalValue = new StringBuilder()
	            			.append(enc)
	            			.append(finalValue.substring(1, last).trim())
	            			.append(enc)
	            			.toString();
	    		}
    		}
    	}
    	
    	return finalValue;
    }
 

}
