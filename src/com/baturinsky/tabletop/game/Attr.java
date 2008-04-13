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
	
	Attr(long _id, String name, String val)
	{
		this._id = _id;
		this.value = val;
		this.name = name;
		if(Accessory.indCount<=_id)
			Accessory.indCount = _id+1;		
	}
}
