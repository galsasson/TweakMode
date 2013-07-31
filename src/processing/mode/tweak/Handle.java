package processing.mode.tweak;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

public class Handle {
	public String type;
	public String name;
	public String strValue;
	public String strNewValue;
	public int varIndex;
	int tabIndex;
	int startChar, endChar, line;
	int newStartChar, newEndChar;
	
	java.lang.Number value, newValue;
	String strDiff;
	// interface
	int x, y, width, height;
	int ballX, ballY;
	int anchorX, anchorY;
	float strength;
	boolean showDiff;
	
	public Handle(String t, String n, int vi, String v, int ti, int l, int sc, int ec)
	{
		type = t;
		name = n;
		varIndex = vi;
		strValue = v;
		tabIndex = ti;
		line = l;
		startChar = sc;
		endChar = ec;

		if (type == "int") {
			value = newValue = Integer.parseInt(strValue);
			strNewValue = new String(strValue);
		} 
		else {
			value = newValue = Float.parseFloat(strValue);
			strNewValue = String.format("%.03f", (Float)newValue);
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
		
		anchorX = width/2;
		anchorY = 2;
		
		strength = 0;
		showDiff = false;
		
		resetBallPos();
	}
	
	public void setBallPos(int bx, int by)
	{
		int screenAnchorX = x + anchorX;
		int screenAnchorY = y + anchorY;
		
		ballX = bx - screenAnchorX;
		ballY = by - screenAnchorY;
		
		updateValue();	
	}
	
	public void resetBallPos()
	{
		ballX = 0;
		ballY = 0;
		
		updateValue();
	}
	
	public void updateValue()
	{
		/* calculate strength of change based on the distance of the ball from its base. */ 
		// map ballY form 0 - 100 to 4 - 1
		strength = getStrength();
		
		if (type == "int") {
			// how many pixels above/below the line
			newValue = Integer.parseInt(strValue) + (int)strength;		
			strNewValue = Integer.toString((Integer)newValue);
			int diff = (Integer)newValue - (Integer)value;
			if (diff != 0) {
				strDiff = Integer.toString(diff);
				if (diff > 0) strDiff = "+" + strDiff;
				showDiff = true;
			}
			else {
				showDiff = false;
			}
		}
		else {
			newValue = Float.parseFloat(strValue) + (float)strength;
			strNewValue = String.format("%.03f", (Float)newValue);
			float diff = (Float)newValue - (Float)value;
			if (diff != 0) {
				strDiff = String.format("%.03f", diff);
				if (diff > 0) strDiff = "+" + strDiff;
				showDiff = true;
			}
			else {
				showDiff = false;
			}
		}
	}
	
	private float getStrength()
	{
		float amount = (float)Math.abs(ballY);
		
		if (amount == 0) {
			return 0;
		}
		
		amount = (amount>500)? 500 : amount;
		
		// map 0-500 to 5.2-0.2
		float xAxis = 5.2f - amount/100;
		float yAxis;
		if (type == "int") {
			yAxis = getNormalDistInt(xAxis) * 1000;
		}
		else {
			yAxis = getNormalDistFloat(xAxis) * 1000;
		}		
		// round to 3 digits after the decimal point
		yAxis = (float)Math.round(yAxis * 1000) / 1000;	
		// handle sign
		if (ballY>0) {
			yAxis *= -1;
		}
		
		return yAxis;			
	}
	
	/**
	* Calculate normal distribution with mean of 0
	*/
	private float getNormalDistFloat(double _x)
	{
	  float tmp = -0.5f * (float)Math.pow(_x, 2);
	  return (1 / (float)Math.sqrt(2*(float)Math.PI)) * (float)Math.exp(tmp);
	}

	private float getNormalDistInt(double _x)
	{
	  float tmp = -0.25f * (float)Math.pow(_x, 2);
	  return (1 / (float)Math.sqrt(2*(float)Math.PI)) * (float)Math.exp(tmp);
	}
		
	public void setPos(int nx, int ny)
	{
		x = nx;
		y = ny;
	}
	
	public void setWidth(int w)
	{
		width = w;
		anchorX = width/2;
	}
	
	public void draw(Graphics2D g2d, boolean highlight)
	{
		AffineTransform prevTrans = g2d.getTransform();
		g2d.translate(x, y);
		
		g2d.setColor(ColorScheme.getInstance().redStrokeColor);
				
		// draw bottom line
		g2d.fillRect(0, 0, width, 2);
		// draw ball
		g2d.fillArc(anchorX + ballX - 3, anchorY + ballY - 3, 6, 6, 0, 360);
		
		// skip text when drawing the vertical line
		if (ballY < -height)
			g2d.drawLine(anchorX, anchorY-height, anchorX, anchorY+ballY);
		else
			g2d.drawLine(anchorX, anchorY, anchorX, anchorY+ballY);
		// draw horizontal line
		g2d.drawLine(anchorX, anchorY+ballY, anchorX+ballX, anchorY+ballY);
		
		// draw marker
		if (highlight) {
			g2d.setColor(ColorScheme.getInstance().markerColor);
			g2d.fillRect(-2, -height, width+4, height);
		}
		
		// draw increment text
		if (showDiff) {
			g2d.setColor(ColorScheme.getInstance().redStrokeColor);
			float diffW = g2d.getFontMetrics().charsWidth(strDiff.toCharArray(), 0, strDiff.length());
			float xx = 0;
			if (ballX > diffW+9) {
				xx = anchorX+ballX-diffW-5;
			}
			else if (ballX < -diffW-5) {
				xx = anchorX+ballX+5;
			}
			else {
				xx = anchorX+5;
			}
			g2d.drawString(strDiff, xx, anchorY+ballY-1);
		}
		
		g2d.setTransform(prevTrans);
	}
	
	public boolean pick(int mx, int my)
	{
		return pickText(mx, my) || pickBall(mx, my);
	}
	
	public boolean pickText(int mx, int my)
	{
		if (mx>x-2 && mx<x+width+2 && my>y-height && my<y) {
			return true;
		}
		
		return false;
	}
	
	public boolean pickBall(int mx, int my)
	{
		int scrAnchorX = x + anchorX;
		int scrAnchorY = y + anchorY;
		
		// if distance between mouse and ball < 6
		if (Math.sqrt(Math.pow(mx-(scrAnchorX+ballX), 2) + Math.pow(my-(scrAnchorY+ballY),  2)) < 6) {
			return true;
		}
		
		return false;		
	}
	
	public boolean valueChanged()
	{
		if (type == "int") {
			return !((Integer)value).equals((Integer)newValue);
		}
		else {
			return !((Float)value).equals((Float)newValue);
		}
	}
}
