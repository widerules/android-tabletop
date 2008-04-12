package com.baturinsky.tabletop.lobby;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.baturinsky.tabletop.R;
import android.widget.Toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FileBrowser extends Activity{

	private static final int CREATE_DIR_ID = 1;
	private static final int FILTER_ID = 2;
	private static final int DELETE_ID = 3;
	private static final int CHANGE_DIR_ID = 4;
	private static final String TAG = "file browser";
	boolean filter = false;
		
	static String[] columnNames = new String[]{"file"};
	File dir, root;
	ArrayList<File> files = new ArrayList<File>();
	
	ArrayAdapter<String> adapter;
	
	ListView listView;
	AutoCompleteTextView textView; 
	
	Activity activity;

	static Pattern showRegexp = null;

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);        
        activity = this;
        setContentView(R.layout.files);
        listView = (ListView)findViewById(R.id.file_list);
        textView = (AutoCompleteTextView)findViewById(R.id.file_name);
        Uri uri = getIntent().getData();
        String rootPath = getIntent().getStringExtra("root");        
        root = rootPath == null?null:new File(getIntent().getStringExtra("root"));
        String showFilter = getIntent().getStringExtra("showFilter");
        if(showFilter != null)
        {
        	filter = true;
        	showRegexp = Pattern.compile(showFilter);
        }
        view(new File(uri.toString()));
        
        textView.requestFocus();
                
        listView.setOnItemClickListener(new OnItemClickListener(){        	
			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView parent, View v, int position, long id) {
				if(position<0)
					return;
				File f = files.get(position); 
		    	if(f.isDirectory())
		    		view(f);
		    	else 
		    		onOk();
			}        	
        });
        
        textView.setOnLongClickListener(new OnLongClickListener(){
			
        	@Override
			public boolean onLongClick(View v) {
				Editable text = textView.getText();				
				String name = text.toString();
				File f = new File(dir, name);
				if(f.isDirectory())
					view(f);
				else
					AlertDialog.show(activity, "You have chosen", 0, f.getAbsolutePath(), "OK", false);
				return true;
			}        	
        });
        
        Button ok = (Button)findViewById(R.id.ok);
        Button cancel = (Button)findViewById(R.id.cancel);
        cancel.setOnClickListener(
        		new OnClickListener(){
					@Override
					public void onClick(View view) {
						activity.finish();
					}        			
        		}
        );
        ok.setOnClickListener(
        		new OnClickListener(){
					@Override
					public void onClick(View view) {
						onOk();
					}        			
        		}
        );
	}


	void view(File newDir){
		if(newDir != null && newDir.exists())
			dir = newDir;
		try {
			String path = dir.getCanonicalPath();
			if(root != null){
				String rootPath = root.getCanonicalPath();
				path = path.substring(rootPath.length());
				if(path.length()==0)
					path = "/";
			}
			setTitle(path);
		} catch (IOException e) {
			Log.w(TAG, "Can't resolve absolute path of current directory");
		}
        File[] dirFiles = dir.listFiles();
        File parent = dir.getParentFile();
        
        ArrayList<String> rows = new ArrayList<String>();
        ArrayList<String> autoRows = new ArrayList<String>();

        files.clear();
        if(parent != null && !dir.equals(root)){
            files.add(parent);
        	rows.add("..");
        	autoRows.add("..");
        }
        
        for(File f:dirFiles){
        	String name = f.getName();
        	if(!f.isDirectory() && filter && !showRegexp.matcher(name).matches())
        		continue;
        	autoRows.add(name);
        	rows.add(f.isDirectory()?"[" + name + "]":name);
        	files.add(f);
        }
        
        ArrayAdapter<String> a = new ArrayAdapter<String>(this, R.layout.list_item, rows);
        listView.setAdapter(a);
        
        ArrayAdapter<String> b = new ArrayAdapter<String>(this, R.layout.list_item, autoRows);
        textView.setText("");
        textView.setAdapter(b);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, CREATE_DIR_ID, R.string.dir_create).setShortcut('1', 'a');
        menu.add(1, CHANGE_DIR_ID, R.string.dir_change).setShortcut('2', 'c');
        menu.add(2, DELETE_ID, R.string.delete).setShortcut('3', 'd');
        menu.add(3, FILTER_ID, R.string.filter).setShortcut('4', 'f');
        return true;
	}
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(filter)
        	menu.get(3).setTitle(getString(R.string.no_filter));
        else
        	menu.get(3).setTitle(getString(R.string.filter));
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(Menu.Item item) {
		final File f = getSelected();
        switch (item.getId()) {
        	case FILTER_ID:
        		filter = !filter;
        		view(null);
        		return true;
        	case DELETE_ID:
        		if(f == dir)
        			return false;
        		AlertDialog.show(activity, null, 0, String.format(getString(R.string.dir_delete_confirm), f.getAbsolutePath()), getString(R.string.ok), 
        				new DialogInterface.OnClickListener(){public void onClick(DialogInterface arg0, int arg1) {	        	        		
        	        		Log.i(TAG, "deleting " + f.getAbsolutePath());
        	        		boolean deleted = f.delete();
        	        		if(!deleted)
        	        			Toast.makeText(activity, R.string.cant_delete_file, Toast.LENGTH_SHORT).show();
        	        		view(null);        					
        				}}, getString(R.string.cancel), null, true, null);
        		return true;
        	case CREATE_DIR_ID:
        		if(textView.getText().toString().length() == 0)
        		{
        			Toast.makeText(this, R.string.type_dir_name, Toast.LENGTH_SHORT).show();
        			break;
        		}

        		AlertDialog.show(activity, null, 0, String.format(getString(R.string.dir_create_confirm), f.getAbsolutePath()), getString(R.string.ok), 
        				new DialogInterface.OnClickListener(){public void onClick(DialogInterface arg0, int arg1) {	        	        		
        	        		boolean res = f.mkdir();
        	        		Log.i(TAG, Boolean.toString(res));
        	        		view(null);
        				}}, getString(R.string.cancel), null, true, null);
        		return true;
        	case CHANGE_DIR_ID:
        		view(getSelected());
        		return true;	
        }
        return super.onOptionsItemSelected(item);
    }

    File getSelected(){
    	String name = textView.getText().toString();
		if(name.length()>0)
			return new File(dir, name);
    	int position = listView.getSelectedItemPosition();
    	File selected = position<=0?dir:files.get(position);
		if(selected != dir)
			return selected;
		return null;
    }
    
    void onOk(){
		File f = getSelected();
		if(f == null){
			Toast.makeText(activity, R.string.file_not_selected, Toast.LENGTH_SHORT).show();
			return;
		}
		if(f.isDirectory())
		{
			view(f);
			return;
		}
		setResult(RESULT_OK, f.getAbsolutePath());
		activity.finish();
    }
    
    static void showCreateDialog(Activity activity, String home, String root, int CALLBACK){
    	Intent i = new Intent("com.baturinsky.tabletop.CREATE", Uri.parse(home));
		i.setClass(activity, FileBrowser.class);
		i.putExtra("root", root);
		i.putExtra("showFilter", ".*\\.db?");
		activity.startSubActivity(i, CALLBACK);
    }
    
    static void showContinueDialog(Activity activity, String home, String root, int CALLBACK){
    	Intent i = new Intent("com.baturinsky.tabletop.CONTINUE", Uri.parse(home));
		i.setClass(activity, FileBrowser.class);
		i.putExtra("root", root);
		i.putExtra("showFilter", ".*\\.db?");
		activity.startSubActivity(i, CALLBACK);
    }


}
