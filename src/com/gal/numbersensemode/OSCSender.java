package com.gal.numbersensemode;

import java.util.ArrayList;

import com.illposed.osc.*;

public class OSCSender {
	
	public static void sendFloat(String varName, float val) throws Exception
	{
		OSCPortOut sender = new OSCPortOut();
		ArrayList<Object> args = new ArrayList<Object>();
		args.add(new String(varName));
		args.add(new Float(val));
		OSCMessage msg = new OSCMessage("/numberchange", args);
		 try {
			sender.send(msg);
		 } catch (Exception e) {
			 System.out.println("Couldn't send");
		 }
	}
	
	public static void sendInt(String varName, int val) throws Exception
	{
		OSCPortOut sender = new OSCPortOut();
		ArrayList<Object> args = new ArrayList<Object>();
		args.add(new String(varName));
		args.add(new Integer(val));
		OSCMessage msg = new OSCMessage("/numberchange", args);
		 try {
			sender.send(msg);
		 } catch (Exception e) {
			 System.out.println("Couldn't send");
		 }
	}

}
