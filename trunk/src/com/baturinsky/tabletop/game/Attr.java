package com.baturinsky.tabletop.game;

public class Attr {
	public String val, name;
	public long _id;
	Attr(String name, String val)
	{
		_id = Accessory.indCount++;
		this.val = val;
		this.name = name;
	}
}
