package galsasson.mode.tweak;

import java.util.ArrayList;

public class SketchState {
	ArrayList<HandleModifier> modifiers;
	String name;
	int score;
	int id;
	
	public SketchState(ArrayList<HandleModifier> modifiers, int id)
	{
		this.modifiers = new ArrayList<HandleModifier>();
		this.name = "version_"+Integer.toString(id);
		this.id = id;
		score = 1;
		for (HandleModifier hm : modifiers)
		{
			this.modifiers.add(hm.clone());
		}
	}

	public void randomize()
	{
		for (HandleModifier hm : modifiers)
		{
			float range = hm.max - hm.min;
			hm.val = hm.min + (float)Math.random()*range;
		}
	}
	
	public void sendValues()
	{
		for (HandleModifier hm : modifiers)
		{
			hm.sendVal();
		}				
	}
	
	public void receiveValues()
	{
		for (HandleModifier hm : modifiers)
		{
			hm.receiveVal();
		}
	}
	
	public SketchState crossoverMerge(SketchState s2, float s2Coeff)
	{
		SketchState newState = new SketchState(modifiers, id+s2.id);
		
		for (int i=0; i<newState.modifiers.size(); i++)
		{
			float source = modifiers.get(i).val;
			float target = s2.modifiers.get(i).val;
			// merge with s2 value with amount s2Coeff (0: only s1, 1: only s2)
			newState.modifiers.get(i).interpolateVal(source, target, s2Coeff);
		}
		
		return newState;
	}
	
	public void mutate(float chance, float amount)
	{
		for (HandleModifier hm: modifiers)
		{
			if (Math.random() < chance)
			{
				hm.mutate(amount);
			}
		}
	}
}
