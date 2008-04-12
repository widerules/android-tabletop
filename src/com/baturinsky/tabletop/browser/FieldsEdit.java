package com.baturinsky.tabletop.browser;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewInflate;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baturinsky.tabletop.R;

public class FieldsEdit extends Activity {

	EditText[] edits;
	String[] names;
	String names1;
	Bundle fields;
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.fields_edit);
        fields = getIntent().getBundleExtra("values");
        if(fields == null)
        	fields = new Bundle(); 
        names1 =  getIntent().getStringExtra("names");
        names = names1.split("\n");
        LinearLayout l = (LinearLayout)this.findViewById(R.id.list);
        edits = new EditText[names.length];
        for(int i = 0;i<names.length;i++){
        	String name = names[i];
			TextView tvn = (TextView)ViewInflate.from(this).inflate(R.layout.small_white_text,null,null);
			tvn.setText(name);
			l.addView(tvn, new LinearLayout.LayoutParams(-1,-2));
			EditText edit = (EditText)ViewInflate.from(this).inflate(R.layout.edit, null,null);
			edits[i] = edit;
			edit.setText(fields.getString(name));
			l.addView(edit, new LinearLayout.LayoutParams(-1,-2));
        }
        ((Button)findViewById(R.id.ok)).setOnClickListener(new OnClickListener(){public void onClick(View view) {
        			onOk();
        		}});                
        ((Button)findViewById(R.id.cancel)).setOnClickListener(new OnClickListener(){public void onClick(View view) {
        	setResult(RESULT_CANCELED);
			finish();
		}});                

	}
	
	void onOk(){
		for(int i=0;i<names.length;i++){
			fields.putString(names[i], edits[i].getText().toString());
		}
		setResult(RESULT_OK, names1, fields);
		finish();
	}
}
