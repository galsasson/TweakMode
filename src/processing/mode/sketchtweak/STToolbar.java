package processing.mode.sketchtweak;

import processing.app.Base;
import processing.app.Editor;
import processing.mode.java.JavaToolbar;

public class STToolbar extends JavaToolbar {

	static protected final int RUN    = 0;
	static protected final int STOP   = 1;

	static protected final int NEW    = 2;
	static protected final int OPEN   = 3;
	static protected final int SAVE   = 4;
	static protected final int EXPORT = 5;

	public STToolbar(Editor editor, Base base) {
		super(editor, base);
	}
}
