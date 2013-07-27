/*
  Part of NumberSenseMode project (https://github.com/galsasson/NumberSenseMode)
  
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

package com.gal.numbersensemode;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
public class NumberSenseTextAreaPainter extends TextAreaPainter 
	implements MouseListener, MouseMotionListener {

	protected NumberSenseTextArea ta;
	protected int horizontalAdjustment = 0;
	
	public boolean interactiveMode = false;
	public ArrayList<Number> numbers = null;
	public Number mouseNumber = null;
	
	private final Object paintMutex = new Object();
	
	public NumberSenseTextAreaPainter(NumberSenseTextArea textArea, TextAreaDefaults defaults) 
	{
		super((JEditTextArea)textArea, defaults);
		System.out.println("NumberSenseTextAreaPainter constructor");
		ta = textArea;
		interactiveMode = false;
	}
	
	/**
	* Repaints the text.
	* @param gfx The graphics context
	*/
	@Override
	public void paint(Graphics gfx) {
		synchronized(paintMutex) {
			super.paint(gfx);

			if (interactiveMode && numbers!=null)
			{
				// enable anti-aliasing
				Graphics2D g2d = (Graphics2D)gfx;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                				RenderingHints.VALUE_ANTIALIAS_ON);

				for (Number n : numbers)
				{
					// draw only interface points that belong to the current tab
					if (n.tabIndex != ta.editor.getSketch().getCurrentCodeIndex())
						continue;
				
					int lineStartChar = ta.getLineStartOffset(n.line);
					int x = ta.offsetToX(n.line, n.newStartChar - lineStartChar);
					int y = ta.lineToY(n.line) + fm.getHeight();
					int end = ta.offsetToX(n.line, n.newEndChar - lineStartChar);
					n.setPos(x, y);
					n.setWidth(end - x);
					n.draw(g2d);
				}
			}
		}	// synchronized section end
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
	public void updateInterface(ArrayList<Number> numbers)
	{
		this.numbers = numbers;
		
		initInterfacePositions();		
	}
	
	public void initInterfacePositions()
	{
		// synchronize this section (don't paint while we make changes to the text of the editor)
		synchronized(paintMutex) {
			SketchCode[] code = ta.editor.getSketch().getCode();

			int prevScroll = ta.getScrollPosition();
			String prevText = ta.getText();
		
			for (int tab=0; tab<code.length; tab++)
			{
				ta.setText(code[tab].getSavedProgram());
				for (Number n : numbers)
				{
					// handle only interface points in tab 'tab'.
					if (n.tabIndex != tab)
						continue;
				
					int lineStartChar = ta.getLineStartOffset(n.line);
					int x = ta.offsetToX(n.line, n.newStartChar - lineStartChar);
					int end = ta.offsetToX(n.line, n.newEndChar - lineStartChar);
					int y = ta.lineToY(n.line) + fm.getHeight();
					n.initInterface(x, y, end-x, 2);
				}
			}
		
			ta.setText(prevText);
			ta.scrollTo(prevScroll, 0);
		}	// synchronized section end
		
		repaint();
	}

	/**
	 * Trims out trailing white-spaces (to the right)
	 * 
	 * @param string
	 * @return - String
	 */
	public String trimRight(String string) {
		String newString = "";
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) != ' ') {
				newString = string.substring(0, i) + string.trim();
				break;
			}
		}
		return newString;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (mouseNumber != null) {
			mouseNumber.setBallPos(e.getX(),  e.getY());
			
			// send OSC message - value changed.
			oscSendNewValue(mouseNumber);

			// update code text with the new value
			updateCodeText();
			
			repaint();
		}
	}
	
	/**
	 * Take the saved code and replace all numbers with their current values.
	 * Update TextArea with the new code.
	 */
	public void updateCodeText()
	{
		int charInc = 0;
		int currentTab = ta.editor.getSketch().getCurrentCodeIndex();
		String code = ta.editor.getSketch().getCode(currentTab).getSavedProgram();

		for (Number n : numbers)
		{
			if (n.tabIndex != currentTab)
				continue;
			
			int s = n.startChar + charInc;
			int e = n.endChar + charInc;
			code = replaceString(code, s, e, n.newValue);
			n.newStartChar = n.startChar + charInc;
			charInc += n.newValue.length() - n.value.length();
			n.newEndChar = n.endChar + charInc;
		}
		
		// don't paint while we do the stuff below
		synchronized(paintMutex) {
			/* by default setText will scroll all the way to the end
			 * remember current scroll position
			 * TODO: this doesn't work yet for horizontal scroll */
			int scrollLine = ta.getScrollPosition();
			int scrollHor = ta.getHorizontalOffset();	
			ta.setText(code);
			ta.scrollTo(scrollLine, scrollHor);
		}
	}
	
	public String replaceString(String str, int start, int end, String put)
	{
		return str.substring(0, start) + put + str.substring(end, str.length());
	}

	public void oscSendNewValue(Number n)
	{
		int index = n.varIndex;
		try {
			if (n.type == "int") {
				int val = Integer.parseInt(n.newValue);
				OSCSender.sendInt(index, val);
			}
			else {
				float val = Float.parseFloat(n.newValue);
				OSCSender.sendFloat(index, val);
			}
		} catch (Exception e) { System.out.println("error sending OSC message!"); }
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (mouseNumber != null)
		{
			mouseNumber.resetBallPos();
			
			// send OSC message - value changed.
			oscSendNewValue(mouseNumber);

			// update code text with the new value
			updateCodeText();			

			mouseNumber = null;
			repaint();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		for (Number n : numbers)
		{
			if (n.pick(e.getX(), e.getY()))
			{
				mouseNumber = n;
				mouseNumber.setBallPos(e.getX(), e.getY());
				repaint();
			}
		}	
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (mouseNumber != null)
			mouseNumber = null;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
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
