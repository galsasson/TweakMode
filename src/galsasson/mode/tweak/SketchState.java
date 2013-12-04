package galsasson.mode.tweak;

import java.util.ArrayList;

public class SketchState {
	ArrayList<HandleModifier> modifiers;
	String name;
	int score;
	int id;
	
	public SketchState(ArrayList<HandleModifier> modifiers, String name, int id)
	{
		this.modifiers = new ArrayList<HandleModifier>();
		this.name = name;
		this.id = id;
		
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
}
