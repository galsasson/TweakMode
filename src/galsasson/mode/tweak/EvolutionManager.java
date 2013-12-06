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
	
	int ids;
	
	int activeState;

	public EvolutionManager(ArrayList<HandleModifier> modifiers, TweakTextAreaPainter painter, int size)
	{
		this.modifiers = modifiers;
		this.painter = painter;
		this.populationSize = size;
		ids = 0;
		
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
			SketchState state = new SketchState(modifiers, "State " + Integer.toString(ids), ids);
			population.add(state);
			ids++;
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
	
	public void randomizeIndex(int index)
	{
		if (index<0 || index>=population.size()) {
			return;
		}
		
		population.get(index).randomize();
		
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
	
	
	public void evolve()
	{
		evolveAverage();
	}
	
	public void evolveAverage()
	{
		ArrayList<SketchState> selectionPool = new ArrayList<SketchState>();
		
		// add states to the pool according to score (fitness)
		for (SketchState ss : population) {
			for (int i=0; i<ss.score; i++) {
				selectionPool.add(ss);
			}
		}
		
		ArrayList<SketchState> newPopulation = new ArrayList<SketchState>();
		for (int i=0; i<populationSize; i++)
		{
			// randomly select two candidates and mate them
			
			// crossover
			SketchState s1 = selectionPool.get((int)(Math.random()*selectionPool.size()));
			// avoid mating with oneself
			SketchState s2;
			do {
				s2 = selectionPool.get((int)(Math.random()*selectionPool.size()));				
			} while(s2.id == s1.id);
			
			SketchState newState = s1.crossoverMerge(s2, 0.5f);
			newState.name = "State " + Integer.toString(ids);
			newState.id = ids++;
			
			// mutation
			newState.mutate(0.1f, 0.25f);
			
			// add to new population
			newPopulation.add(newState);
		}
		
		// replace the current population
		population = newPopulation;
		 
		// update the gui&code
		setState(activeState, false);
		
	}
	
	public void dispose()
	{
		gui.hide();
		gui.dispose();
	}
}
