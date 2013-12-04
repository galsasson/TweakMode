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
	
	public void initGui()
	{
		gui = new EvolutionGui(this);
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
			SketchState state = new SketchState(modifiers);
			state.randomize();
			population.add(state);
		}		
	}
	
	public void setState(int state)
	{
		if (state < 0 || state>population.size()-1) {
			return;
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
