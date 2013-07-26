package com.gal.numbersensemode;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

public class Number {
	public String type;
	public String name;
	public String value;
	public String newValue;
	int tabIndex;
	int startChar, endChar, line;
	int newStartChar, newEndChar;
	
	// interface
	int x, y, width, height;
	int ballX, ballY;
	int anchorX, anchorY;
	
	public Number(String t, String n, String v, int ti, int l, int sc, int ec)
	{
		type = t;
		name = n;
		value = v;
		tabIndex = ti;
		line = l;
		startChar = sc;
		endChar = ec;
		
		newValue = new String(value);
		newStartChar = startChar;
		newEndChar = endChar;
	}
	
	public String toString()
	{
		return type + " " + name + " = " + value + 
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
		anchorY = 4;
		
		resetBallPos();
	}
	
	public void setBallPos(int bx, int by)
	{
		int screenAnchorX = x + anchorX;
		int screenAnchorY = y + anchorY;
		
		ballX = bx - screenAnchorX;
		ballY = by - screenAnchorY;
		
		if (type == "int") {
			int val = Integer.parseInt(value) - ballY;
			newValue = Integer.toString(val);
		}
		else {
			float val = Float.parseFloat(value) - ballY;
			newValue = Float.toString(val);
		}
	}
		
	public void resetBallPos()
	{
		ballX = 0;
		ballY = 0;
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
	
	public void draw(Graphics2D g2d)
	{
		AffineTransform prevTrans = g2d.getTransform();
		g2d.translate(x, y);
		
		g2d.setColor(Color.RED);
		
		// draw bottom line
		g2d.fillRect(0, 0, width, height);
		
		// draw ball
		g2d.fillArc(anchorX + ballX - 3, anchorY + ballY - 3, 6, 6, 0, 360);
		g2d.drawLine(anchorX + ballX, anchorY + ballY, anchorX, anchorY);
		
		g2d.setTransform(prevTrans);
	}
	
	public boolean pick(int mx, int my)
	{
		int scrAnchorX = x + anchorX;
		int scrAnchorY = y + anchorY;
		
		// if distance between mouse and ball < 6
		if (Math.sqrt(Math.pow(mx-(scrAnchorX+ballX), 2) + Math.pow(my-(scrAnchorY+ballY),  2)) < 6) {
			return true;
		}
		
		return false;
	}
}
