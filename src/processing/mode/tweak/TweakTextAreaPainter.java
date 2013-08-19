/*
  Part of TweakMode project (https://github.com/galsasson/TweakMode)
  
  Under Google Summer of Code 2013 - 
  http://www.google-melange.com/gsoc/homepage/google/gsoc2013
  
  Copyright (C) 2013 Gal Sasson
	
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2
  as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package processing.mode.tweak;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

import processing.app.SketchCode;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.SyntaxDocument;
import processing.app.syntax.TextAreaDefaults;
import processing.app.syntax.TextAreaPainter;
import processing.app.syntax.TokenMarker;

/**
 * Custom painter for XQTextArea. Handles underlining of error lines.
 * 
 * @author Gal Sasson &lt;sasgal@gmail.com&gt;
 * 
 */
public class TweakTextAreaPainter extends TextAreaPainter 
	implements MouseListener, MouseMotionListener {

	protected TweakTextArea ta;
	protected int horizontalAdjustment = 0;
	
	public boolean interactiveMode = false;
	public ArrayList<Handle> numbers = null;
	public ArrayList<ColorControlBox> colorBoxes = null;
	
	public Handle mouseHandle = null;
	
	int cursorType;
	BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

	// Create a new blank cursor.
	Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
	    cursorImg, new Point(0, 0), "blank cursor");
	
	private final Object paintMutex = new Object();
	
	public TweakTextAreaPainter(TweakTextArea textArea, TextAreaDefaults defaults) 
	{
		super((JEditTextArea)textArea, defaults);
		ta = textArea;
		interactiveMode = false;
		cursorType = Cursor.DEFAULT_CURSOR;
	}
	
	/**
	* Repaints the text.
	* @param gfx The graphics context
	*/
	@Override
	public synchronized void paint(Graphics gfx)
	{
		super.paint(gfx);

		if (interactiveMode && numbers!=null)
		{
			// enable anti-aliasing
			Graphics2D g2d = (Graphics2D)gfx;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                				RenderingHints.VALUE_ANTIALIAS_ON);
			
			for (Handle n : numbers)
			{
				// draw only interface points that belong to the current tab
				if (n.tabIndex != ta.editor.getSketch().getCurrentCodeIndex()) {
					continue;
				}

				// update n position and width, and draw it
				int lineStartChar = ta.getLineStartOffset(n.line);
				int x = ta.offsetToX(n.line, n.newStartChar - lineStartChar);
				int y = ta.lineToY(n.line) + fm.getHeight() + 1;
				int end = ta.offsetToX(n.line, n.newEndChar - lineStartChar);
				n.setPos(x, y);
				n.setWidth(end - x);
				n.draw(g2d, n==mouseHandle);
			}
			
			// draw color boxes
			for (ColorControlBox cBox: colorBoxes)
			{
				// draw only boxes that belong to the current tab
				if (cBox.getTabIndex() != ta.editor.getSketch().getCurrentCodeIndex()) {
					continue;
				}
				
				int lineStartChar = ta.getLineStartOffset(cBox.getLine());
				int x = ta.offsetToX(cBox.getLine(), cBox.getCharIndex() - lineStartChar);
				int y = ta.lineToY(cBox.getLine()) + fm.getDescent();
				cBox.setPos(x, y+1);
				cBox.draw(g2d);
			}
		}
	}
		  
	public void startInterativeMode()
	{
		interactiveMode = true;
		repaint();
	}
	
	public void stopInteractiveMode()
	{
		interactiveMode = false;
		repaint();
	}
	
	// Update the interface
	public void updateInterface(ArrayList<Handle> numbers, ArrayList<ColorControlBox> colorBoxes)
	{
		this.numbers = numbers;
		this.colorBoxes = colorBoxes;
		
		initInterfacePositions();
		repaint();		
	}

	/**
	* Initialize all the number changing interfaces.
	* synchronize this method to prevent the execution of 'paint' in the middle.
	* (don't paint while we make changes to the text of the editor)
	*/
	public synchronized void initInterfacePositions()
	{
		SketchCode[] code = ta.editor.getSketch().getCode();
		int prevScroll = ta.getScrollPosition();
		String prevText = ta.getText();
		
		for (int tab=0; tab<code.length; tab++)
		{
			String tabCode = ((TweakEditor)ta.editor).tweakMode.baseCode[tab];
			ta.setText(tabCode);
			for (Handle n : numbers)
			{
				// handle only interface points in tab 'tab'.
				if (n.tabIndex != tab)
					continue;
				
				int lineStartChar = ta.getLineStartOffset(n.line);
				int x = ta.offsetToX(n.line, n.newStartChar - lineStartChar);
				int end = ta.offsetToX(n.line, n.newEndChar - lineStartChar);
				int y = ta.lineToY(n.line) + fm.getHeight() + 1;
				n.initInterface(x, y, end-x, fm.getHeight());
			}

			for (ColorControlBox cBox : colorBoxes)
			{
				if (cBox.getTabIndex() != tab) {
					continue;
				}

				int lineStartChar = ta.getLineStartOffset(cBox.getLine());
				int x = ta.offsetToX(cBox.getLine(), cBox.getCharIndex() - lineStartChar);
				int y = ta.lineToY(cBox.getLine()) + fm.getDescent();
				cBox.initInterface(x, y+1, fm.getHeight()-2, fm.getHeight()-2);
			}
		}
		
		ta.setText(prevText);
		ta.scrollTo(prevScroll, 0);	
	}

	/**
	 * Take the saved code of the current tab and replace all numbers with their current values.
	 * Update TextArea with the new code.
	 */
	public void updateCodeText()
	{
		int charInc = 0;
		int currentTab = ta.editor.getSketch().getCurrentCodeIndex();
		SketchCode sc = ta.editor.getSketch().getCode(currentTab);
		String code = ((TweakEditor)ta.editor).tweakMode.baseCode[currentTab];
		
		for (Handle n : numbers)
		{
			if (n.tabIndex != currentTab)
				continue;
			
			int s = n.startChar + charInc;
			int e = n.endChar + charInc;
			code = replaceString(code, s, e, n.strNewValue);
			n.newStartChar = n.startChar + charInc;
			charInc += n.strNewValue.length() - n.strValue.length();
			n.newEndChar = n.endChar + charInc;
		}
		
		replaceTextAreaCode(code);
		// update also the sketch code for later
		sc.setProgram(code);
	}
	
	private synchronized void replaceTextAreaCode(String code)
	{
			// don't paint while we do the stuff below
			/* by default setText will scroll all the way to the end
			 * remember current scroll position */
			int scrollLine = ta.getScrollPosition();
			int scrollHor = ta.getHorizontalScroll();
			ta.setText(code);			
			ta.setOrigin(scrollLine, -scrollHor);			
	}
	
	public String replaceString(String str, int start, int end, String put)
	{
		return str.substring(0, start) + put + str.substring(end, str.length());
	}

	public void oscSendNewValue(Handle n)
	{
		int index = n.varIndex;
		try {
			if (n.type == "int") {
				int val = Integer.parseInt(n.strNewValue);
				OSCSender.sendInt(index, val);
			}
			else if (n.type == "hex") {
				Long val = Long.parseLong(n.strNewValue.substring(2, n.strNewValue.length()), 16);
				OSCSender.sendInt(index, val.intValue());
			}
			else if (n.type == "float") {
				float val = Float.parseFloat(n.strNewValue);
				OSCSender.sendFloat(index, val);
			}
		} catch (Exception e) { System.out.println("error sending OSC message!"); }
	}
	
	public void updateCursor(int mouseX, int mouseY)
	{
		for (Handle n : numbers)
		{
			// skip numbers not in the current tag
			if (n.tabIndex != ta.editor.getSketch().getCurrentCodeIndex())
				continue;
			
			if (n.pick(mouseX, mouseY))
			{
				cursorType = Cursor.W_RESIZE_CURSOR;
				setCursor(new Cursor(cursorType));
				return;
			}
		}
		
		if (cursorType == Cursor.W_RESIZE_CURSOR || cursorType == -1) {
			cursorType = Cursor.DEFAULT_CURSOR;
			setCursor(new Cursor(cursorType));
		}		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (mouseHandle != null) {
			mouseHandle.setCurrentX(e.getX());
			
			// send OSC message - value changed.
			oscSendNewValue(mouseHandle);

			// update code text with the new value
			updateCodeText();
			
			repaint();
		}
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		for (Handle n : numbers)
		{
			// skip numbers not in the current tag
			if (n.tabIndex != ta.editor.getSketch().getCurrentCodeIndex())
				continue;
			
			if (n.pick(e.getX(), e.getY()))
			{
				cursorType = -1;
				this.setCursor(blankCursor);
				mouseHandle = n;
				mouseHandle.setCenterX(e.getX());
				repaint();
				return;
			}
		}		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (mouseHandle != null) {
			mouseHandle.resetProgress();
			mouseHandle = null;
			
			updateCursor(e.getX(), e.getY());
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		updateCursor(e.getX(), e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
