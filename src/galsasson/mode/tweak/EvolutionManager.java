package galsasson.mode.tweak;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class EvolutionManager {
	ArrayList<HandleModifier> modifiers;
	ArrayList<SketchState> population;
	int populationSize;
	
	TweakTextAreaPainter painter;
	EvolutionGui gui;
	
	int activeState;

	public EvolutionManager(ArrayList<HandleModifier> modifiers, TweakTextAreaPainter painter, int size)
	{
		this.modifiers = modifiers;
		this.painter = painter;
		this.populationSize = size;
		
		initPopulation();
		activeState = 0;
	}
	
	public void initGui(String modeFolder)
	{
		String dataFolder = modeFolder + "/data";
		gui = new EvolutionGui(this, dataFolder);
		gui.frame.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	        	gui.frame.setVisible(false);
	        	gui = null;
	        }
	      });

		gui.show(0, 0);		
	}
	
	public void initPopulation()
	{
		population = new ArrayList<SketchState>();
		
		for (int i=0; i<populationSize; i++)
		{
			SketchState state = new SketchState(modifiers, "State " + Integer.toString(i), i);
			population.add(state);
		}		
	}
	
	public void randomize()
	{
		for (SketchState ss : population)
		{
			ss.randomize();
		}
		
		setState(activeState, false);
	}
	
	public void setState(int state, boolean updateCurrent)
	{
		if (state < 0 || state>population.size()-1) {
			return;
		}
		
		// before changing the state, make sure to update the value of the
		// current state to match the code.
		// (in case the user manipulated the code handles)
		if (updateCurrent) {
			population.get(activeState).receiveValues();
		}
		
		activeState = state;
		
		// TODO: lerp here from current state to the other
		population.get(state).sendValues();
		
		// update the code text and repaint
		painter.updateCodeText();
		painter.repaint();
	}
	
	public void dispose()
	{
		gui.hide();
		gui.dispose();
	}
}
