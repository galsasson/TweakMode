package processing.mode.tweak;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class ColorControlBox {
	
	ArrayList<Handle> handles;
	ColorMode colorMode;
	Color color;
	boolean ilegalColor = false;
	
	// interface
	int x, y, width, height;
	
	public ColorControlBox(ColorMode mode, ArrayList<Handle> handles)
	{
		this.colorMode = mode;
		this.handles = handles;
		
		// add this box to the handles so they can update this color on change
		for (Handle h : handles) {
			h.setColorBox(this);
		}
		
		color = getCurrentColor();
	}
	
	public void initInterface(int x, int y, int w, int h)
	{
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
	}
	
	public void setPos(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public void draw(Graphics2D g2d)
	{
		AffineTransform trans = g2d.getTransform();
		g2d.translate(x, y);

		// draw black outline outline
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(1));
		g2d.drawArc(0, 0, width, height, 0, 360);

		// draw current color
		g2d.setColor(color);
		g2d.fillArc(2, 2, width-4, height-4, 0, 360);
		
		if (ilegalColor) {
			g2d.setColor(Color.RED);
			g2d.setStroke(new BasicStroke(2));
			g2d.drawLine(width*3/4, height/4, width/4, height*3/4);
		}
		
		g2d.setTransform(trans);
	}
	
	public Color getCurrentColor()
	{
		try {
			if (handles.size() == 1)
			{
				int value = handles.get(0).newValue.intValue();
				if ((value&0xff000000) != 0) {
					// treat as color(argb)
					int argb = handles.get(0).newValue.intValue();
					return verifiedHexColor(argb);
				}
				else {
					// treat as color(gray)
					float gray = handles.get(0).newValue.floatValue();
					return verifiedGrayColor(gray);
				}
			}
			else if (handles.size() == 2)
			{
				int value = handles.get(0).newValue.intValue();
				if ((value&0xff000000) != 0) {
					// treat as color(argb, a)
					int argb = handles.get(0).newValue.intValue();
					float a = handles.get(1).newValue.floatValue();
					return verifiedHexColor(argb, a);
				}
				else {
					// color(gray, alpha)
					float gray = handles.get(0).newValue.floatValue();
					return verifiedGrayColor(gray);
				}
			}
			else if (handles.size() == 3)
			{
				// color(v1, v2, v3)
				float v1 = handles.get(0).newValue.floatValue();
				float v2 = handles.get(1).newValue.floatValue();
				float v3 = handles.get(2).newValue.floatValue();

				if (colorMode.modeType == ColorMode.RGB) {
					return verifiedRGBColor(v1, v2, v3, colorMode.aMax);
				}
				else {
					return verifiedHSBColor(v1, v2, v3, colorMode.aMax);
				}
			}
			else if (handles.size() == 4)
			{
				// color(v1, v2, v3, alpha)
				float v1 = handles.get(0).newValue.floatValue();
				float v2 = handles.get(1).newValue.floatValue();
				float v3 = handles.get(2).newValue.floatValue();
				float a = handles.get(3).newValue.floatValue();

				if (colorMode.modeType == ColorMode.RGB) {
					return verifiedRGBColor(v1, v2, v3, a);
				}
				else {
					return verifiedHSBColor(v1, v2, v3, a);
				}
			}
		}
		catch (Exception e) {
			System.out.println("error parsing color value: " + e.toString());
			ilegalColor = true; 
			return Color.WHITE;
		}
		
		// couldn't figure out this color, return WHITE color
		ilegalColor = true;
		return Color.WHITE;
	}
	
	private Color verifiedGrayColor(float gray)
	{
		if (gray < 0 || gray > colorMode.v1Max) {
			return colorError();
		}
		
		ilegalColor = false;
		gray = gray/colorMode.v1Max * 255;
		return new Color((int)gray, (int)gray, (int)gray, 255);
	}
	
	private Color verifiedHexColor(int argb)
	{
		int r = (argb>>16)&0xff;
		int g = (argb>>8)&0xff;
		int b = (argb&0xff);
		
		ilegalColor = false;
		return new Color(r, g, b, 255);		
	}
	
	private Color verifiedHexColor(int argb, float alpha)
	{
		int r = (argb>>16)&0xff;
		int g = (argb>>8)&0xff;
		int b = (argb&0xff);

		ilegalColor = false;
		return new Color(r, g, b, 255);
	}
	
	public Color verifiedRGBColor(float r, float g, float b, float a)
	{
		if (r < 0 || r > colorMode.v1Max || 
			g < 0 || g > colorMode.v2Max ||
			b < 0 || b > colorMode.v3Max) {
			return colorError();
		}

		ilegalColor = false;
		r = r/colorMode.v1Max * 255;
		g = g/colorMode.v2Max * 255;
		b = b/colorMode.v3Max * 255;
		return new Color((int)r, (int)g, (int)b, 255);
	}

	public Color verifiedHSBColor(float h, float s, float b, float a)
	{
		if (h < 0 || h > colorMode.v1Max || 
			s < 0 || s > colorMode.v2Max ||
			b < 0 || b > colorMode.v3Max) {
			return colorError();
		}

		ilegalColor = false;
		Color c = Color.getHSBColor(h/colorMode.v1Max, s/colorMode.v2Max, b/colorMode.v3Max);
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
	}
	
	private Color colorError()
	{
		ilegalColor = true;
		return Color.WHITE;
	}

	public void colorChanged()
	{
		color = getCurrentColor();
	}
	
	public int getTabIndex()
	{
		return handles.get(0).tabIndex;
	}
	
	public int getLine()
	{
		return handles.get(0).line;
	}
	
	public int getCharIndex()
	{
		int lastHandle = handles.size()-1;
		return handles.get(lastHandle).newEndChar + 2;
	}
	
	public String toString()
	{
		return handles.size() + " handles, color mode: " + colorMode.toString();
	}
}
