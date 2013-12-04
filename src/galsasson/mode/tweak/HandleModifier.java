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
	
	public void sendVal()
	{
		handle.setValue(val);
		handle.updateColorBox();
	}
	
	public HandleModifier clone()
	{
		return new HandleModifier(handle, min, max);
	}
}
