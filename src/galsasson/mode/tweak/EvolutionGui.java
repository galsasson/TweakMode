package galsasson.mode.tweak;

import galsasson.mode.tweak.ColorSelector.ColorSelectorBox;
import galsasson.mode.tweak.ColorSelector.ColorSelectorSlider;
import galsasson.mode.tweak.ColorSelector.SelectorTopBar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;

import javax.swing.Box;
import javax.swing.JFrame;

import processing.core.PApplet;
import processing.core.PImage;

public class EvolutionGui {
	JFrame frame;
	GuiScreen screen;
	
	EvolutionManager manager;
	
	public EvolutionGui(EvolutionManager mgr)
	{
		manager = mgr;
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
		frame.getContentPane().add(box, BorderLayout.CENTER);
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

	public class GuiScreen extends PApplet
	{
		boolean started = false;
		
		public void setup()
		{
			size(100, 500);
			colorMode(HSB, 360, 100, 100);
			noFill();			
		}
		
		public void draw()
		{
			background(255);
			
			if (!started)
			{
				fill(0);
				rect(20, height/2-20, 60, 40, 5, 5, 5, 5);
				return;
			}
			
			noFill();
			stroke(0);
			strokeWeight(2);
			rect(40, 0, 20, height, 3, 3, 3, 3);
			for (int i=0; i<manager.populationSize-1; i++)
			{
				strokeWeight(1);
				line(40, 10+i*10, 60, 10+i*10);
			}
		}
		
		public void mousePressed()
		{
			if (!started) {
				// handle start button click
				int nextState = (manager.activeState+1)%manager.populationSize;
				manager.setState(nextState);
				started = true;
				return;
			}
			
			// handle sketch score
			int index = (height-mouseY) / manager.populationSize;
			System.out.println("set score: " + index);
		}
	}
}


