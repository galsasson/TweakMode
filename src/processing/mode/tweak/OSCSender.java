package processing.mode.tweak;

import java.util.ArrayList;

import com.illposed.osc.*;

public class OSCSender {
	
	public static void sendFloat(int index, float val) throws Exception
	{
		OSCPortOut sender = new OSCPortOut();
		ArrayList<Object> args = new ArrayList<Object>();
		args.add(new Integer(index));
		args.add(new Float(val));
		OSCMessage msg = new OSCMessage("/tm_change_float", args);
		 try {
			sender.send(msg);
		 } catch (Exception e) {
			 System.out.println("Couldn't send");
		 }
	}
	
	public static void sendInt(int index, int val) throws Exception
	{
		OSCPortOut sender = new OSCPortOut();
		ArrayList<Object> args = new ArrayList<Object>();
		args.add(new Integer(index));
		args.add(new Integer(val));
		OSCMessage msg = new OSCMessage("/tm_change_int", args);
		 try {
			sender.send(msg);
		 } catch (Exception e) {
			 System.out.println("Couldn't send");
		 }
	}

}
