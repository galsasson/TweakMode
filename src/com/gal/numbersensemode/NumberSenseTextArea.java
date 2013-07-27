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

import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import processing.app.Editor;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.SyntaxStyle;
import processing.app.syntax.TextAreaDefaults;
import processing.app.syntax.Token;
import processing.app.syntax.TokenMarker;

/**
 * Custom TextArea for NumberSenseMode
 * 
 * @author Gal Sasson &lt;sasgal@gmail.com&gt;
 * 
 */
public class NumberSenseTextArea extends JEditTextArea {
	public Editor editor;
	NumberSenseTextAreaPainter nspainter;
	
	// save input listeners to stop/start text edit
	ComponentListener[] prevCompListeners;
	MouseListener[] prevMouseListeners;
	MouseMotionListener[] prevMMotionListeners;
	
	boolean interactiveMode;

	public NumberSenseTextArea(Editor editor, TextAreaDefaults defaults) {
		super(defaults);
		System.out.println("NumberSenseTextArea contructor");
		this.editor = editor;
		ComponentListener[] componentListeners = painter
				.getComponentListeners();
		MouseListener[] mouseListeners = painter.getMouseListeners();
		MouseMotionListener[] mouseMotionListeners = painter
				.getMouseMotionListeners();

		remove(painter);

		nspainter = new NumberSenseTextAreaPainter(this, defaults);
		painter = nspainter;

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
		nspainter.addMouseListener(nspainter);
		nspainter.addMouseMotionListener(nspainter);
		nspainter.startInterativeMode();
		painter.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
		
		nspainter.stopInteractiveMode();
		painter.setCursor(new Cursor(Cursor.TEXT_CURSOR));
		
		interactiveMode = false;
	}
	
	public void updateInterface(ArrayList<Number> numbers)
	{
		nspainter.updateInterface(numbers);
	}
	
}
