package processing.mode.tweak;

import java.awt.Color;

public class ColorScheme {
	private static ColorScheme instance = null;
	public Color redStrokeColor;
	public Color grayPaneColor;
	public Color markerColor;
	
	private ColorScheme()
	{
		redStrokeColor = new Color(160, 20, 20);	// dark red
		grayPaneColor = new Color(0, 0, 0, 180);
		markerColor = new Color(228, 240, 91, 127);
	}
	
	public static ColorScheme getInstance() {
		if (instance == null) {
			instance = new ColorScheme();
		}
		return instance;
	}

}


