package com.baturinsky.tabletop.service;

public interface PartyListener {
	void user(long id);
	void begin(long time);	
	void write(long id, String val);
	void delete(long id);
	void link(long sup, long sub);
	void unlink(long sup, long sub);
	void end();
	void write_named(String name, String val);
}
