package com.gal.numbersensemode;

public class Number {
	public String type;
	public String name;
	public String value;
	int tabIndex;
	int startChar, endChar;
	
	public Number()
	{
		this("int", "numbersense_noname", "0", 0, 0, 0);
	}
	
	public Number(String t, String n, String v, int ti, int sc, int ec)
	{
		type = t;
		name = n;
		value = v;
		tabIndex = ti;
		startChar = sc;
		endChar = ec;
	}	
}
