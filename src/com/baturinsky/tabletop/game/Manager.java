package com.baturinsky.tabletop.game;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.baturinsky.tabletop.R;
import com.baturinsky.tabletop.service.PartyConstants;
import com.baturinsky.tabletop.service.PartyListener;

public class Manager extends Accessory {

	final static String TAG = "manager";

	static XMLReader xr = null;
	Context cx;
	//TODO: Need a lock
	static byte[] buffer = new byte[100000];
	//TODO: Will be weak tables
	HashMap<String, Bitmap> parsedBitmaps = new HashMap<String, Bitmap>();
	HashMap<String, Zip> parsedZips = new HashMap<String, Zip>();
	HashMap<String, Long> nameIds = new HashMap<String, Long>(); 
	boolean logging = false;
	boolean externalTransaction = false;

	private PartyListener listener;

	long nameId(String name){
		Long n = nameIds.get(name);
		if(n==null)
		{
			long _id = Accessory.indCount++;
			nameIds.put(name, _id);
			if(listener!=null)
			{
				_begin();
				listener.write(_id, name);
				listener.link(PartyConstants.NAME, _id);
				_end();
			}
			return _id;
		}
		return n.longValue();
	}
	
	void setPartyListener(PartyListener listener) {
		this.listener = listener;		
	}

	Manager(Context cx, String name) {
		this.cx = cx;
		changeAttribute("name", name);
	}

	static public XMLReader getReader() {
		if (xr == null) {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			try {
				sp = spf.newSAXParser();
				xr = sp.getXMLReader();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return xr;
	};

	static public InputStream getStream(Context cx, String name)
			throws Exception {
		if (name.substring(0, 4).equals("raw:")) {
			String end = name.substring(4);
			int id = Class.forName(R.raw.class.getName()).getDeclaredField(end)
					.getInt(null);
			InputStream str = cx.getResources().openRawResource(id);
			return str;
		}
		InputStream str = cx.openFileInput(name);
		return str;
	}
	
	static public String readString(Context cx, String name) throws Exception {
		InputStream stream = getStream(cx, name);
		if(stream==null)
			return null;
		int l = stream.read(buffer, 0, buffer.length);
		String str = new String(buffer, 0, l);
		return str;
	}

	public InputStream getStream(String name) throws Exception {
		return getStream(cx, name);
	}

	public Bitmap getBmp(String uri) throws Exception {
		int h = uri.indexOf('#');
		if (h >= 0) {
			getZip(uri.substring(0, h));
		}
		if (parsedBitmaps.containsKey(uri)) {
			return parsedBitmaps.get(uri);
		}
		InputStream is = getStream(uri);
		Bitmap bmp = BitmapFactory.decodeStream(is);
		parsedBitmaps.put(uri, bmp);
		return bmp;
	}

	class Zip {
		TreeMap<String, Bitmap> zipBitmaps = new TreeMap<String, Bitmap>();

		Zip(String zipName) {
			Log.i(TAG, "Parsing zip " + zipName);
			if (zipName != null && cx != null) {
				try {
					InputStream is = Manager.getStream(cx, zipName);
					ZipInputStream deckStream = new ZipInputStream(is);
					Bitmap bmp;
					ZipEntry ze;
					int read, s;
					for (;;) {
						ze = deckStream.getNextEntry();
						if (ze == null)
							break;
						s = (int) ze.getSize();
						read = 0;
						while (read < s)
							read += deckStream.read(buffer, read, s - read);
						bmp = BitmapFactory.decodeByteArray(buffer, 0, s);
						if (bmp == null)
							continue;
						String pid = ze.getName().replaceAll("\\.\\w*$", "");
						zipBitmaps.put(pid, bmp);
						parsedBitmaps.put(zipName + "#" + pid, bmp);
					}
					parsedZips.put(zipName, this);
				} catch (Exception e) {
					Log.e(TAG, "reading zip", e);
				}
			}
		}

	};

	Zip getZip(String zipName) {
		if (zipName != null && cx != null) {
			if (parsedZips.containsKey(zipName))
				return parsedZips.get(zipName);
			else
				return new Zip(zipName);
		}
		return null;
	}

	public Accessory parseFile(String path) {
		try {
			Parser p = new Parser(this);
			XMLReader xr = Manager.getReader();
			InputStream str = getStream(path);
			xr.setContentHandler(p);
			xr.parse(new InputSource(str));
			return p.root;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	Manager manager() {
		return this;
	}

	@Override
	void childAttributeChanged(Accessory child, Attr attr, String oldValue,
			String newValue) {
		if (logging)
			Log.i(TAG, "attribute <" + attr._id + ">" + attr.name + " of "
					+ child.fullName() + " changed from " + oldValue + " to "
					+ newValue);
		if (listener != null) {
			_begin();
			if (newValue == null) {
				listener.delete(attr._id);
			} else {
				if(oldValue == null)
					listener.link(nameId(attr.name), attr._id);
				listener.write(attr._id, newValue);
			}
			_end();
		}
	}

	@Override
	void childMoved(Accessory child, Accessory from, Accessory to) {
		if (from == child.sup()) {
			if (from != null) {
				if (logging)
					Log.i(TAG, "removing " + child.fullName() + " from "
							+ from.fullName());
			}
			if (listener != null) {
				_begin();
				if (to == null)
					listener.delete(child._id());
				else
					listener.unlink(from._id(), child._id());
				_end();
			}
			return;
		} else {
			if (listener != null) {
				_begin();
				if (from != null && from.manager() == this)
					listener.link(to._id(), child._id());
				else {
					listener.write(child._id(), child.name());
					listener.link(PartyConstants.ACCESSORY, child._id());
					if(child.attrs() != null){
						for (Attr a : child.attrs().values()) {						
							listener.write(a._id, a.value);
							listener.link(child._id(), a._id);
							listener.link(nameId(a.name), a._id);
							listener.link(PartyConstants.ATTRIBUTE, a._id);
						}
					}
				}
				_end();
			}
		}

		if (logging)
			Log.i(TAG, "/" + child.toString() + "\n\\moved from "
					+ (from == null ? null : from.fullName()) + " to "
					+ (to == null ? null : to.fullName()));

	}

	private void _begin(){
		if(!externalTransaction)
			listener.begin(new Date().getTime());
	}

	private void _end(){
		if(!externalTransaction)
			listener.end();
	}
	
	void begin(){
		_begin();
		externalTransaction = true;
	}

	void end(){
		externalTransaction = false;
		_end();
	}

}
