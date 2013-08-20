package processing.mode.tweak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import processing.app.Sketch;

public class SketchParser 
{
	int intVarCount;
	int floatVarCount;
	final String varPrefix = "tweakmode";
	
	String[] codeTabs;
	boolean requiresComment;
	ArrayList<Handle> allHandles;
	ArrayList<ColorMode> colorModes;
	ArrayList<ColorControlBox> colorBoxes;
	
	public SketchParser(String[] codeTabs, boolean requiresComment)
	{
		this.codeTabs = codeTabs;
		this.requiresComment = requiresComment;
		intVarCount=0;
		floatVarCount=0;
		allHandles = new ArrayList<Handle>();
		
		allHandles.addAll(findAllNumbers());
		allHandles.addAll(findAllHexNumbers());
		allHandles.addAll(findAllWebColorNumbers());
		Collections.sort(allHandles, new HandleComparator());
		
		// handle colors
		colorModes = findAllColorModes();
		colorBoxes = createColorBoxes();
		
		/* If there is more than one color mode in this sketch,
		 * allow only hex and webcolors.
		 * Current there is no notion of order of execution so we
		 * cannot know which color mode relate to a color.
		 */
		if (colorModes.size() > 1) {
			ArrayList<ColorControlBox> toDelete = new ArrayList<ColorControlBox>();
			for (ColorControlBox ccb : colorBoxes)
			{
				if (!ccb.isHex) {
					toDelete.add(ccb);
				}
			}
			colorBoxes.removeAll(toDelete);
		}
	}
	
	/**
	 * Get a list of all the numbers in this sketch
	 * @return
	 * list of all numbers in the sketch (excluding hexadecimals)
	 */
	private ArrayList<Handle> findAllNumbers()
	{
		ArrayList<Handle> numbers = new ArrayList<Handle>();

		/* for every number found:
		 * save its type (int/float), name, value and position in code.
		 */
		for (int i=0; i<codeTabs.length; i++)
		{
			String c = codeTabs[i];
			Pattern p = Pattern.compile("[\\[\\{<>(),\\s\\+\\-\\/\\*^%!|&=?:~]\\d+\\.?\\d*");
			Matcher m = p.matcher(c);
        
			while (m.find())
			{
				int start = m.start()+1;
				int end = m.end();
				
				if (isInComment(start, codeTabs[i])) {
					// ignore comments
					continue;
				}

				if (requiresComment) {
					// only add numbers that have the "// tweak" comment in their line
					if (!lineHasTweakComment(start, c)) {
						continue;
					}
				}

				// remove any 'f' after the number
				if (c.charAt(end) == 'f') {
					end++;
				}
				
				// if its a negative, include the '-' sign
				if (c.charAt(start-1) == '-') {
					if (isNegativeSign(start-2, c)) {
						start--;
					}
				}

				// special case for ignoring (0x...). will be handled later
				if (c.charAt(m.end()) == 'x' ||
						c.charAt(m.end()) == 'X') {
					continue;
				}

				// special case for ignoring number inside a string ("")
				if (isInsideString(start, c))
					continue;

				// beware of the global assignment (bug from 26.07.2013)
				if (isGlobal(m.start(), c))
					continue;

				int line = countLines(c.substring(0, start)) - 1;			// zero based
				String value = c.substring(start, end);
				//value
    			if (value.contains(".")) {
    				// consider this as a float
        			String name = varPrefix + "_float[" + floatVarCount +"]";
        			int decimalDigits = getNumDigitsAfterPoint(value);
        			numbers.add(new Handle("float", name, floatVarCount, value, i, line, start, end, decimalDigits));
    				floatVarCount++;
    			} else {
    				// consider this as an int
        			String name = varPrefix + "_int[" + intVarCount +"]";
        			numbers.add(new Handle("int", name, intVarCount, value, i, line, start, end, 0));
    				intVarCount++;
    			}    			
    		}
    	}

    	return numbers;
    }
	
	/**
	 * Get a list of all the hexadecimal numbers in the code
	 * @return
	 * list of all hexadecimal numbers in the sketch
	 */
	private ArrayList<Handle> findAllHexNumbers()
	{
		ArrayList<Handle> numbers = new ArrayList<Handle>();

		/* for every number found:
		 * save its type (int/float), name, value and position in code.
		 */
		for (int i=0; i<codeTabs.length; i++)
		{
			String c = codeTabs[i];
			Pattern p = Pattern.compile("[\\[\\{<>(),\\s\\+\\-\\/\\*^%!|&=?:~]0x[A-Fa-f0-9]+");
			Matcher m = p.matcher(c);
        
			while (m.find())
			{
				int start = m.start()+1;
				int end = m.end();

				if (isInComment(start, codeTabs[i])) {
					// ignore comments
					continue;
				}

				if (requiresComment) {
					// only add numbers that have the "// tweak" comment in their line
					if (!lineHasTweakComment(start, c)) {
						continue;
					}
				}

				// special case for ignoring number inside a string ("")
				if (isInsideString(start, c)) {
					continue;
				}

				// beware of the global assignment (bug from 26.07.2013)
				if (isGlobal(m.start(), c)) {
					continue;
				}

				int line = countLines(c.substring(0, start)) - 1;			// zero based
				String value = c.substring(start, end);
				String name = varPrefix + "_int[" + intVarCount + "]";
				Handle handle;
				try {
					handle = new Handle("hex", name, intVarCount, value, i, line, start, end, 0);
				}
				catch (NumberFormatException e) {
					// don't add this number
					continue;
				}
				numbers.add(handle);
				intVarCount++;
    		}
    	}

    	return numbers;
    }

	/**
	 * Get a list of all the webcolors (#) numbers in the code
	 * @return
	 * list of all hexadecimal numbers in the sketch
	 */
	private ArrayList<Handle> findAllWebColorNumbers()
	{
		ArrayList<Handle> numbers = new ArrayList<Handle>();

		for (int i=0; i<codeTabs.length; i++)
		{
			String c = codeTabs[i];
			Pattern p = Pattern.compile("#[A-Fa-f0-9]{6}");
			Matcher m = p.matcher(c);
        
			while (m.find())
			{
				int start = m.start();
				int end = m.end();

				if (isInComment(start, codeTabs[i])) {
					// ignore comments
					continue;
				}

				if (requiresComment) {
					// only add numbers that have the "// tweak" comment in their line
					if (!lineHasTweakComment(start, c)) {
						continue;
					}
				}

				// special case for ignoring number inside a string ("")
				if (isInsideString(start, c)) {
					continue;
				}

				// beware of the global assignment (bug from 26.07.2013)
				if (isGlobal(m.start(), c)) {
					continue;
				}

				int line = countLines(c.substring(0, start)) - 1;			// zero based
				String value = c.substring(start, end);
				String name = varPrefix + "_int[" + intVarCount + "]";
				Handle handle;
				try {
					handle = new Handle("webcolor", name, intVarCount, value, i, line, start, end, 0);
				}
				catch (NumberFormatException e) {
					// don't add this number
					continue;
				}
				numbers.add(handle);
				intVarCount++;
    		}
    	}

    	return numbers;
    }

	private ArrayList<ColorMode> findAllColorModes()
	{
		ArrayList<ColorMode> modes = new ArrayList<ColorMode>();
		
		for (String tab : codeTabs)
		{
			int index = -1;
			// search for a call to colorMode function
			while ((index = tab.indexOf("colorMode", index+1)) > -1) {
				// found colorMode at index
				
				if (isInComment(index, tab)) {
					// ignore comments
					continue;
				}

				index += 9;
				int parOpen = tab.indexOf('(', index);
				if (parOpen < 0) {
					continue;
				}
				
				int parClose = tab.indexOf(')', parOpen+1);
				if (parClose < 0) {
					continue;
				}
				
				// try to parse this mode
				String modeDesc = tab.substring(parOpen+1, parClose);
				ColorMode newMode;
				try {
					newMode = ColorMode.fromString(modeDesc);
				}
				catch (Exception e) {
					// failed to parse the mode, don't add
					continue;
				}
				modes.add(newMode);
			}
		}
		
		if (modes.size() == 0) {
			// create the default mode
			modes.add(new ColorMode());
		}
		
		return modes;
	}
	
	private ArrayList<ColorControlBox> createColorBoxes()
	{
		ArrayList<ColorControlBox> ccbs = new ArrayList<ColorControlBox>();
		
		for (int i=0; i<codeTabs.length; i++)
		{
			String tab = codeTabs[i];
			// search tab for the functions: 'color', 'fill', 'stroke', 'background', 'tint'
			Pattern p = Pattern.compile("color\\(|color\\s\\(|fill[\\(\\s]|stroke[\\(\\s]|background[\\(\\s]|tint[\\(\\s]");
			Matcher m = p.matcher(tab);
			
			while (m.find())
			{
				ArrayList<Handle> colorHandles = new ArrayList<Handle>();
				
				// look for the '(' and ')' positions
				int openPar = tab.indexOf("(", m.start());
				int closePar = tab.indexOf(")", m.end());
				if (openPar < 0 || closePar < 0) {
					// ignore this color
					continue;
				}
				
				if (isInComment(m.start(), tab)) {
					// ignore colors in a comment
					continue;
				}

				System.out.println("found color use at " + openPar + " - " + closePar + "('" + tab.substring(openPar, closePar) + "')");
				
				// look for handles inside the parenthesis
				for (Handle handle : allHandles)
				{
					if (handle.tabIndex == i &&
							handle.startChar > openPar &&
							handle.endChar <= closePar) {
						// we have a match
						System.out.println("handle match: " + handle.newValue);
						colorHandles.add(handle);
					}
				}
				
				if (colorHandles.size() > 0) {
					/* make sure there is no other stuff between '()' like variables.
					 * substract all handle values from string inside parenthesis and
					 * check there is no garbage left
					 */
					String insidePar = tab.substring(openPar+1, closePar);
					for (Handle h : colorHandles) {
						insidePar = insidePar.replace(h.strValue, "");
					}
					System.out.println("after removing all values we have: '" + insidePar + "'");
					// make sure there is only ' ' and ',' left in the string.
					boolean garbage = false;
					for (int j=0; j<insidePar.length(); j++) {
						if (insidePar.charAt(j) != ' ' && insidePar.charAt(j) != ',') {
							// don't add this color box because we can not know the
							// real value of this color
							garbage = true;
						}
					}
					
					// create a new color box
					if (!garbage) {
						ccbs.add(new ColorControlBox(colorModes.get(0), colorHandles));
					}
				}
			}
		}
		
		return ccbs;
	}
	
	public static boolean containsTweakComment(String[] codeTabs)
	{
		for (String tab : codeTabs) {
			if (hasTweakComment(tab)) {
				return true;
			}
		}
		
		return false;
		
	}
	
	public static boolean lineHasTweakComment(int pos, String code)
	{
		int lineEnd = getEndOfLine(pos, code);
		if (lineEnd < 0) {
			return false;
		}
		
		String line = code.substring(pos, lineEnd);		
		return hasTweakComment(line);
	}
	
	private static boolean hasTweakComment(String code)
	{
		Pattern p = Pattern.compile("\\/\\/.*tweak", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(code);
		if (m.find()) {
			return true;
		}
		
		return false;		
	}
	
	
	private boolean isNegativeSign(int pos, String code)
	{
		// go back and look for ,{[(=?+-/*%<>:&|^!~
		for (int i=pos; i>=0; i--)
		{
			char c = code.charAt(i);
			if (c == ' ') {
				continue;
			}
			if (c==',' || c=='{' || c=='[' || c=='(' ||
					c=='=' || c=='?' || c=='+' || c=='-' ||
					c=='/' || c=='*' || c=='%' || c=='<' ||
					c=='>' || c==':' || c=='&' || c=='|' ||
					c=='^' || c=='!' || c=='~') {
				return true;
			}
			else {
				return false;
			}
		}

		return false;
	}
	
	private int getNumDigitsAfterPoint(String number)
	{
		String tmp[] = number.split("\\.");

		if (tmp.length < 2) {
			return 0;
		}

		return tmp[1].length();
	}

	private int countLines(String str)
	{
		String[] lines = str.split("\r\n|\n\r|\n|\r");
		return lines.length;
	}

	/**
	* Are we inside a string? (TODO: ignore comments in the code)
	* @param pos
	* position in the code
	* @param code
	* the code
	* @return
	*/
	private boolean isInsideString(int pos, String code)
	{
		int quoteNum = 0;	// count '"'

		for (int c = pos; c>=0 && code.charAt(c) != '\n'; c--)
		{
			if (code.charAt(c) == '"') {
				quoteNum++;
			}
		}

		if (quoteNum%2 == 1) {
			return true;
		}

		return false;
	}

	/**
	* Is this a global position?
	* @param pos position
	* @param code code
	* @return
	* true if the position 'pos' is in global scope in the code 'code'
 	*/
	private boolean isGlobal(int pos, String code)
	{
		int cbOpenNum = 0;	// count '{'
		int cbCloseNum = 0;	// count '}'

		for (int c=pos; c>=0; c--)
		{
			if (code.charAt(c) == '{') {
				cbOpenNum++;
			}
			else if (code.charAt(c) == '}') {
				cbCloseNum++;
			}
		}

		if (cbOpenNum == cbCloseNum) {
			return true;
		}

		return false;
	}
	
	private boolean isInComment(int pos, String code)
	{
		// look for one line comment
		int lineStart = getStartOfLine(pos, code);
		if (lineStart < 0) {
			return false;
		}
		if (code.substring(lineStart, pos).indexOf("//") != -1) {
			return true;
		}
		
		// TODO: look for block comments
		
		return false;
	}
	
	public static int getEndOfLine(int pos, String code)
	{
		return code.indexOf("\n", pos);
	}
	
	public static int getStartOfLine(int pos, String code)
	{
		while (pos >= 0) {
			if (code.charAt(pos) == '\n') {
				return pos+1;
			}
			pos--;
		}
		
		return -1;
	}
	
	private String replaceString(String str, int start, int end, String put)
	{
		return str.substring(0, start) + put + str.substring(end, str.length());
	}
}
