package com.baturinsky.tabletop.browser;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.baturinsky.tabletop.R;
import com.baturinsky.tabletop.service.Party;

public class PartyBrowser extends Activity {
	private static final int UPDATE = 1;
	private static final int INSERT = 2;
	private static final int DELETE = 3;
	private static final int QUERY = 4;
	private static final int ROOT = 5;
	Party party;
	final String master = "sqlite_master";
	String table = master;
	String[] names;
	Cursor cursor;
	ListView list;
	final public String TAG = "browser";
	boolean root;

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Uri uri = getIntent().getData();
        try {
			party = new Party(new File(uri.toString()));
		} catch (Exception e) {
			Toast.makeText(this, "Party is corrupt or not existant", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
        root = true;
		view();
	}
	
	void view()
	{		
		SQLiteDatabase db = party.getDb();

        if(root){
        	cursor = db.query(master, new String[]{"name"}, "type='table'", null, null, null, null);
        } else {
        	cursor = db.query(table, null, null, null, null, null, null);
        }
    	setTitle(table + ":" + cursor.count() + " fields");

        setContentView(R.layout.browser);
		names = cursor.getColumnNames();
		list = (ListView)findViewById(R.id.list);
		ListAdapter ca = new BaseAdapter(){

			@Override
			public int getCount() {
				return cursor.count();
			}

			@Override
			public Object getItem(int position) {
				cursor.moveTo(position);
				return cursor;
			}

			@Override
			public long getItemId(int position) {
				return (long)position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TableLayout tl = new TableLayout(parent.getContext());
				cursor.moveTo(position);
				for(int i=0; i<names.length;i++){
					TableRow tr = new TableRow(parent.getContext());
					TextView tvn = (TextView)ViewInflate.from(parent.getContext()).inflate(R.layout.small_white_text,null,null);
					tvn.setText(names[i]);
					tr.addView(tvn, new TableRow.LayoutParams());
					TextView tvv = (TextView)ViewInflate.from(parent.getContext()).inflate(R.layout.small_white_text,null,null);
					tvv.setText(cursor.getString(i));
					tr.addView(tvv, new TableRow.LayoutParams());
					tl.addView(tr, new TableLayout.LayoutParams());
				}
								
				return tl;
			}			
		};
		list.setAdapter(ca);
		list.setOnItemClickListener(new OnItemClickListener(){
			@SuppressWarnings("unchecked")
			public void onItemClick(AdapterView parent, View v, int position, long id) {
				cursor.moveTo(position);
				if(root){
					table = cursor.getString(0);
					root = false;
					view();
					return;
				}
				Intent i = new Intent("com.baturinsky.tabletop.EDIT");
				i.setClass(parent.getContext(), FieldsEdit.class);
				Bundle b = new Bundle();
				StringBuffer order = new StringBuffer();
				for(int ind=0; ind<names.length;ind++){
					b.putString(names[ind], cursor.getString(ind));
					order.append(names[ind]+ "\n");
				}
				i.putExtra("values", b);
				i.putExtra("names", order.toString());
				startSubActivity(i, UPDATE);
			}			
		});

		list.requestFocus();
	}

    protected void onActivityResult(int requestCode, int resultCode,
            String data, Bundle fields) {
    	if(resultCode == RESULT_OK){
	        String[] names = data.split("\n");
	        StringBuffer sb= new StringBuffer(); 
	        for(String name:names){
	        	sb.append(name + " = " + fields.getString(name) + "; ");
	        }
	        Toast.makeText(this, sb, Toast.LENGTH_SHORT).show();
    		String id = fields.getString("_id");
    		SQLiteDatabase db = party.getDb();
    		ContentValues cv = new ContentValues();
    		for(String name:names){
    			cv.put(name, fields.getString(name));
    		}
	        switch(requestCode){
	        	case UPDATE:
	        		db.update(table, cv, "_id=?", new String[]{id});
	        		view();
	        		break;
	        	case INSERT:
	        		cv.remove("_id");
	        		db.insert(table, "", cv);
	        		view();
	        	break;
	        }
    	}
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT, R.string.insert).setShortcut('1', 'i');
        menu.add(1, DELETE, R.string.delete).setShortcut('2', 'd');
        menu.add(2, QUERY, R.string.query).setShortcut('3', 'q');
        menu.add(2, ROOT, R.string.root).setShortcut('4', 'r');
        return true;
	}
    
    @Override
    public boolean onOptionsItemSelected(Menu.Item item) {
        switch (item.getId()) {
        case INSERT:
			Intent i = new Intent("com.baturinsky.tabletop.EDIT");
			i.setClass(this, FieldsEdit.class);
			StringBuffer order = new StringBuffer();
			for(int ind=0; ind<names.length;ind++){
				order.append(names[ind]+ "\n");
			}
			i.putExtra("names", order.toString());
			startSubActivity(i, INSERT);			
        	return true;
        case DELETE:
			int pos = list.getSelectedItemPosition();
			cursor.moveTo(pos);
    		AlertDialog.show(this, null, 0, String.format(getString(R.string.dir_delete_confirm), "this field"), getString(R.string.ok), 
    				new DialogInterface.OnClickListener(){public void onClick(DialogInterface arg0, int arg1) {
    					SQLiteDatabase db = party.getDb();
    					db.delete(table, "_id=?", new String[]{cursor.getString(0)});
    					view();
    				}}, getString(R.string.cancel), null, true, null);
        	
        	return true;
        case ROOT:
        	root = true;
        	view();
        	return true;
        }
    	return false;
    }
}
