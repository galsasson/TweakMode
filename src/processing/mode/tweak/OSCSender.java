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
			 System.out.println("Couldn't send new value of float " + index);
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
			 System.out.println("Couldn't send new value of int " + index);
			 System.out.println(e.toString());
		 }
	}

	public static void sendLong(int index, long val) throws Exception
	{
		OSCPortOut sender = new OSCPortOut();
		ArrayList<Object> args = new ArrayList<Object>();
		args.add(new Integer(index));
		args.add(new Long(val));
		OSCMessage msg = new OSCMessage("/tm_change_long", args);
		 try {
			sender.send(msg);
		 } catch (Exception e) {
			 System.out.println("Couldn't send new value of long " + index);
			 System.out.println(e.toString());
		 }
	}
}
