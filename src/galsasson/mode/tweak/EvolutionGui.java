package galsasson.mode.tweak;

import galsasson.mode.tweak.ColorSelector.ColorSelectorBox;
import galsasson.mode.tweak.ColorSelector.ColorSelectorSlider;
import galsasson.mode.tweak.ColorSelector.SelectorTopBar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JFrame;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;

public class EvolutionGui {
	JFrame frame;
	GuiScreen screen;
	
	EvolutionManager manager;
	String dataFolder;
	
	public EvolutionGui(EvolutionManager mgr, String dfolder)
	{
		manager = mgr;
		dataFolder = dfolder;
		createFrame();
	}
	
	public void createFrame()
	{
		frame = new JFrame();
		frame.setBackground(Color.BLACK);
		
		Box box = Box.createHorizontalBox();
		box.setBackground(Color.BLACK);
		
		screen = new GuiScreen();
		screen.init();
		box.add(screen);
		frame.getContentPane().add(box);
		frame.pack();
		frame.setResizable(false);
//		frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
	
	public void show(int x, int y)
	{
		frame.setLocation(x, y);
		frame.setVisible(true);
	}
	
	public void hide()
	{
		frame.setVisible(false);
	}
	
	public void dispose()
	{
//		frame.dispose();
	}
	

	/*
	 * PApplets for the evolution gui
	 */
	public enum GuiState {
		WELCOME, RATE
	}

	public class GuiScreen extends PApplet
	{
		int w=150;
		int h=400;
		PFont font;
		ArrayList<StateCell> states;
		
		Button randomize;
		Button evolve;
		
		public void setup()
		{
			size(w, h);
			noFill();
			try {
				font = new PFont(new FileInputStream(dataFolder + "/Inconsolata-22.vlw"));
			}
			catch (Exception e) {}
			
			states = new ArrayList<StateCell>();
			int y=30;
			for (SketchState ss : manager.population)
			{
				states.add(new StateCell(ss, 0, y, w, 45,font));
				y+=45;
			}
			
			// create buttons
			randomize = new Button(0, h-60, w, 25, "Randomize", font);
			evolve = new Button(0, h-30, w, 25, "Evolve", font);
		}
		
		public void draw()
		{
			background(230);
			
			
			// draw heading
			noStroke();
			fill(50);
			textFont(font);
			textAlign(CENTER, CENTER);
			text("Code Evolver", w/2, 15);
			noFill();
			stroke(50);
			line(0, 30, w, 30);
			
			// draw states
			for (StateCell sc : states)
			{
				sc.draw();
			}
			
			// draw buttons
			randomize.draw();
			evolve.draw();
		}		
		
		public void mousePressed()
		{
			for (int i=0; i<states.size(); i++)
			{
				StateCell sc = states.get(i);
				if (sc.contains(mouseX, mouseY))
				{
					// set i as the active state
					manager.setState(i);
					
					// send the click to this state
					sc.handleClick(mouseX, mouseY);
					return;
				}
			}
			
			if (randomize.contains(mouseX, mouseY)) {
				manager.randomize();
				return;
			}
			
			if (evolve.contains(mouseX, mouseY)) {
				System.out.println("evolve");
				return;
			}
		}
		
		public Dimension getPreferredSize() {
			return new Dimension(w, h);
		}

		public Dimension getMinimumSize() {
			return new Dimension(w, h);
		}

		public Dimension getMaximumSize() {
			return new Dimension(w, h);
		}
		
		
		/* State cell */
		public class StateCell
		{
			PVector pos;
			PVector size;
			SketchState state;
			PFont font;
			PShape star;
			
			public StateCell(SketchState state, float x, float y, float w, float h, PFont font)
			{
				this.state = state;
				pos = new PVector(x, y);
				size = new PVector(w, h);
				this.font = font;
			}
			
			public void draw()
			{
				pushMatrix();
				translate(pos.x, pos.y);
				
				// draw ellipse
				if (state.id == manager.activeState) {
					// draw bright background
					noStroke();
					fill(255);
					rect(2, 2, size.x-4, size.y-4);
					fill(20);
				}
				else {
					noFill();
					stroke(20);
				}
				ellipse(10, size.y/4, 8, 8);
				
				// draw contour
				stroke(50);
				noFill();
				rect(0, 0, size.x-1, size.y-1);

				noStroke();
				fill(20);
				textAlign(LEFT, CENTER);
				textFont(font, 18);
				text(state.name, 20, size.y/4);
				
				// draw five stars (boxes for now)
				stroke(50);
				noFill();
				pushMatrix();
				translate(28, size.y*3/4-3);
				for (int i=0; i<5; i++)
				{
					if (state.score > i) {
						fill(245, 171, 22);
					}
					else {
						noFill();
					}
					rect(-8, -8, 16, 16);
					
					translate(22, 0);
				}
				popMatrix();
				
				popMatrix();
			}
			
			public boolean contains(int x, int y)
			{
				if (x>=pos.x && x<pos.x+size.x &&
					y>=pos.y && y<pos.y+size.y) {
					return true;
				}
				
				return false;
			}
			
			public void handleClick(int x, int y)
			{
				// check if the user is rating this state
				PVector p = new PVector(pos.x+20, pos.y+size.y*3/4-11);
				PVector s = new PVector(16, 16);
				for (int i=0; i<5; i++)
				{
					if (x > p.x && x < p.x + s.x &&
						y > p.y && y < p.y + s.y)
					{
						// we have a rating
						state.score = i+1;
					}
					p.add(22, 0, 0);
				}
			}
		}
		
		public class Button
		{
			PVector pos;
			PVector size;
			String caption;
			PFont font;
			
			public Button(float x, float y, float w, float h, String cap, PFont font)
			{
				pos = new PVector(x, y);
				size = new PVector(w, h);
				caption = cap;
				this.font = font;
			}
			
			public void draw()
			{
				stroke(50);
				noFill();
				rect(pos.x+2, pos.y+2, size.x-4, size.y-4, 3, 3, 3, 3);
				
				noStroke();
				fill(50);
				textAlign(CENTER, CENTER);
				textFont(font, 18);
				text(caption, pos.x+size.x/2, pos.y+size.y/2);
			}
			
			public boolean contains(int x, int y)
			{
				if (x>pos.x && x<pos.x+size.x &&
					y>pos.y && y<pos.y+size.y)
				{
					return true;
				}
				
				return false;
			}
		}
	}
}


