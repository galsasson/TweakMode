package galsasson.mode.tweak;

import java.util.ArrayList;

public class SketchState {
	ArrayList<HandleModifier> modifiers;
	int score;
	
	public SketchState(ArrayList<HandleModifier> modifiers)
	{
		this.modifiers = new ArrayList<HandleModifier>();
		
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
