package processing.mode.tweak;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import processing.app.RunnerListener;
import processing.app.Sketch;
import processing.app.Base;
import processing.app.Editor;
import processing.app.EditorState;
import processing.app.Mode;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.mode.java.JavaBuild;
import processing.mode.java.JavaMode;
import processing.mode.java.runner.Runner;

/**
 * Mode for enabling real-time modifications to numbers in the code.
 *
 */
public class TweakMode extends JavaMode {
	TweakEditor editor;
	
	String baseCode[];
	
	public ArrayList<Handle> handles;
	
	public boolean dumpModifiedCode;
	
	final static int SPACE_AMOUNT = 0;
	
	public TweakMode(Base base, File folder) 
	{
		super(base, folder);

		// needed so that core libraries like opengl, etc. are loaded.
		for (Mode m : base.getModeList()) {
			if (m.getClass() == JavaMode.class) {
				JavaMode jMode = (JavaMode) m;
				librariesFolder = jMode.getLibrariesFolder();
				rebuildLibraryList(); 
				break;
			}
		}

		// Fetch examples and reference from java mode
		examplesFolder = Base.getContentFile("modes/java/examples");
		referenceFolder = Base.getContentFile("modes/java/reference");

		dumpModifiedCode = false;
	}

    /**
     * Return the pretty/printable/menu name for this mode. This is separate
     * from the single word name of the folder that contains this mode. It could
     * even have spaces, though that might result in sheer madness or total
     * mayhem.
     */
    @Override
    public String getTitle() {
        return "Tweak";
    }

    /**
     * Create a new editor associated with this mode.
     */
    @Override
    public Editor createEditor(Base base, String path, EditorState state) {
    	editor = new TweakEditor(base, path, state, this);
    	return (Editor)editor;
    }

    /**
     * Returns the default extension for this editor setup.
     */
    /*
    @Override
    public String getDefaultExtension() {
        return null;
    }
    */

    /**
     * Returns a String[] array of proper extensions.
     */
    /*
    @Override
    public String[] getExtensions() {
        return null;
    }
    */

    /**
     * Get array of file/directory names that needn't be copied during "Save
     * As".
     */
    /*
    @Override
    public String[] getIgnorable() {
        return null;
    }
    */
    
    /**
     * Retrieve the ClassLoader for JavaMode. This is used by Compiler to load
     * ECJ classes. Thanks to Ben Fry.
     *
     * @return the class loader from java mode
     */
    @Override
    public ClassLoader getClassLoader() {
        for (Mode m : base.getModeList()) {
            if (m.getClass() == JavaMode.class) {
                JavaMode jMode = (JavaMode) m;
                return jMode.getClassLoader();
            }
        }
        return null;  // badness
    }

	@Override
	public Runner handleRun(Sketch sketch, RunnerListener listener) throws SketchException 
	{
		boolean launchInteractive = false;
		System.out.println("Tweak: run");

		if (isSketchModified(sketch)) {
			editor.deactivateRun();
			Base.showMessage("Save", "Please save the sketch before running in Tweak Mode.");
			return null;	
		}
    	
		initBaseCode(sketch);
		// check for "// tweak" comment in the sketch 
		boolean requiresTweak = sketchHasTweakComment();
    	
		// parse the saved sketch to get all (or only with "//tweak" comment) numbers
		handles = getAllNumbers(sketch, requiresTweak);
    	
		// add our code to the sketch
		launchInteractive = automateSketch(sketch, handles);
		
		JavaBuild build = new JavaBuild(sketch);
		String appletClassName = build.build(false);
		if (appletClassName != null) {
			final Runner runtime = new Runner(build, listener);
			new Thread(new Runnable() {
				public void run() {
					runtime.launch(false);  // this blocks until finished
              
					// executed when the sketch quits
					editor.initEditorCode(baseCode, handles, false);
					editor.stopInteractiveMode(handles);
				}
			}).start();
          
			if (launchInteractive) { 

				// replace editor code with baseCode 
				editor.initEditorCode(baseCode, handles, false);				
				editor.updateInterface(handles);
				editor.startInteractiveMode();
			}

			return runtime;
		}
        
		return null;    	
	}

    /**
     * Replace all numbers with variables and add code to initialize these variables and handle OSC messages.
     * @param sketch
     * 	the sketch to work on
     * @param numbers
     * 	list of numbers to replace in this sketch
     * @return
     *  true on success
     */
    private boolean automateSketch(Sketch sketch, ArrayList<Handle> numbers)
    {
    	SketchCode[] code = sketch.getCode();

    	if (code.length<1)
    		return false;

    	if (numbers.size() == 0)
    		return false;
    	
    	System.out.print("Tweak: instrument code... ");
		
		// Copy current program to interactive program
		
    	/* modify the code below, replace all numbers with their variable names */
    	// loop through all tabs in the current sketch
    	for (int tab=0; tab<code.length; tab++)
    	{
    		int charInc = 0;
			String c = baseCode[tab];
			for (Handle n : numbers)
    		{
    			// handle only numbers that belong to the current tab
				// (put numbers list inside SketchCode?)
    			if (n.tabIndex != tab)
    				continue;
    			
    			// replace number value with a variable
    			c = replaceString(c, n.startChar + charInc, n.endChar + charInc, n.name);
    			charInc += n.name.length() - n.strValue.length();
    		}
			code[tab].setProgram(c);
    	}
    	System.out.println("ok");
    	
    	System.out.print("Tweak: add header... ");
    	
    	/* add the main header to the code in the first tab */
    	String c = code[0].getProgram();
    	
    	// header contains variable declaration, initialization, and OSC listener function
    	String header;
    	header = "\n\n" +
    		 "/*************************/\n" +
    		 "/* MODIFIED BY TWEAKMODE */\n" +
		 	 "/*************************/\n" +
    		 "\n\n";
    	
    	// add needed OSC imports and the global OSC object
    	header += "import oscP5.*;\n";
    	header += "import netP5.*;\n\n";
    	header += "OscP5 tweakmode_oscP5;\n\n";
    	
    	// write a declaration for int and float arrays
    	header += "int[] tweakmode_int = new int["+howManyInts(numbers)+"];\n";
    	header += "float[] tweakmode_float = new float["+howManyFloats(numbers)+"];\n\n";
    	
    	/* add the class for the OSC event handler that will respond to our messages */
    	header += "public class TweakMode_OscHandler {\n" +
    			  "  public void oscEvent(OscMessage msg) {\n" +
                  "    String type = msg.addrPattern();\n" +
                  "    if (type.contains(\"/tm_change_int\")) {\n" +
                  "      int index = msg.get(0).intValue();\n" +
                  "      int value = msg.get(1).intValue();\n" +    
                  "      tweakmode_int[index] = value;\n" +
                  "    }\n" +
                  "    else if (type.contains(\"/tm_change_float\")) {\n" +
                  "      int index = msg.get(0).intValue();\n" +
                  "      float value = msg.get(1).floatValue();\n" +   
                  "      tweakmode_float[index] = value;\n" +
                  "    }\n" +
                  "  }\n" +
                  "}\n";
    	header += "TweakMode_OscHandler tweakmode_oscHandler = new TweakMode_OscHandler();\n";

    	header += "void tweakmode_initAllVars() {\n";
    	for (Handle n : numbers)
    	{
    		header += "  " + n.name + " = " + n.strValue + ";\n";
    	}
    	header += "}\n\n";
    	header += "void tweakmode_initOSC() {\n";
    	header += "  tweakmode_oscP5 = new OscP5(tweakmode_oscHandler,57110);\n";
    	header += "}\n";
    	
    	header += "\n\n\n\n\n";
    	
    	// add call to our initAllVars and initOSC functions from the setup() function.
    	String addToSetup = "\n  tweakmode_initAllVars();\n  tweakmode_initOSC();\n\n";
    	int pos = getSetupStart(c);
    	c = replaceString(c, pos, pos, addToSetup);

    	code[0].setProgram(header + c);
    	
    	System.out.println("ok");

    	/* print out modified code */
    	if (dumpModifiedCode) {
    		System.out.println("\nModified code:\n");
    		for (int i=0; i<code.length; i++)
    		{
    			System.out.println("file " + i + "\n=========");
    			System.out.println(code[i].getProgram());
    		}
    	}
    	

    	return true;
    }

	/**
	 * Get a list of all the numbers in this sketch
	 * @param sketch: the sketch to take the numbers from
	 * @return
	 * ArrayList<Number> of all the numbers
	 */
	public ArrayList<Handle> getAllNumbers(Sketch sketch, boolean requiresComment)
	{
//		SketchCode[] code = sketch.getCode();
		int intVarCount = 0;
		int floatVarCount = 0;

		ArrayList<Handle> numbers = new ArrayList<Handle>();

		/* for every number found:
		 * save its type (int/float), name, value and position in code.
		 */
		String varPrefix = "tweakmode";
		for (int i=0; i<baseCode.length; i++)
		{
			String c = baseCode[i];
			Pattern p = Pattern.compile("[\\[\\{<>(),\\s\\+\\-\\/\\*^%!|&=?:~]\\d+\\.?\\d*");
			Matcher m = p.matcher(c);
        
			while (m.find())
			{
				int start = m.start()+1;
				int end = m.end();
				
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

				// special case for ignoring (0x...)
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
    			String name;
    			if (value.contains(".")) {
    				// consider this as a float
        			name = varPrefix + "_float[" + floatVarCount +"]";
        			int decimalDigits = getNumDigitsAfterPoint(value);
        			numbers.add(new Handle("float", name, floatVarCount, value, i, line, start, end, decimalDigits));
    				floatVarCount++;
    			} else {
    				// consider this as an int
        			name = varPrefix + "_int[" + intVarCount +"]";
        			numbers.add(new Handle("int", name, intVarCount, value, i, line, start, end, 0));
    				intVarCount++;
    			}    			
    		}
    	}

    	return numbers;
    }

	private void initBaseCode(Sketch sketch)
	{
    	SketchCode[] code = sketch.getCode();
    	
    	String space = new String();
    	
    	for (int i=0; i<SPACE_AMOUNT; i++) {
    		space += "\n";
    	}
    	
    	baseCode = new String[code.length];
		for (int i=0; i<code.length; i++)
		{
			baseCode[i] = new String(code[i].getSavedProgram());
			baseCode[i] = space + baseCode[i] + space;
		} 
	}
	
	private boolean lineHasTweakComment(int pos, String code)
	{
		int lineEnd = code.indexOf("\n", pos);
		if (lineEnd < 0) {
			return false;
		}
		
		String line = code.substring(pos, lineEnd);		
		return hasTweakComment(line);
	}
	
	public boolean sketchHasTweakComment()
	{
		for (String code : baseCode) {
			if (hasTweakComment(code)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean hasTweakComment(String code)
	{
		Pattern p = Pattern.compile("\\/\\/.*tweak", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(code);
		if (m.find()) {
			return true;
		}
		
		return false;		
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
    		if (code.charAt(c) == '"')
    			quoteNum++;
    	}
    	
    	if (quoteNum%2 == 1)
    		return true;
    	
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
    		if (code.charAt(c) == '{')
    			cbOpenNum++;
    		else if (code.charAt(c) == '}')
    			cbCloseNum++;
    	}
    	
    	if (cbOpenNum == cbCloseNum)
    		return true;
    	
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
    
	private String replaceString(String str, int start, int end, String put)
	{
		return str.substring(0, start) + put + str.substring(end, str.length());
	}
	
	private int howManyInts(ArrayList<Handle> numbers)
	{
		int count = 0;
		for (Handle n : numbers) {
			if (n.type == "int")
				count++;
		}
		return count;
	}

	private int howManyFloats(ArrayList<Handle> numbers)
	{
		int count = 0;
		for (Handle n : numbers) {
			if (n.type == "float")
				count++;
		}
		return count;
	}

	private int getSetupStart(String code)
	{
		int pos;
		
		pos = code.indexOf("setup()");
		pos = code.indexOf("{", pos);
		return pos+1;
	}

	private boolean isSketchModified(Sketch sketch)
	{
		for (SketchCode sc : sketch.getCode()) {
			if (sc.isModified()) {
				return true;
			}
		}
		return false;
	}
	
	private int getNumDigitsAfterPoint(String number)
	{
		String tmp[] = number.split("\\.");
		
		if (tmp.length < 2)
			return 0;
		
		return tmp[1].length();
	}

}
