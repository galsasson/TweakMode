/*
  Part of SketchTweakMode project (https://github.com/galsasson/SketchTweakMode)
  
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

package processing.mode.sketchtweak;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableModel;

import processing.app.Base;
import processing.app.EditorState;
import processing.app.Mode;
import processing.app.SketchException;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.PdeTextAreaDefaults;
import processing.mode.java.JavaBuild;
import processing.mode.java.JavaEditor;

/**
 * Editor for STMode
 * 
 * @author Gal Sasson &lt;sasgal@gmail.com&gt;
 * 
 */
public class STEditor extends JavaEditor {

	SketchTweakMode stmode;
	/**
	 * Custom TextArea
	 */
	protected STTextArea stTextArea;
	protected final STEditor thisEditor;

	protected STEditor(Base base, String path, EditorState state,
			final Mode mode) {
		super(base, path, state, mode);
		thisEditor = this;

		stmode = (SketchTweakMode)mode;
	}

	/**
	 * Override creation of the default textarea.
	 */
	protected JEditTextArea createTextArea() {
		stTextArea = new STTextArea(this, new PdeTextAreaDefaults(mode));
		return stTextArea;
	}
	
	public void handleStop()
	{
		super.handleStop();
		stmode.handleStop();
	}

	public JMenu buildModeMenu() {

		// Enable Error Checker - CB
		// Show/Hide Problem Window - CB
		// Show Warnings - CB
		JMenu menu = new JMenu("SketchTweakMode");
		JCheckBoxMenuItem item;

		item = new JCheckBoxMenuItem("Just a dummy item");
		item.setSelected(true);
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!((JCheckBoxMenuItem) e.getSource()).isSelected()) {
					System.out.println("unselected item");
					getTextArea().repaint();
				} else {
					System.out.println("selected item");
				}
			}
		});
		menu.add(item);

		return menu;
	}
	
	public void startInteractiveMode()
	{
		stTextArea.startInteractiveMode();
	}
	
	public void stopInteractiveMode()
	{
		stTextArea.stopInteractiveMode();
	}
	
	public void updateInterface(ArrayList<Number> numbers)
	{
		stTextArea.updateInterface(numbers);
	}
}
