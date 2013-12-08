package galsasson.mode.tweak;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import processing.app.Base;
import processing.app.Sketch;

public class EvolutionManager {
	final boolean DEBUG = false;
	
	Sketch sketch;
	ArrayList<HandleModifier> modifiers;
	ArrayList<SketchState> population;
	int populationSize;
	
	TweakTextAreaPainter painter;
	EvolutionGui gui;
	
	int ids;
	
	int activeState;

	public EvolutionManager(Sketch sketch, ArrayList<HandleModifier> modifiers, TweakTextAreaPainter painter, int size)
	{
		this.sketch = sketch;
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
			SketchState state = new SketchState(modifiers, ids);
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
		// make sure to update the value of the current state to match the code.
		// (in case the user manipulated the code handles before pressing evolve)
		population.get(activeState).receiveValues();
		
//		evolveAverage();
		evolveTheBest();
	}
	
	public void evolveTheBest()
	{
		// pick the best score (multiple if more than one sketch with the same score)
		ArrayList<SketchState> bestPool = new ArrayList<SketchState>();
		int maxScore = population.get(0).score;
		for (SketchState state : population)
		{
			if (state.score == maxScore) {
				bestPool.add(state);
			}
			else if (state.score > maxScore) {
				bestPool.clear();
				maxScore = state.score;
				bestPool.add(state);
			}
		}
		
		if (DEBUG)
		{
			System.out.println("best: bestPool size = " + bestPool.size());
			for (SketchState state : bestPool)
			{
				System.out.println("      - " + state.id);
			}
		}
		
		ArrayList<SketchState> selectionPool = new ArrayList<SketchState>();
		// add states to the pool according to score (fitness)
		for (SketchState ss : population) {
			boolean add = true;
			// set add to false if sketch state is one of the best
			for (SketchState best : bestPool) {
				if (ss == best) {
					add = false;
					break;
				}
			}
			
			if (add) {
				for (int i=0; i<ss.score; i++) {
					selectionPool.add(ss);
				}
			}
		}
		
		if (DEBUG)
		{
			System.out.println("best: selectionPool size = " + selectionPool.size());
			for (SketchState state : selectionPool)
			{
				System.out.println("      - " + state.id);
			}
		}
		
		// create children for each one of the best scores
		ArrayList<SketchState> newPopulation = new ArrayList<SketchState>();
		int nChild=population.size()/bestPool.size();
		int reminder = population.size()%bestPool.size();
		for (int i=0; i<bestPool.size(); i++)
		{
			// calc how many childs for each best sketch
			int n = nChild;
			if (reminder > 0)
			{
				n++;
				reminder--;
			}

			// mate this best state with a random state
			SketchState s1 = bestPool.get(i);
			for (int s=0; s<n; s++)
			{
				SketchState s2 = selectionPool.get((int)(Math.random()*selectionPool.size()));
				// crossover with random interpolation coeff between 0.25-0.75
				SketchState newState = s1.crossoverMerge(s2, 0.25f+(float)Math.random()*0.5f);
				newState.name = "version_" + Integer.toString(ids);
				newState.id = ids++;
				
				// mutation
				newState.mutate(0.1f, 0.25f);
				
				// add to new population
				newPopulation.add(newState);
			}
		}
		
		// replace the current population
		population = newPopulation;
		 
		// update the gui&code
		setState(activeState, false);
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
			newState.name = "version_" + Integer.toString(ids);
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
	
	public void exportIndex(int index)
	{
		if (index<0 || index>=population.size()) {
			return;
		}
		
		String sketchName = sketch.getName();
		String versionName = population.get(index).name;
		
		// show file chooser for the user to choose a directory
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Save '" + versionName +"' in");
		int ret = fc.showOpenDialog(gui.frame);
		
		if (ret == JFileChooser.APPROVE_OPTION) {
			try {
				File targetDir = fc.getSelectedFile();
				System.out.println("saving in: " + targetDir);
				
				// create the target sketch directory
				String name = sketch.getName() + "_" + versionName;
				targetDir = new File(targetDir, name);
				if (!targetDir.mkdir()) {
					Base.showWarning("Save failed", "Cannot create directory "+targetDir+"\nTry saving in a different directory.", null);
					return;
				}
				
				// copy the data folder from the sketch (if exists)
				File targetDataDir = new File(targetDir, "data");
				if (sketch.getDataFolder().exists()) {
					Base.copyDir(sketch.getDataFolder(), targetDataDir);
				}
				
				// save all code tabs into files
				// save the first tab (name should be changed)
				File dest = new File(targetDir, name + ".pde");
				Base.saveFile(sketch.getCode(0).getProgram(), dest);
				
				// save the rest of the tabs
				for (int i=1; i<sketch.getCodeCount(); i++)
				{
					dest = new File(targetDir, sketch.getCode(i).getFileName());
					Base.saveFile(sketch.getCode(i).getProgram(), dest);
				}
				
				Base.showMessage("State Saved!", versionName + " was saved in " + targetDir);
			}
			catch (Exception ex)
			{
				Base.showWarning("Save failed", "There was an error and the version did not save!", null);
			}
		}
	}
	
	public void dispose()
	{
		gui.hide();
		gui.dispose();
	}
}
