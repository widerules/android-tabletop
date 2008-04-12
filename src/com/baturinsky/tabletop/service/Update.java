package com.baturinsky.tabletop.service;

import java.nio.ByteBuffer;

import android.content.Context;
import android.util.Log;

public class Update implements PartyListener
{
	ByteBuffer data;
	
	private static final char 
	USER = '@', 
	COME = '>',
	LEAVE = '<',
	SHOW = '!',
	HIDE = '?',
	WRITE = 'w',
	ERACE = 'e',
	LINK = '=',
	UNLINK = '~',
	BEGIN = ':',
	END = '\0';
	
	private static final long VERSION = 1;

	static final String TAG = "Update";
	
	Context context;
	
	Update(int capacity, long user){
		data = ByteBuffer.allocate(capacity);
		data.putLong(VERSION);
		data.putLong(user);
	}
	
	Update(ByteBuffer data, int limit){
		this.data = data;
		data.limit(limit);		
	}
	
	Update(byte[] data){
		this.data = ByteBuffer.wrap(data);
		this.data.limit(data.length);
	}

	void putString(String s){
		putBytes(s.getBytes());
	}

	String getString(){
		return new String(getBytes());
	}
	
	void putBytes(byte[] bytes){
		data.putInt(bytes.length);
		data.put(bytes, 0, bytes.length);
	}
	
	byte[] getBytes(){
		int size = data.getInt();
		byte[] bytes = new byte[size]; 
		data.get(bytes);
		return bytes;
	}	

	@Override
	public void write(long id, String val){
		data.putChar(WRITE);
		data.putLong(id);
		putString(val);
	}
	
	@Override
	public void erace(long id){
		data.putChar(ERACE);
		data.putLong(id);
	}

	@Override
	public void link(long sup, long sub){
		data.putChar(LINK);
		data.putLong(sup);
		data.putLong(sub);
	}

	@Override
	public void unlink(long sup, long sub){
		data.putChar(UNLINK);
		data.putLong(sup);
		data.putLong(sub);
	}
	
	@Override
	public void begin(long time){
		data.putChar(BEGIN);
		data.putLong(time);
	}

	@Override
	public void end() {
		data.putChar(END);		
	}	
	
	@Override
	public void user(long id) {
		data.putChar(USER);
		data.putLong(id);
	}
	

	void apply(PartyListener l)
	{
		long version, user, id;
		char tag;
		data.position(0);
		version = data.getLong();
		user = data.getLong();
		Log.i(TAG, "Reading update ver. " + version + " of user " + user);
		if(version != VERSION){
			Log.e(TAG, "Wrong update version");
			return;
		}
		while(data.hasRemaining()){
			tag = data.getChar();
			switch(tag){
			/*case COME:
				l.come(data.getLong(), getString());
				break;
			case LEAVE:
				id = data.getLong();
				l.leave(id);
				break;
			case SHOW:
				l.show(data.getLong(),data.getLong());
				break;
			case HIDE:
				l.hide(data.getLong(), data.getLong());
				break;*/
			case WRITE:
				l.write(data.getLong(), getString());
				break;
			case ERACE:
				l.erace(data.getLong());
				break;
			case LINK:
				l.link(data.getLong(), data.getLong());
				break;
			case UNLINK:
				l.unlink(data.getLong(), data.getLong());
				break;
			case BEGIN:
				l.begin(data.getLong());
				break;
			case END:
				l.end();
				break;
			case USER:
				l.user(data.getLong());
				break;
			default:
				Log.e(TAG, "Wrong tag " + tag + " at " + data.position());
				return;
			}
		}
	}

	@Override
	public void write_named(String name, String val) {
		// TODO Auto-generated method stub		
	}
}
