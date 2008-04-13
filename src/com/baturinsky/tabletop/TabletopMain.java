package com.baturinsky.tabletop;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.baturinsky.tabletop.game.Table;

public class TabletopMain extends Activity {
	public static final String TAG = "Tabletop";
	private static final int INVITE_FRIEND = 1;
	private static final int OTHER_CHESS = 2;
	private static final int OTHER_DRAUGHTS = 3;
	private static final int LICENSE = 4;

	Table table;

	@Override
	public void onCreate(Bundle icicle) {
		Log.i(TAG, "Starting");
		super.onCreate(icicle);
		table = new Table(this);
		setContentView(table);
		// table.load("x.db", "raw:chess#initial");
		// table.load(null, "raw:chess#initial");
		table.load(null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, INVITE_FRIEND, R.string.invite_friend)
				.setShortcut('1', 'a');
		menu.add(1, OTHER_CHESS, R.string.play_chess).setShortcut('2', 'c');
		menu.add(2, OTHER_DRAUGHTS, R.string.play_draughts).setShortcut('3',
				'd');
		menu.add(3, LICENSE, R.string.show_license).setShortcut('4', 'l');
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(Menu.Item item) {
		switch (item.getId()) {
		case INVITE_FRIEND:
			AlertDialog.show(this, null, 0,
					getString(R.string.invite_friend_wip),
					getString(R.string.ok), false);
			return true;
		case OTHER_CHESS:
			table.load(null, "raw:chess#initial");
			return true;
		case OTHER_DRAUGHTS:
			table.load(null, "raw:draughts#draughts_initial");
			return true;
		case LICENSE:
			AlertDialog.show(this, null, 0, getString(R.string.license),
					getString(R.string.ok), false);
			return true;
		}
		return false;
	}

}