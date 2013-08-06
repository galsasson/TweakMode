package processing.mode.tweak;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class HProgressBar {
	int x, y, size;
	int pos;
	
	public HProgressBar(int size)
	{
		this.size = size;
		x = 0;
		y = 0;
		pos = 0;
	}
	
	public void setPos(int pos)
	{
		this.pos = pos;
	}
	
	public void draw(Graphics2D g2d)
	{
		AffineTransform trans = g2d.getTransform();
		g2d.translate(x, y);
		
		g2d.setColor(ColorScheme.getInstance().progressFillColor);
		if (pos < 0) {
			g2d.fillRect(pos, 0, -pos, size/3);
		}
		else {
			g2d.fillRect(0, 0, pos, size/3);
		}
		
		g2d.setTransform(trans);
	}

}
