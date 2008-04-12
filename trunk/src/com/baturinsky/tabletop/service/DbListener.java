package com.baturinsky.tabletop.service;

import java.util.Date;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class DbListener implements PartyListener {
	SQLiteDatabase db;
	SQLiteStatement sql, sqlLink;
	boolean transactionOpened = false;
	
	public DbListener (SQLiteDatabase db){
		this.db = db;
		sqlLink = db.compileStatement("INSERT INTO links (sup, sub) VALUES ?, ? ");
	}

	@Override
	public void erace(long id) {
		if(!transactionOpened)
			begin();
		sql = db.compileStatement("DELETE FROM users where _id = ?");
		sql.bindLong(1, id);
		sql.execute();
	}

	@Override
	public void link(long sup, long sub) {
		if(!transactionOpened)
			begin();
		sqlLink = db.compileStatement("INSERT INTO links (sup, sub) VALUES ?, ? ");
		sqlLink.bindLong(1, sup);
		sqlLink.bindLong(2, sub);
		sqlLink.execute();
	}

	@Override
	public void unlink(long sup, long sub) {
		if(!transactionOpened)
			begin();
		sql = db.compileStatement("DELETE FROM links where sup = ? and sub = ?");
		sql.bindLong(1, sup);
		sql.bindLong(2, sub);
		sql.execute();
	}

	@Override
	public void write(long id, String val) {
		if(!transactionOpened)
			begin();
		sql = db.compileStatement("REPLACE INTO notes (_id, val) VALUES ?, ?");
		sql.bindLong(1, id);
		sql.bindString(2, val);
		sql.execute();
		
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
		sql = db.compileStatement("INSERT into nameds (name, val) VALUES ?, ?");
		sql.bindString(1, name);
		sql.bindString(2, val);
		sql.execute();
	}
}
