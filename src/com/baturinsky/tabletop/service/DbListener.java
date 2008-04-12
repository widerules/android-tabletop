package com.baturinsky.tabletop.service;

import java.util.Date;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class DbListener implements PartyListener {
	SQLiteDatabase db;
	SQLiteStatement sqlLink, sqlDelete, sqlUnlink, sqlWrite, sqlWriteNamed;
	boolean transactionOpened = false;
	
	public DbListener (SQLiteDatabase db){
		this.db = db;
		sqlLink = db.compileStatement("REPLACE INTO links (sup, sub) VALUES (?, ?) ");
		sqlUnlink = db.compileStatement("DELETE FROM links WHERE sup = ? and sub = ?");
		sqlDelete = db.compileStatement("DELETE FROM notes WHERE _id = ?");
		sqlWrite = db.compileStatement("REPLACE INTO notes (_id, value) VALUES (?, ?)");
		sqlWriteNamed = db.compileStatement("INSERT INTO named (name, value) VALUES (?, ?)");
	}
		
	@Override
	public void delete(long id) {
		if(!transactionOpened)
			begin();
		sqlDelete.bindLong(1, id);
		sqlDelete.execute();
	}

	@Override
	public void link(long sup, long sub) {
		if(!transactionOpened)
			begin();
		sqlLink.bindLong(1, sup);
		sqlLink.bindLong(2, sub);
		sqlLink.execute();
	}

	@Override
	public void unlink(long sup, long sub) {
		if(!transactionOpened)
			begin();
		sqlUnlink.bindLong(1, sup);
		sqlUnlink.bindLong(2, sub);
		sqlUnlink.execute();
	}

	@Override
	public void write(long id, String val) {
		if(!transactionOpened)
			begin();
		sqlWrite.bindLong(1, id);
		sqlWrite.bindString(2, val==null?"":val);
		sqlWrite.execute();
		
	}

	@Override
	public void begin(long time) {
		transactionOpened = true;
		db.execSQL("BEGIN");
	}

	public void begin() {
		begin(new Date().getTime());
	}

	@Override
	public void end() {
		if(transactionOpened){
			transactionOpened = false;
			db.execSQL("END");		
		}
	}

	@Override
	public void user(long id) {
	}

	@Override
	public void write_named(String name, String val) {		
		sqlWriteNamed.bindString(1, name);
		sqlWriteNamed.bindString(2, val);
		sqlWriteNamed.execute();
	}
}
