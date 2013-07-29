package processing.mode.sketchtweak;

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
public class SketchTweakMode extends JavaMode {
	STEditor editor;
	
    public SketchTweakMode(Base base, File folder) {
        super(base, folder);
    }

    /**
     * Return the pretty/printable/menu name for this mode. This is separate
     * from the single word name of the folder that contains this mode. It could
     * even have spaces, though that might result in sheer madness or total
     * mayhem.
     */
    @Override
    public String getTitle() {
        return "Sketch Tweak";
    }

    /**
     * Create a new editor associated with this mode.
     */
    @Override
    public Editor createEditor(Base base, String path, EditorState state) {
    	editor = new STEditor(base, path, state, this);
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
    public Runner handleRun(Sketch sketch, RunnerListener listener) throws SketchException {
    	boolean launchInteractive;
    	System.out.println("SketchTweak: run");
    	
    	if (sketch.isModified()) {
    		editor.deactivateRun();
    		Base.showMessage("Save", "Please save the sketch before running.");
    		return null;	
    	}
    	
    	/* parse the saved sketch to get all numbers */
    	ArrayList<Number> numbers = getAllNumbers(sketch);
    	
    	/* add our code to the sketch */
    	launchInteractive = automateSketch(sketch, numbers);
    	
        JavaBuild build = new JavaBuild(sketch);
        String appletClassName = build.build(false);
        if (appletClassName != null) {
          final Runner runtime = new Runner(build, listener);
          new Thread(new Runnable() {
            public void run() {
              runtime.launch(false);  // this blocks until finished
              
              // executed when the sketch quits
              editor.stopInteractiveMode();
            }
          }).start();
          
          if (launchInteractive)
          {
        	  // annoying bug: editor shows the modified code
        	  revertSketch(sketch);
          
        	  editor.updateInterface(numbers);
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
    private boolean automateSketch(Sketch sketch, ArrayList<Number> numbers)
    {
    	SketchCode[] code = sketch.getCode();

    	if (code.length<1)
    		return false;

    	if (numbers.size() == 0)
    		return false;
    	
    	System.out.print("SketchTweak: instrument code... ");

    	/* modify the code below, replace all numbers with their variable names */
    	// loop through all tabs in the current sketch
    	for (int tab=0; tab<code.length; tab++)
    	{
    		int charInc = 0;
			String c = code[tab].getSavedProgram();
			for (Number n : numbers)
    		{
    			// handle only numbers that belong to the current tab
				// (put numbers list inside SketchCode?)
    			if (n.tabIndex != tab)
    				continue;
    			
    			// replace number value with a variable
    			c = replaceString(c, n.startChar + charInc, n.endChar + charInc, n.name);
    			charInc += n.name.length() - n.value.length();
    		}
			code[tab].setProgram(c);
    	}
    	System.out.println("ok");
    	
    	System.out.print("SketchTweak: add header... ");
    	
    	/* add the main header to the code in the first tab */
    	String c = code[0].getProgram();
    	
    	// header contains variable declaration, initialization, and OSC listener function
    	String header;
    	header = "\n" +
    		 "/***************************/\n" +
    		 "/* MODIFIED BY NUMBERSENSE */\n" +
		 	 "/***************************/\n" +
    		 "\n\n";
    	
    	// add needed OSC imports and the global OSC object
    	header += "import oscP5.*;\n";
    	header += "import netP5.*;\n\n";
    	header += "OscP5 oscP5;\n\n";
    	
    	// write a declaration for int and float arrays
    	header += "int[] numbersense_int = new int["+howManyInts(numbers)+"];\n";
    	header += "float[] numbersense_float = new float["+howManyFloats(numbers)+"];\n\n";
    	header += "void numbersense_initAllVars() {\n";
    	for (Number n : numbers)
    	{
    		header += "  " + n.name + " = " + n.value + ";\n";
    	}
    	header += "}\n\n";
    	header += "void numbersense_initOSC() {\n";
    	header += "  oscP5 = new OscP5(this,57110);\n";
    	header += "}\n";
    	
    	header += "\n\n\n\n\n";
    	
    	/* add the OSC event handler that will respond to our messages */
        header += "void oscEvent(OscMessage msg) {\n" +
                  "  String type = msg.addrPattern();\n" +
                  "  if (type.contains(\"/ns_change_int\")) {\n" +
                  "    int index = msg.get(0).intValue();\n" +
                  "    int value = msg.get(1).intValue();\n" +    
                  "    numbersense_int[index] = value;\n" +
                  "  }\n" +
                  "  else if (type.contains(\"/ns_change_float\")) {\n" +
                  "    int index = msg.get(0).intValue();\n" +
                  "    float value = msg.get(1).floatValue();\n" +   
                  "    numbersense_float[index] = value;\n" +
                  "  }\n" +
                  "}\n";
    	
    	// add call to our initAllVars and initOSC functions from the setup() function.
    	String addToSetup = "\n  numbersense_initAllVars();\n  numbersense_initOSC();\n\n";
    	int pos = getSetupStart(c);
    	c = replaceString(c, pos, pos, addToSetup);    	

    	code[0].setProgram(header + c);
    	
    	System.out.println("ok");

    	/* print out modified code */    	
//    	System.out.println("Modified code:");
//    	for (int i=0; i<code.length; i++)
//    	{
//    		System.out.println("file " + i + "\n=======");
//    		System.out.println(code[i].getProgram());
//    	}
    	

    	return true;
    }

	/**
	* After compiling the modified sketch, bring it back the original code to show in PDE
	*/
    public void revertSketch(Sketch sketch)
    {
    	SketchCode[] code = sketch.getCode();
    	
    	for (SketchCode c : code)
    	{
    		c.setProgram(c.getSavedProgram());
    	}
    }

    /**
     * Get a list of all the numbers in this sketch
     * @param sketch: the sketch to take the numbers from
     * @return
     * ArrayList<Number> of all the numbers
     */
    public ArrayList<Number> getAllNumbers(Sketch sketch)
    {
    	SketchCode[] code = sketch.getCode();
    	int intVarCount = 0;
    	int floatVarCount = 0;

    	ArrayList<Number> numbers = new ArrayList<Number>();

    	/* for every number found:
    	 * save its type (int/float), name, value and position in code.
    	 */
    	String varPrefix = "numbersense";
    	for (int i=0; i<code.length; i++)
    	{
    		String c = new String(code[i].getSavedProgram());
    		Pattern p = Pattern.compile("[\\[\\{<>(),\\s\\+\\-\\/\\*^%!|&=]\\d+\\.?\\d*");
    		Matcher m = p.matcher(c);
        
    		while (m.find())
    		{
    			// special case for ignoring (0x...)
    			if (c.charAt(m.end()) == 'x' ||
    				c.charAt(m.end()) == 'X')
    				continue;
    			
    			// special case for ignoring number inside a string ("")
    			if (isInsideString(m.start(), c))
    				continue;
    			
    			// beware of the global assignment (bug from 26.07.2013)
    			if (isGlobal(m.start(), c))
    				continue;
    			
    			int line = countLines(c.substring(0, m.start())) - 1;			// zero based
    			String value = m.group(0).substring(1, m.group(0).length());
    			String name;
    			if (value.contains(".")) {
    				// consider this as a float
        			name = varPrefix + "_float[" + floatVarCount +"]";
        			numbers.add(new Number("float", name, floatVarCount, value, i, line, m.start()+1, m.end()));
    				floatVarCount++;
    			} else {
    				// consider this as an int
        			name = varPrefix + "_int[" + intVarCount +"]";
        			numbers.add(new Number("int", name, intVarCount, value, i, line, m.start()+1, m.end()));
    				intVarCount++;
    			}    			
    		}
    	}

    	return numbers;
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
    
	private String replaceString(String str, int start, int end, String put)
	{
		return str.substring(0, start) + put + str.substring(end, str.length());
	}
	
	private int howManyInts(ArrayList<Number> numbers)
	{
		int count = 0;
		for (Number n : numbers) {
			if (n.type == "int")
				count++;
		}
		return count;
	}
	
	private int howManyFloats(ArrayList<Number> numbers)
	{
		int count = 0;
		for (Number n : numbers) {
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
	
	
}
