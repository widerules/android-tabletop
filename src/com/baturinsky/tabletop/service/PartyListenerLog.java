package com.baturinsky.tabletop.service;

import java.util.Date;

import android.util.Log;

public class PartyListenerLog implements PartyListener{

	final static String TAG = "pl log";
	
	@Override
	public void begin(long time) {
		Log.i(TAG, " BEGIN " + new Date(time).toString());		
	}

	@Override
	public void end() {
		Log.i(TAG, " END ");
	}

	@Override
	public void delete(long id) {
		Log.i(TAG, id + " RIP ");		
	}

	@Override
	public void link(long sup, long sub) {
		Log.i(TAG, sup + " -> " + sub);		
	}

	@Override
	public void unlink(long sup, long sub) {
		Log.i(TAG, sup + " -X-> " + sub);		
	}

	@Override
	public void user(long id) {
		 Log.i(TAG, " USER " + id);		
	}

	@Override
	public void write(long id, String val) {
		Log.i(TAG, id + " := " + val);
	}

	@Override
	public void write_named(String name, String val) {
		Log.i(TAG, name + " := " + val);		
	}

}
