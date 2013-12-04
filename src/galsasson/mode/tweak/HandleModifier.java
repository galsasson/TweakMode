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
		
		this.val = min;
	}
	
	public void sendVal()
	{
		handle.setValue(val);		
	}
	
	public HandleModifier clone()
	{
		return new HandleModifier(handle, min, max);
	}
}
