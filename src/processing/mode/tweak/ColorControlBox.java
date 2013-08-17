package processing.mode.tweak;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class ColorControlBox {
	
	ArrayList<Handle> handles;
	ColorMode colorMode;
	Color color;
	int tabIndex;
	int line;
	int charIndex;
	
	// interface
	int x, y, width, height;
	
	public ColorControlBox(ColorMode mode, ArrayList<Handle> handles, int tab, int line, int pos)
	{
		this.colorMode = mode;
		this.handles = handles;
		tabIndex = tab;
		this.line = line;
		charIndex = pos;
		
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
		
		g2d.setColor(color);
		g2d.fillRect(0, 0, width, height);
		
		g2d.setTransform(trans);
	}
	
	public Color getCurrentColor()
	{
		if (handles.size() == 1)
		{
			int a, r, g, b;
			if (handles.get(0).type == "hex") {
				// color([a]rgb)
				int value = handles.get(0).newValue.intValue();
				if (value > 0xffffff) {
					a = ((value&0xff000000) >> 24);					
				}
				else {
					a = 255;
				}
				r = ((value&0x00ff0000) >> 16);
				g = ((value&0x0000ff00) >> 8);
				b = (value&0x000000ff);
				return new Color(r, g, b, a);
			}
			
			// color(gray)
			float gray = handles.get(0).newValue.floatValue();
			r = g = b = (int)(gray / colorMode.v1Max * 255);
			return new Color(r, g, b, 255);
		}
		else if (handles.size() == 2)
		{
			int r, g, b, a;
			if (handles.get(0).type == "hex") {
				// color(rgb, a)
				int rgb = handles.get(0).newValue.intValue();
				r = ((rgb&0xff0000) >> 16);
				g = ((rgb&0x00ff00) >> 8);
				b = (rgb&0x0000ff);
				float tmpa = handles.get(1).newValue.floatValue();
				a = (int)(tmpa / colorMode.aMax * 255);
				return new Color(r, g, b, a);
			}
			
			// color(gray, alpha)
			float gray = handles.get(0).newValue.floatValue();
			r = g = b = (int)(gray / colorMode.v1Max * 255);
			float tmpa = handles.get(1).newValue.floatValue();
			a = (int)(tmpa / colorMode.aMax * 255);
			return new Color(r, g, b, a);
		}
		else if (handles.size() == 3)
		{
			// color(v1, v2, v3)
			float tmp = handles.get(0).newValue.floatValue();
			float v1 = (tmp / colorMode.v1Max * 255);
			tmp = handles.get(1).newValue.floatValue();
			float v2 = (tmp / colorMode.v2Max * 255);
			tmp = handles.get(2).newValue.floatValue();
			float v3 = (tmp / colorMode.v3Max * 255);
			
			if (colorMode.modeType == ColorMode.RGB) {
				return new Color((int)v1, (int)v2, (int)v3, 255);
			}
			
			// color(v1, v2, v3) HSB
			Color c = Color.getHSBColor(v1 / 255 * 360, v2 / 255, v3 / 255);
			return c;
		}
		else if (handles.size() == 4)
		{
			// color(v1, v2, v3, alpha)
			float tmp = handles.get(0).newValue.floatValue();
			float v1 = (tmp / colorMode.v1Max * 255);
			tmp = handles.get(1).newValue.floatValue();
			float v2 = (tmp / colorMode.v2Max * 255);
			tmp = handles.get(2).newValue.floatValue();
			float v3 = (tmp / colorMode.v3Max * 255);
			tmp = handles.get(3).newValue.floatValue();
			float alpha = (tmp / colorMode.aMax * 255);
			
			if (colorMode.modeType == ColorMode.RGB) {
				return new Color((int)v1, (int)v2, (int)v3, (int)alpha);
			}
			
			// color(v1, v2, v3) HSB
			Color c = Color.getHSBColor(v1 / 255 * 360, v2 / 255, v3 / 255);
			return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)alpha);	// no better way?
		}
		
		// couldn't figure out this color
		return null;
	}
	
	public String toString()
	{
		return handles.size() + " handles, color mode: " + colorMode.toString();
	}
}
