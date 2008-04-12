package com.baturinsky.tabletop.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.baturinsky.tabletop.R;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class Party {
	SQLiteDatabase db;	
	String name;
		
	static public String dbName(String account, String roomName){
		return account + "_" + roomName + ".db";
	}
	
	protected Party()
	{
	}
	
	public Party(File f) throws Exception
	{
		db = SQLiteDatabase.open(f, null);
		if(db == null)
			throw new Exception("No such party");
	}
	
	static public Party create(File f, Context c)
	{		
		Party p = new Party();
		try {
			p.name = f.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try{
			if(f.exists()){
				f.delete();
			}
			p.db = SQLiteDatabase.create(f, 1, null);
			String dbinit = c.getResources().getString(R.raw.dbinit);
			for(String s:dbinit.split(";")){
				p.db.execSQL(s);
			}
			
		} catch(SQLException e){
			e.printStackTrace();
			f.delete();
			throw e;
		}
		return p;
	}
	

	public SQLiteDatabase getDb(){
		return db;
	}
		
	public String getOptionValue(String name){
		Cursor c = db.query("options", new String[]{"value"}, "name = ?", new String[]{name}, "_id", null, null);
		if(!c.next())
			return null;
		return c.getString(0);
	}
	
	public String[] getOptionValues(String name){
		Cursor c = db.query("options", new String[]{"value"}, "name = ?", new String[]{name}, "_id", null, null);
		List<String> sl = new ArrayList<String>();
		while(c.next())
			sl.add(c.getString(0));			
		return sl.toArray(new String[c.count()]);
	}

	public Cursor getOptions(){
		Cursor c = db.query("options", new String[]{"_id", "name", "value"}, null, null, null, null, null);
		return c;
	}

	public void setOption(String name, String value){
		SQLiteStatement sql = db.compileStatement("INSERT INTO options (name, value) VALUES( ?, ? )");
		sql.bindString(1, name);
		sql.bindString(2, value);
		sql.execute();
	}

	public void resetOption(String name, String value){
		db.execSQL("BEGIN");
		SQLiteStatement sql = db.compileStatement("DELETE FROM options WHERE name = ?");
		sql.bindString(1, name);					
		sql.execute();
		sql = db.compileStatement("INSERT INTO options (name, value) VALUES( ?, ? )");
		sql.bindString(1, name);
		sql.bindString(2, value);
		sql.execute();
		db.execSQL("COMMIT");
	}

}
