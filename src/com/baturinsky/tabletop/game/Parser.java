package com.baturinsky.tabletop.game;

import org.xml.sax.Attributes;
import org.xml.sax.ext.DefaultHandler2;

import android.util.Log;

public class Parser extends DefaultHandler2
{
    protected static final String TAG = "game.parser";
	Accessory current;
	Accessory root = null;
	
	public Parser(Manager m){
		current = m;
	}
	
	@Override 
    public void startElement(String uri, String name, 
                                             String qname, Attributes attrs){
		Accessory a;
		if(name.equals("deck")){
			a = new Deck();
		} else if(name.equals("piece") || name.equals("visible") || name.equals("position")) {
			a = new Visible();
		} else if(name.equals("grid")) {
			a = new Grid();
		} else if(name.equals("picture")) {
			a = new Picture();
		} else if(name.equals("copy")) {
			String of = attrs.getValue("of");
			Accessory prototype = current.locate(of);
			assert prototype != null: of + " not found";
			a = prototype.copy();
		} else	{
			a = new Accessory();
		}
		
		a.read(current, attrs);
		a.parseZip();
		a.parseLine();

		if(root == null)
			root = a;
		
		current = a;
	}
	    
    @Override
    public void endElement(String uri, String name, String qName){
    	Log.i(TAG, current.order().size() + " items in " + current.fullName());
    	current = current.sup();
    }
    
	static float minNum(String r) {
		if (r == null)
			return 0;
		int i = r.indexOf('~');
		if (i >= 0)
			return Float.parseFloat(r.substring(0, i));
		else
			return Float.parseFloat(r);
	}

	static float maxNum(String r) {
		if (r == null)
			return 0;
		int i = r.indexOf('~');
		if (i >= 0)
			return Float.parseFloat(r.substring(i + 1));
		else
			return Float.parseFloat(r);
	}

}
