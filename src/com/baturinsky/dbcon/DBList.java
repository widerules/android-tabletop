package com.baturinsky.dbcon;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;


import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.baturinsky.tabletop.R;


public class DBList  extends ListActivity {

	private static final int TYPE_URL_ID = 1;
	private static final int FILTER_ID = 2;
	private static final int DELETE_ID = 3;

		
	static String[] columnNames = new String[]{"file"};
	File dir;
	File parent;
	ArrayList<File> files = new ArrayList<File>();
	
	ArrayAdapter<String> adapter;

	protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setDefaultKeyMode(SHORTCUT_DEFAULT_KEYS);
                       
        setDir(new File("/data/data/com.baturinsky.tabletop"));
	}
	
	static Pattern dbRegexp = Pattern.compile(".*\\.db?");
	void setDir(File newDir){
		if(newDir != null)
			dir = newDir;
		setTitle(dir.getAbsolutePath());
        File[] dirFiles = dir.listFiles();
        parent = dir.getParentFile();
        
        ArrayList<String> rows = new ArrayList<String>();

        files.clear();
        if(parent != null){
            files.add(parent);
        	rows.add("..");        	
        }
        
        for(File f:dirFiles){
        	String name = f.getName();
        	if(f.isDirectory())
        		rows.add("[" + name + "]");        		
        	else if(!filterDB || dbRegexp.matcher(name).matches())
        		rows.add(name);
        	else
        		continue;
        	files.add(f);
        }
        
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, rows);

        setListAdapter(adapter);
	}

	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	if(files.get(position).isDirectory())
    		setDir(files.get(position));
    }

	boolean filterDB = false;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, TYPE_URL_ID, "Type url").setShortcut('1', 'a');
        menu.add(1, FILTER_ID, "DB only").setShortcut('2', 'f');
        menu.add(2, DELETE_ID, "Delete").setShortcut('3', 'd');
        return true;
	}
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(filterDB)
        	menu.get(1).setTitle("Show all");
        else
        	menu.get(1).setTitle("DB Only");
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(Menu.Item item) {
        switch (item.getId()) {
        	case FILTER_ID:
        		filterDB = !filterDB;
        		setDir(null);
        		return true;
        	case DELETE_ID:
        		int position = getSelectedItemPosition();
        		File f = files.get(position);
        		f.delete();
        		setDir(null);
        		return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
