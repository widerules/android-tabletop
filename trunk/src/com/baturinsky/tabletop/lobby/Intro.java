package com.baturinsky.tabletop.lobby;

import java.io.File;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.Html.TagHandler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.baturinsky.tabletop.R;
import com.baturinsky.tabletop.browser.PartyBrowser;
import com.baturinsky.tabletop.service.Party;

public class Intro extends Activity {

	    protected static final String TAG = "intro";
	    
	    protected static final int CREATE_PARTY = 0;
	    protected static final int CONTINUE_PARTY = 1;
	    protected static final int ABOUT = 2;
	    
	    protected static final int SELECT_FILE = 1;
	    Activity activity;

		@Override
	    protected void onCreate(Bundle icicle) {
	        super.onCreate(icicle);
	        activity = this;
	        
	        //Accessory.parseFile(this, "raw:cards");
	        setContentView(R.layout.intro);
	        ListView lv = (ListView)findViewById(R.id.menu);
	        final String home = "/data/data/com.baturinsky.tabletop/databases";
	        final String root = "/data/data/com.baturinsky.tabletop";
	        
	        lv.setOnItemClickListener(
        		new OnItemClickListener(){
					@SuppressWarnings("unchecked")
					public void onItemClick(AdapterView parent, View v,
							int position, long id) {
						Intent i;
						Log.i(TAG, Integer.toString(position));
						switch(position){
						case CREATE_PARTY:
							FileBrowser.showCreateDialog(activity, Uri.parse(home).toString(), root, CREATE_PARTY);
							break;
						case CONTINUE_PARTY:
							FileBrowser.showContinueDialog(activity, Uri.parse(home).toString(), root, CONTINUE_PARTY);
							break;
						case ABOUT:
							setContentView(R.layout.help);
							Spanned text = Html.fromHtml(
									getString(R.string.help_text),
									null,
									new TagHandler(){

										public boolean handleTag(
												boolean arg0, String tag,
												Editable arg2,
												XmlPullParser arg3) {
											Log.w(TAG, tag);
											return true;
										}
										
									}
							);
							((TextView)findViewById(R.id.text)).setText(text);
							break;
						}
					}	        			
        		}
	        );
	    }
		
	     protected void onActivityResult(int requestCode, int resultCode,
	             String data, Bundle extras) {
	    	if(data == null)
	    		return;
			if(!data.matches(".*\\.db?")) {
				data = data + ".db";
			}

	    	if (requestCode == CONTINUE_PARTY) {
		        if(resultCode == RESULT_OK)
		        {
			        Toast.makeText(this, "continuing party" + data, Toast.LENGTH_SHORT).show();
			        startParty(data);
		        }
	        }

	        if (requestCode == CREATE_PARTY) {
				if(resultCode == RESULT_OK){
					File file = new File(data);
					Party p;
					try {
						p = new Party(file);
						p.setOption("viewer", PartyBrowser.class.getName());
						startParty(data);
					} catch (Exception e) {						
						e.printStackTrace();
						Toast.makeText(this, "Party is corrupt or not existant", Toast.LENGTH_SHORT).show();
					}
				}
	        }
	     }
	     
	     void startParty(String name)
	     {	    	
			File file = new File(name);
			Party p = Party.create(file,this);
			String viewer = p.getOptionValue("viewer");
			if(viewer == null)
				viewer = PartyBrowser.class.getName();
			Intent i = new Intent("com.baturinsky.tabletop.VIEW", Uri.parse(name));
			Toast.makeText(this, "using viewer " + viewer, Toast.LENGTH_SHORT).show();
			i.setClassName(this, viewer);
			startSubActivity(i, CONTINUE_PARTY);	    	 
	     }
}
