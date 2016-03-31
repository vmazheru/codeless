package cl.ugly.util;

import java.util.ArrayList;
import java.util.List;

public final class StringUtils {
	
	private StringUtils(){}
	
	/**
	 * Convert strings like ThisIsALongVariableName to "This Is A Long Variable Name", that is
	 * break Pascal or Camel case strings into words separated by spaces
	 */
	public static String normilizeCamelOrPascalCase(String s) {
		if(s == null) return null;
		char[] chars = s.toCharArray();
		List<Character> result = new ArrayList<>();
		List<Character> uppers = new ArrayList<>();
		for(int i = 0; i < chars.length; i++) {
			char ch = chars[i];
			boolean isUpper = Character.isUpperCase(ch);
			if(isUpper) {
				uppers.add(ch);
			} else {
				int uppersSize = uppers.size();
				if(uppersSize > 0) {
					if(!result.isEmpty() && !(result.get(result.size()-1) == ' ')) {
						result.add(' ');
					}
					for(int j = 0; j < uppersSize; j++) {
						if(j == uppersSize - 1) {
							if(!result.isEmpty() && !(result.get(result.size()-1) == ' ')) {
								result.add(' ');
							}
						}
						result.add(uppers.get(j));
					}
					uppers.clear();
				}
				
				result.add(i != 0 ? ch : Character.toUpperCase(ch));
			}
		}
		
		if(!uppers.isEmpty()) {
			for(Character ch : uppers) {
				result.add(ch);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for(Character c : result) sb.append(c);
		return sb.toString().trim();
	}
	
	/**
	 * For a delimited string, get its field by index without using String.split()
	 * The value index starts with 0
	 */
	public static String getValueFromDelimitedString(String s, int index, char delimiter) {
		if(s == null || s.isEmpty() || index < 0) return null;
		
		int from = -1;
		int i = 0;
		while(i++ < index) {
			from = s.indexOf(delimiter, from+1);
			if(from == -1) return null; // index is to high, we're beyond string end
		}
		int to = s.indexOf(delimiter, from+1);
		if(to == -1) to = s.length(); //for the last value there is no ending delimiter
		return s.substring(from+1, to);
	}
	
}
