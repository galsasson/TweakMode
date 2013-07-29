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

import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import processing.app.Editor;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.SyntaxDocument;
import processing.app.syntax.SyntaxStyle;
import processing.app.syntax.TextAreaDefaults;
import processing.app.syntax.Token;
import processing.app.syntax.TokenMarker;

/**
 * Custom TextArea for Tweak Mode
 * 
 * @author Gal Sasson &lt;sasgal@gmail.com&gt;
 * 
 */
public class TweakTextArea extends JEditTextArea {
	public Editor editor;
	TweakTextAreaPainter tweakPainter;
	
	// save input listeners to stop/start text edit
	ComponentListener[] prevCompListeners;
	MouseListener[] prevMouseListeners;
	MouseMotionListener[] prevMMotionListeners;
	
	boolean interactiveMode;

	public TweakTextArea(Editor editor, TextAreaDefaults defaults) {
		super(defaults);
		this.editor = editor;
		ComponentListener[] componentListeners = painter
				.getComponentListeners();
		MouseListener[] mouseListeners = painter.getMouseListeners();
		MouseMotionListener[] mouseMotionListeners = painter
				.getMouseMotionListeners();

		remove(painter);

		tweakPainter = new TweakTextAreaPainter(this, defaults);
		painter = tweakPainter;

		for (ComponentListener cl : componentListeners)
			painter.addComponentListener(cl);

		for (MouseListener ml : mouseListeners)
			painter.addMouseListener(ml);

		for (MouseMotionListener mml : mouseMotionListeners)
			painter.addMouseMotionListener(mml);

		prevCompListeners = componentListeners;
		prevMouseListeners = mouseListeners;
		prevMMotionListeners = mouseMotionListeners;
		
		interactiveMode = false;
		
		add(CENTER, painter);
	}
	
	/**
	* Set document with a twist, includes the old caret
	* and scroll positions, added for p5. [fry]
	* - verify that start and stop fall inside the document [gal]
	*/
	@Override
	public void setDocument(SyntaxDocument document,
			int start, int stop, int scroll) {
		if (this.document == document)
			return;
		if (this.document != null)
			this.document.removeDocumentListener(documentHandler);
		this.document = document;

		document.addDocumentListener(documentHandler);

		if (start > document.getLength())
			start = document.getLength();
		if (stop > document.getLength())
			stop = document.getLength();
		
		select(start, stop);
		updateScrollBars();
		setScrollPosition(scroll);
		painter.repaint();
	}
	
	
	/* remove all standard interaction listeners */
	public void removeAllListeners()
	{
		ComponentListener[] componentListeners = painter
				.getComponentListeners();
		MouseListener[] mouseListeners = painter.getMouseListeners();
		MouseMotionListener[] mouseMotionListeners = painter
				.getMouseMotionListeners();

		for (ComponentListener cl : componentListeners)
			painter.removeComponentListener(cl);

		for (MouseListener ml : mouseListeners)
			painter.removeMouseListener(ml);

		for (MouseMotionListener mml : mouseMotionListeners)
			painter.removeMouseMotionListener(mml);		
	}
	
	public void startInteractiveMode()
	{
		// ignore if we are already in interactiveMode
		if (interactiveMode)
			return;
		
		interactiveMode = true;
		
		this.editable = false;
		removeAllListeners();
		
		// add our private interaction listeners
		tweakPainter.addMouseListener(tweakPainter);
		tweakPainter.addMouseMotionListener(tweakPainter);
		tweakPainter.startInterativeMode();
		painter.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void stopInteractiveMode()
	{
		// ignore if we are not in interactive mode
		if (!interactiveMode)
			return;
		
		this.editable = true;
		removeAllListeners();
		
		// add the original text-edit listeners
		for (ComponentListener cl : prevCompListeners)
			painter.addComponentListener(cl);

		for (MouseListener ml : prevMouseListeners)
			painter.addMouseListener(ml);

		for (MouseMotionListener mml : prevMMotionListeners)
			painter.addMouseMotionListener(mml);		
		
		tweakPainter.stopInteractiveMode();
		painter.setCursor(new Cursor(Cursor.TEXT_CURSOR));
		
		interactiveMode = false;
	}
	
	public void updateInterface(ArrayList<Handle> numbers)
	{
		tweakPainter.updateInterface(numbers);
	}
	
}
