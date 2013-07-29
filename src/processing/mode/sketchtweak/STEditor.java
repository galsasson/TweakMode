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
import javax.swing.text.BadLocationException;

import processing.app.Base;
import processing.app.EditorState;
import processing.app.EditorToolbar;
import processing.app.Mode;
import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.PdeTextAreaDefaults;
import processing.app.syntax.SyntaxDocument;
import processing.mode.java.JavaBuild;
import processing.mode.java.JavaEditor;
import processing.mode.java.JavaToolbar;
import processing.mode.java.runner.Runner;

/**
 * Editor for STMode
 * 
 * @author Gal Sasson &lt;sasgal@gmail.com&gt;
 * 
 */
public class STEditor extends JavaEditor 
{
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
	
	public EditorToolbar createToolbar() {
		return new STToolbar(this, base);
	}

	/**
	 * Override creation of the default textarea.
	 */
	protected JEditTextArea createTextArea() {
		stTextArea = new STTextArea(this, new PdeTextAreaDefaults(mode));
		return stTextArea;
	}
	
	@Override
	public void handleStop()
	{
		super.handleStop();
	}

	public JMenu buildModeMenu() {
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
		if (wasCodeModified()) {
			// ask to keep the values
			int ret = Base.showYesNoQuestion(this, "Sketch Tweak", 
									"Keep the changes?", 
									"You changed some values in your sketch. Would you like to keep the changes?");
			if (ret == 1) {
				// Don't keep changes
				loadSavedSketch();
				// update the painter to draw the new (old) code
				stTextArea.invalidate();
			}
			else {
				// the new values are already present, just make sure the user can save
				sketch.setModified(true);
			}
		}
		
		stTextArea.stopInteractiveMode();
	}
	
	public void updateInterface(ArrayList<Number> numbers)
	{
		stTextArea.updateInterface(numbers);
	}
	
	/**
	* Deactivate run button
	* Do this because when Mode.handleRun returns null the play button stays on.
	*/
	public void deactivateRun()
	{
		toolbar.deactivate(STToolbar.RUN);
	}
	
	private boolean wasCodeModified()
	{
		for (SketchCode c : sketch.getCode()) {
			if (!c.getProgram().equals(c.getSavedProgram())) {
				return true;
			}
		}
		
		return false;
	}
	
	private void loadSavedSketch()
	{
		for (SketchCode c : sketch.getCode()) {
			if (!c.getProgram().equals(c.getSavedProgram())) {
				c.setProgram(c.getSavedProgram());		
				// set document to null so the text editor will refresh program contents
				// when the document tab is being clicked
				c.setDocument(null);
			}
		}
		
		// this will update the current code		
		setCode(sketch.getCurrentCode());
	}
}
