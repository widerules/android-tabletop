package com.baturinsky.tabletop.game;

public class Attr {
	public String value, name;
	public long _id;
	Attr(String name, String val)
	{
		_id = Accessory.indCount++;
		this.value = val;
		this.name = name;
	}
}
