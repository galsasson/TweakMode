package processing.mode.tweak;

public class ColorMode {
	final static int RGB = 0;
	final static int HSB = 1;
	
	float v1Max, v2Max, v3Max, aMax;
	int modeType;
	
	String drawContext;
	
	public ColorMode(String context)
	{
		this.drawContext = context;
		modeType = RGB;
		v1Max = 255;
		v2Max = 255;
		v3Max = 255;
		aMax = 255;
	}
	
	public ColorMode(String context, int type, float v1, float v2, float v3, float a)
	{
		this.drawContext = context;
		modeType = type;
		v1Max = v1;
		v2Max = v2;
		v3Max = v3;
		aMax = a;
	}
	
	public static ColorMode fromString(String context, String mode)
	{
		String[] elements = mode.split(",");
		
		// determine the type of the color mode
		int type = RGB;
		if (elements[0].trim().equals("HSB")) {
			type = HSB;
		}
		
		if (elements.length == 1) {
			// colorMode in the form of colorMode(type)
			return new ColorMode(context, type, 255, 255, 255, 255);
		}
		else if (elements.length == 2) {
			// colorMode in the form of colorMode(type, max)
			float max = Float.parseFloat(elements[1].trim());
			return new ColorMode(context, type, max, max, max, max);
		}
		else if (elements.length == 4) {
			// colorMode in the form of colorMode(type, max1, max2, max3)
			float r = Float.parseFloat(elements[1].trim());
			float g = Float.parseFloat(elements[2].trim());
			float b = Float.parseFloat(elements[3].trim());
			return new ColorMode(context, type, r, g, b, 255);
		}
		else if (elements.length == 5) {
			// colorMode in the form of colorMode(type, max1, max2, max3, maxA)
			float r = Float.parseFloat(elements[1].trim());
			float g = Float.parseFloat(elements[2].trim());
			float b = Float.parseFloat(elements[3].trim());
			float a = Float.parseFloat(elements[4].trim());
			return new ColorMode(context, type, r, g, b, a);			
		}

		// failed to parse the given string
		return null;
	}
	
	public String toString()
	{
		String type;
		if (modeType == RGB) {
			type = "RGB";
		}
		else {
			type = "HSB";
		}
		
		return "ColorMode: " + type + ": (" + v1Max + ", " + v2Max + ", " + v3Max + ", " + aMax + ")";
	}
}
