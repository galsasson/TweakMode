package processing.mode.tweak;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.Comparator;

public class Handle {
	public String type;
	public String name;
	public String strValue;
	public String strNewValue;
	public int varIndex;
	int tabIndex;
	int startChar, endChar, line;
	int newStartChar, newEndChar;
	int numDigits;					// number of digits after the decimal point
	float incValue;
	
	java.lang.Number value, newValue;
	String strDiff;
	
	// interface
	int x, y, width, height;
	int xCenter, xCurrent, xLast;
	HProgressBar progBar = null;
	String textFormat;
	
	boolean showDiff;
	
	
	public Handle(String t, String n, int vi, String v, int ti, int l, int sc, int ec, int nd)
	{
		type = t;
		name = n;
		varIndex = vi;
		strValue = v;
		tabIndex = ti;
		line = l;
		startChar = sc;
		endChar = ec;
		numDigits = nd;
		
		incValue = (float)(1/Math.pow(10, numDigits));

		if (type == "int") {
			value = newValue = Integer.parseInt(strValue);
			strNewValue = strValue;
			textFormat = "%d";
		}
		else if (type == "hex") {
			value = newValue = Integer.parseInt(strValue.substring(2, strValue.length()), 16);
			strNewValue = strValue;
			textFormat = "0x%x";
		}
		else if (type == "float") {
			value = newValue = Float.parseFloat(strValue);
			strNewValue = strValue;
			textFormat = "%.0" + numDigits + "f";
		}
		
		newStartChar = startChar;
		newEndChar = endChar;
	}
	
	public String toString()
	{
		return type + " " + name + " = " + strValue + 
				" (tab: " + tabIndex + ", line: " + line + 
				", start: " + startChar + ", end: " + endChar + ")"; 
	}
	
	public void initInterface(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		// create drag ball
		progBar = new HProgressBar(height, width);
	}
	
	public void setCenterX(int mx)
	{
		xLast = xCurrent = xCenter = mx;
	}
	
	public void setCurrentX(int mx)
	{
		xLast = xCurrent;
		xCurrent = mx;
		
		progBar.setPos(xCurrent - xCenter);
		
		updateValue();
	}
	
	public void resetProgress()
	{
		progBar.setPos(0);
	}
	
	public void updateValue()
	{
		float change = getChange();
		
		if (type == "int") {
			if ((Integer)newValue + (int)change > Integer.MAX_VALUE ||
					(Integer)newValue + (int)change < Integer.MIN_VALUE) {
				change = 0;
			}
			newValue = (Integer)newValue + (int)change;
			strNewValue = String.format(textFormat, (Integer)newValue);
		}
		else if (type == "hex") {
			if ((Integer)newValue + (int)change < 0) {
				change = 0;
			}
			newValue = (Integer)newValue + (int)change;
			strNewValue = String.format(textFormat, (Integer)newValue);			
		}
		else if (type == "float") {
			if ((Float)newValue + change > Float.MAX_VALUE ||
					(Float)newValue + change < Float.MIN_VALUE) {
				change = 0;
			}
			newValue = (Float)newValue + change;
			strNewValue = String.format(textFormat, (Float)newValue);
		}
	}
	
	private float getChange()
	{
		int pixels = xCurrent - xLast;

		return (float)pixels*incValue;
	}
	
	public void setPos(int nx, int ny)
	{
		x = nx;
		y = ny;
	}
	
	public void setWidth(int w)
	{
		width = w;
		
		progBar.setWidth(w);
	}
	
	public void draw(Graphics2D g2d, boolean hasFocus)
	{
		AffineTransform prevTrans = g2d.getTransform();
		g2d.translate(x, y);
		
		// draw underline on the number
		g2d.setColor(ColorScheme.getInstance().progressFillColor);
		g2d.drawLine(0, 0, width, 0);
		
		if (hasFocus) {
			if (progBar != null) {
				g2d.translate(width/2, 2);
				progBar.draw(g2d);
			}
		}
		
		g2d.setTransform(prevTrans);
	}
	
	public boolean pick(int mx, int my)
	{
		return pickText(mx, my);
	}
	
	public boolean pickText(int mx, int my)
	{
		if (mx>x-2 && mx<x+width+2 && my>y-height && my<y) {
			return true;
		}
		
		return false;
	}
	
	public boolean valueChanged()
	{
		if (type == "int") {
			return !((Integer)value).equals((Integer)newValue);
		}
		else if (type == "hex") {
			return (!((Integer)value).equals((Integer)newValue));
		}
		else {
			return !((Float)value).equals((Float)newValue);
		}
	}
}

/*
 * Used for sorting the handles by order of occurrence inside each tab 
 */
class HandleComparator implements Comparator<Handle> {
    public int compare(Handle handle1, Handle handle2) {
    	int tab = handle1.tabIndex - handle2.tabIndex;
    	if (tab == 0) {
    		return handle1.startChar - handle2.startChar;
    	}
    	else {
    		return tab;
    	}
    }
}
