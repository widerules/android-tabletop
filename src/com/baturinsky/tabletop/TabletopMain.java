package com.baturinsky.tabletop;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.baturinsky.tabletop.game.Table;

public class TabletopMain extends Activity {
	public static final String TAG = "Tabletop";

	@Override
	public void onCreate(Bundle icicle) {
		Log.i(TAG, "Starting");
		super.onCreate(icicle);
		setContentView(new Table(this));
	}

}