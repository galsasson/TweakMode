package processing.mode.sketchtweak;

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
	public int varIndex;
	int tabIndex;
	int startChar, endChar, line;
	int newStartChar, newEndChar;
	
	// interface
	int x, y, width, height;
	int ballX, ballY;
	int anchorX, anchorY;
	double strength;
	
	public Number(String t, String n, int vi, String v, int ti, int l, int sc, int ec)
	{
		type = t;
		name = n;
		varIndex = vi;
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
		anchorY = 2;
		
		strength = 0;
		
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
			int val = Integer.parseInt(value) + (int)strength;		
			newValue = Integer.toString(val);
		}
		else {
			float val = Float.parseFloat(value) + (float)strength;
			newValue = String.format("%.03f", val);
		}
	}
	
	private double getStrength()
	{
		if (ballY == 0)
			return 0;
			
		double amountX = 4 - (double)Math.abs(ballY)/100 * 2;
		double amountY = getNormalDist(amountX) * 1000;
		amountY = (double)Math.round(amountY * 1000) / 1000;
		if (ballY>0)
			amountY *= -1;
		
//		System.out.println("amountY = " + amountY);
		return amountY;			
	}
	
	private double getNormalDist(double _x)
	{
	  double tmp = -0.5 * Math.pow(_x, 2);
	  return (1 / Math.sqrt(2*Math.PI)) * Math.exp(tmp);
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
		
		g2d.setColor(new Color(160, 20, 20));	// dark red
				
		// draw bottom line
		g2d.fillRect(0, 0, width, 2);
		// draw ball and line
		g2d.fillArc(anchorX + ballX - 3, anchorY + ballY - 3, 6, 6, 0, 360);
		
		// don't draw vertical line on the text
		if (ballY < -height)
			g2d.drawLine(anchorX, anchorY-height, anchorX, anchorY+ballY);
		else
			g2d.drawLine(anchorX, anchorY, anchorX, anchorY+ballY);
		// draw horizontal line
		g2d.drawLine(anchorX, anchorY+ballY, anchorX+ballX, anchorY+ballY);
		
		// draw marker
		if (highlight) {
			g2d.setColor(new Color(228,240,91, 127));
			g2d.fillRect(-2, -height, width+4, height);
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
}
