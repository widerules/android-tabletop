package com.baturinsky.tabletop.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.baturinsky.tabletop.R;
import com.baturinsky.tabletop.service.DbListener;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.util.Log;

public class Table extends View {

	final static String TAG = "v";

	private static final String LAST_URI = "/data/data/com.baturinsky.tabletop/lasturi.txt";

	private Paint mPaint;
	private Bitmap mBitmap;
	private Canvas mCanvas;

	private Visible selected = null;
	private Visible game = null;	
	private Manager manager;
	
	SQLiteDatabase db = null;
	
	public Table(Context cx) {
		super(cx);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.BLACK);

		selected = null;

		manager = new Manager(getContext(), "manager");
		manager.logging = true;
		
		//manager.setPartyListener(new PartyListenerLog());
		load(null, "raw:chess#initial");
	}
	
	void load(String dbName, String init){
		closeDb();
		if(dbName == null)
			dbName = lastUri();
		saveLastUri(dbName);
		
		if(init != null){
			String[] suri = init.split("#");
			if(suri.length == 2)
			{
				Accessory pack = manager.parseFile(suri[0]);
				Visible position = (Visible) pack.locate(suri[1]);
				
				openDb(dbName);
				
				game = new Visible();
				game.copy(position);
				game.into(manager);
			}
		} else {
			openDb(dbName);
		}
	}
	
	void openDb(String uri){
		closeDb();
		File file = new File(uri);
		if(file.exists())
			db = SQLiteDatabase.open(file, null);
		else
		{
			db = SQLiteDatabase.create(file, 1, null);
			String dbinit = getContext().getResources().getString(R.raw.dbinit);
			for(String s:dbinit.split(";")){
				db.execSQL(s);
			}
		}
		manager.setPartyListener(new DbListener(db));
	}
	
	void closeDb(){
		if(db!=null)
		{
			db.close();
			db = null;
		}
	}

	protected void onDraw(Canvas canvas) {
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Bitmap newBitmap = Bitmap.createBitmap(w, h, false);
		Canvas newCanvas = new Canvas();
		newCanvas.setDevice(newBitmap);

		mBitmap = newBitmap;
		mCanvas = newCanvas;

		redrawTable();
	}

	protected void redrawTable() {
		if (mCanvas == null)
			return;

		mCanvas.drawARGB(255, 0, 128, 0);
		game.calculateCtm();
		game.draw(mCanvas);

		invalidate();
	}

	boolean silentUp = false;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float[] place = new float[] { event.getX(), event.getY(), 0 };
		Visible pieceUnder = game.at(place);

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (selected != null) {
				if (pieceUnder != selected) {
					if (pieceUnder != null)
						pieceUnder.drop(selected, place);
				}
				selected.setFocus(false);
				selected = null;
				redrawTable();
				silentUp = true;
			}
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (silentUp) {
				silentUp = false;
				return true;
			}
			if (selected == null) {
				if (pieceUnder != null && !pieceUnder.flag(Visible.STATIC)) {
					selected = pieceUnder;
					selected.setFocus(true);
					redrawTable();
				}
			}
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
		}
		return true;
	}

	String lastUri(){
		FileInputStream stream = null;
		try {
			stream = getContext().openFileInput(LAST_URI);
			byte[] b = new byte[200];
			stream.read(b);
			stream.close();
			return new String(b);
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
			Toast.makeText(getContext(), "Can't recall last opened party - opening default", Toast.LENGTH_SHORT).show();
			return "/data/data/com.baturinsky.tabletop/databases/newgame.db";
		}
	}
	
	void saveLastUri(String uri){
		try {
			FileOutputStream stream = getContext().openFileOutput(LAST_URI, Context.MODE_PRIVATE);
			stream.write(uri.getBytes());
			stream.close();
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
			e.printStackTrace();
			Toast.makeText(getContext(), "Failed to remember last opened party", Toast.LENGTH_SHORT).show();
		}
	}
}