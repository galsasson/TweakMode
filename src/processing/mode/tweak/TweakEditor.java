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
public class TweakEditor extends JavaEditor 
{
	TweakMode tweakMode;
	
	/**
	 * Custom TextArea
	 */
	protected TweakTextArea tweakTextArea;

	protected TweakEditor(Base base, String path, EditorState state,
			final Mode mode) {
		super(base, path, state, mode);

		tweakMode = (TweakMode)mode;
	}
	
	public EditorToolbar createToolbar() {
		return new TweakToolbar(this, base);
	}

	/**
	 * Override creation of the default textarea.
	 */
	protected JEditTextArea createTextArea() {
		tweakTextArea = new TweakTextArea(this, new PdeTextAreaDefaults(mode));
		return tweakTextArea;
	}
	
	public JMenu buildModeMenu() {
		JMenu menu = new JMenu("Tweak");
		JCheckBoxMenuItem item;

		item = new JCheckBoxMenuItem("Dump modified code");
		item.setSelected(false);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tweakMode.dumpModifiedCode = ((JCheckBoxMenuItem)e.getSource()).isSelected();
			}
		});
		menu.add(item);

		return menu;
	}
	
	public void startInteractiveMode()
	{
		tweakTextArea.startInteractiveMode();
	}
	
	public void stopInteractiveMode()
	{				
		tweakTextArea.stopInteractiveMode();

		// must check before we remove the spaces
		boolean modified = wasCodeModified();
				
		// remove space from the code (before and after)
		removeSpacesFromCode();

		if (modified) {
			// ask to keep the values
			int ret = Base.showYesNoQuestion(this, "Sketch Tweak", 
									"Keep the changes?", 
									"You changed some values in your sketch. Would you like to keep the changes?");
			if (ret == 1) {
				// Don't keep changes
				loadSavedCode();
				// update the painter to draw the new (old) code
				tweakTextArea.invalidate();
			}
			else {
				// the new values are already present, just make sure the user can save
				for (SketchCode c : sketch.getCode()) {
					c.setModified(true);
				}
			}
		}
	}
	
	public void updateInterface(ArrayList<Handle> numbers)
	{
		tweakTextArea.updateInterface(numbers);
	}
	
	/**
	* Deactivate run button
	* Do this because when Mode.handleRun returns null the play button stays on.
	*/
	public void deactivateRun()
	{
		toolbar.deactivate(TweakToolbar.RUN);
	}
	
	private boolean wasCodeModified()
	{
		SketchCode[] code = sketch.getCode();
		for (int i=0; i<code.length; i++) {
			if (!code[i].getProgram().equals(tweakMode.baseCode[i])) {
				return true;
			}
		}
		
		return false;
	}
	
	public void initEditorCode(String[] newCode, ArrayList<Handle> handles)
	{
		SketchCode[] sketchCode = sketch.getCode();
		for (int tab=0; tab<newCode.length; tab++) {
				// beautify the numbers
				int charInc = 0;
				String code = newCode[tab];
		
				for (Handle n : handles)
				{
					if (n.tabIndex != tab)
						continue;
			
					int s = n.startChar + charInc;
					int e = n.endChar + charInc;
					code = replaceString(code, s, e, n.strNewValue);
					n.newStartChar = n.startChar + charInc;
					charInc += n.strNewValue.length() - n.strValue.length();
					n.newEndChar = n.endChar + charInc;
				}
				
				sketchCode[tab].setProgram(code);		
				/* Wild Hack: set document to null so the text editor will refresh 
				   the program contents when the document tab is being clicked */
				sketchCode[tab].setDocument(null);
			}
		
		// this will update the current code		
		setCode(sketch.getCurrentCode());
	}
	
	private void loadSavedCode()
	{
		SketchCode[] code = sketch.getCode();
		for (int i=0; i<code.length; i++) {
			if (!code[i].getProgram().equals(code[i].getSavedProgram())) {
				code[i].setProgram(code[i].getSavedProgram());		
				/* Wild Hack: set document to null so the text editor will refresh 
				   the program contents when the document tab is being clicked */
				code[i].setDocument(null);
			}
		}
		
		// this will update the current code
		setCode(sketch.getCurrentCode());
	}
	
	private void removeSpacesFromCode()
	{
		SketchCode[] code = sketch.getCode();
		for (int i=0; i<code.length; i++) {
			String c = code[i].getProgram();
			c = c.substring(TweakMode.SPACE_AMOUNT, c.length() - TweakMode.SPACE_AMOUNT);
			code[i].setProgram(c);
			/* Wild Hack: set document to null so the text editor will refresh
			   the program contents when the document tab is being clicked */
			code[i].setDocument(null);
		}
		// this will update the current code
		setCode(sketch.getCurrentCode());
	}
	
	private String replaceString(String str, int start, int end, String put)
	{
		return str.substring(0, start) + put + str.substring(end, str.length());
	}
}
