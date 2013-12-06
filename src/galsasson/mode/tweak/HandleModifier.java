package galsasson.mode.tweak;

public class HandleModifier {
	public float min;
	public float max;
	public float val;
	
	Handle handle;

	public HandleModifier(Handle handle, float min, float max) {
		this.handle = handle;
		this.min = min;
		this.max = max;
		
		this.val = handle.value.floatValue();
	}
	
	public HandleModifier(Handle handle, float min, float max, float val) {
		this.handle = handle;
		this.min = min;
		this.max = max;
		this.val = val;
	}
	
	public void sendVal()
	{
		handle.setValue(val);
		handle.updateColorBox();
	}
	
	public void receiveVal()
	{
		this.val = handle.newValue.floatValue();
	}
	
	public void interpolateVal(float target, float t)
	{
		float diff = target-val;
		val += diff*t; 
	}
	
	public void interpolateVal(float source, float target, float t)
	{
		float diff = target-source;
		val += diff*t; 
	}
	
	public void mutate(float amount)
	{
		float max = val*amount;
		float d = (float)(Math.random()*max);
		if (Math.random()<0.5) {
			d *= -1;
		}
		
		val += d;
		
		if (val>max) {
			val = max;
		}
		else if (val<min) {
			val = min;
		}
	}
	
	public HandleModifier clone()
	{
		return new HandleModifier(handle, min, max, val);
	}
}
