package com.baturinsky.tabletop.game;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.xml.sax.Attributes;

import android.util.Log;

public class Accessory {

	final static int WIDTH = 0, HEIGHT = 1, DEPTH = 2;
	final static int X = 0, Y = 1, Z = 2;
	final static String XNames[] = new String[] { "x", "y", "z" };
	final static String SNames[] = new String[] { "width", "height", "depth" };
	final static String TAG = "accessory";

	static int indCount = 1; //TODO:Should be taken from manager 

	private int _id;
	private String name;
	private Accessory sup;

	private TreeMap<String, Accessory> subs;
	private ArrayList<Accessory> order;
	private TreeMap<String, Attr> attrs = new TreeMap<String, Attr>();
	
	TreeMap<String, Attr> attrs(){
		return attrs;
	}
	
	static void putToRange(List<Accessory> r, Accessory a, int q) {
		assert (a != null);
		assert (r != null);
		if (q == 1) {
			r.add(a);
		} else {
			for (int i = 1; i <= q; i++)
				r.add(a);
		}
	}

	Accessory() {
		_id = indCount++;
	}

	String name() {
		if (name == null)
			interpretId();
		return name;
	}
	
	int _id(){
		return _id;
	}

	Accessory sup() {
		return sup;
	}

	ArrayList<Accessory> order() {
		if (order == null)
			order = new ArrayList<Accessory>();
		return order;
	}

	TreeMap<String, Accessory> subs() {
		if (subs == null)
			subs = new TreeMap<String, Accessory>();
		return subs;
	}

	void addToMap(Accessory sub) {
		String sid = sub.name() == null ? "" : sub.name();
		if (sid.length() == 0 || subs().containsKey(sid)) {
			for (int i = 2;; i++) {
				String id = sid + "#" + i;
				if (!subs().containsKey(id)) {
					subs().put(id, sub);
					return;
				}
			}
		} else {
			subs().put(sub.name(), sub);
		}
	}

	void interpretId() {
		String aid = get("name");
		if (aid != null)
			this.name = aid;
	}

	void remove() {
		notifyChildMoved(this, sup(), null);
		detach();
	}

	private void detach() {
		if (sup() == null)
			return;
		sup().order.remove(this);
		sup().subs().remove(name());
		sup = null;
	}

	final Accessory into(Accessory to) {
		if(to == sup)
			return this;
		
		if (to != null && to.name() == null && to.sup() != null)
			return into(to.sup());
		
		Accessory from = sup();
		assert from != null || to != null;
		notifyChildMoved(this, from, to); //tell to old sup
		detach();
		if (to == null)
			return this;
		
		sup = to;
		//interpretAttributes();
		to.addToMap(this);
		to.order().add(this);
		notifyChildMoved(this, from, to); //tell to new sup
		return this;
	}

	private String get_here(String name) {
		Attr a = attrs.get(name);
		return a==null?null:a.value;
	}

	void changeAttribute(String name, String value) {
		Attr a = attrs.get(name);
		String old;
		if(a == null)
		{
			old = null;
			if(value != null){
				a = new Attr(name, value);
				attrs.put(name, a);
			}
		} else {
			old = a.value;
			a.value = value;
		}
		notifyChildAttributeChanged(this, a, old, value);		
		if(value == null){
			if(a!=null)
				attrs.remove(name);
		}
	}

	void discard(String name) {
		if (attrs != null)
			attrs.remove(name);
	}

	String get(String name) {
		String v = get_here(name);
		if (v != null)
			return v;
		if (sup() == null)
			return null;
		return sup().get_default("child_" + name);
	}

	private String get_default(String name) {
		String v = get_here(name);
		if (v != null)
			return v;
		if (sup() == null)
			return null;
		return sup().get_default(name);
	}

	protected boolean has(String name) {
		if(has_here(name))
			return true;
		if (sup == null)
			return false;
		return sup.has_default("child_" + name);
	}
	
	private boolean has_here(String name) {
		if (attrs != null) 
			return attrs.containsKey(name);
		else
			return false;
	}

	private boolean has_default(String name) {
		if(has_here(name))
			return true;
		if (sup == null)
			return false;
		return sup.has_default(name);
	}

	public int getInt(String name) {
		String s = get(name);
		if (s == null)
			return 0;
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return 0;
		}
	}

	public float getFloat(String name) {
		String s = get(name);
		if (s == null)
			return 0;
		try {
			return Float.parseFloat(s);
		} catch (Exception e) {
			return 0;
		}
	}

	public boolean getBoolean(String name) {
		String v = get(name);
		if (v == null)
			return false;
		v = v.toLowerCase().trim();
		return v.equals("yes") || v.equals("true") || v.equals("1");
	}

	public List<Accessory> range(String descr) {
		ArrayList<Accessory> r = new ArrayList<Accessory>();
		for (String s : descr.split(";")) {
			int star = s.indexOf('*');
			int q = 1;
			if (star >= 0) {
				q = Integer.parseInt(s.substring(star + 1));
				s = s.substring(0, star);
			}
			int i = s.indexOf('~');
			if (i >= 0) {
				Accessory first = locate(s.substring(0, i));
				if (first == null)
					continue;
				if (first.sup() == null) {
					if (sup().name() == (s.substring(i + 1)))
						r.add(sup());
					continue;
				}
				Accessory last = first.sup().subs().get(s.substring(i + 1));
				if (last == null)
					continue;
				for (Accessory a : first.sup().subs.subMap(first.name(), last.name())
						.values())
					putToRange(r, a, q);
				putToRange(r, last, q);
			} else {
				putToRange(r, locate(s), q);
			}
		}
		return r;
	}

	final public Accessory locate(String id) {
		if (id == null)
			return null;
		String[] path = id.split("\\.");
		Accessory at = this;

		while (at.sup() != null && !at.subs().containsKey(path[0])
				&& !path[0].equals(at.name())) {
			at = at.sup();
		}
		if (at == null) {
			Log.e(TAG, "Can't locate " + id
					+ ", no supertag or child of supertag with id " + path[0]);
			return null;
		}
		if (!path[0].equals(at.name()) && at.subs != null
				&& at.subs().containsKey(path[0]))
			at = at.subs().get(path[0]);

		for (int i = 1; i < path.length; i++) {
			at = at.subs().get(path[i]);
			if (at == null) {
				Log.e(TAG, "Can't locate " + id + ", no child with name "
						+ path[i] + " in tag with name " + path[i - 1]);
				return null;
			}
		}

		if (!path[path.length - 1].equals(at.name())) {
			Log.e(TAG, "Can't locate " + id + ". Don't really know why.");
			return null;
		}
		return at;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName().toLowerCase() + " "
				+ fullName());
		if (attrs != null)
			for (String k : attrs.keySet()) {
				sb.append("\n\t" + k + " = " + get(k));
			}
		return sb.toString();
	}

	Accessory construct(){
		return new Accessory();
	}
	
	final Accessory copyTo(Accessory to) {
		Accessory a = construct();
		a.into(to);
		a.copy(this);
		return a;
	}	
	
	final Accessory copy(Accessory proto) {
		addAttributes(proto.attrs);
		interpretId();
		for (Accessory a : proto.order())
		{
			a.copyTo(this);
		}
		return this;
	}
	
	Accessory interpretModel() {
		interpretId();
		for (Accessory a : order())
		{
			a.interpretModel();
		}
		return this;
	}


	final String fullName() {
		String s = name() == null?"nameless":name();
		for (Accessory a = sup(); a != null && !(a instanceof Manager); a = a.sup()) {
			s = a.name() + "." + s;
		}
		return "<" + _id + ">" + s;
	}

	List<Visible> visibles() {
		ArrayList<Visible> l = new ArrayList<Visible>();
		if (order != null) {
			for (Accessory a : order) {
				if (a instanceof Visible)
					l.add((Visible) a);
			}
		}
		return l;
	}

	Accessory readXmlAttributeName(Attributes a) {
		name = a.getValue("name");
		return this;
	}

	Accessory readXmlAttributes(Attributes a) {
		if (a != null) {
			if (attrs == null)
				attrs = new TreeMap<String, Attr>();
			for (int index = 0; index < a.getLength(); index++) {
				String v = a.getValue(index);
				if (v.length() > 0)
					changeAttribute(a.getQName(index), v);
				else
					changeAttribute(a.getQName(index), null);
			}
		}
		return this;
	}

	Accessory addAttributes(TreeMap<String, Attr> as) {
		for(Attr a:as.values()){
			if(!attrs.containsKey(a.name))
				changeAttribute(a.name, a.value);
		}
		return this;
	}

	void parseLine() {
		String pieces = get("line");
		if (pieces == null)
			return;
		discard("line");
		float[][] xrange = new float[3][2];
		for (int x = 0; x < 3; x++) {
			String r = get("range_" + XNames[x]);
			xrange[x][0] = Parser.minNum(r);
			xrange[x][1] = Parser.maxNum(r);
		}
		List<Accessory> r = range(pieces);
		int n = r.size();
		int i = 0;
		for (Accessory of : r) {
			if (!(of instanceof Visible))
				continue;
			Visible a = (Visible) of.copyTo(this);
			float[] p = new float[3];
			for (int x = 0; x < 3; x++) {
				p[x] = xrange[x][0]
						+ ((i == 0) ? 0 : (xrange[x][1] - xrange[x][0]) * i
								/ (n - 1));
			}
			a.setPlace(p);
			i++;
		}
	}

	void parseZip() {
		String zipName = get("zip_file");
		if (zipName == null)
			return;
		discard("zip_file");
		Manager.Zip z = manager().getZip(zipName);
		for (String pid : z.zipBitmaps.keySet()) {
			Picture p = new Picture();
			p.changeAttribute("name", pid);
			p.into(this);
			p.addAttributes(attrs);
			p.changeAttribute("file", zipName + "#" + pid);
			p.interpretModel();			
		}
	}

	final void notifyChildMoved(Accessory child, Accessory from, Accessory to) {
		if (sup() != null) {
			sup().childMoved(child, from, to);
			sup().notifyChildMoved(child, from, to);
		}
	}

	final void notifyChildAttributeChanged(Accessory child, Attr attr,
			String oldValue, String newValue) {
		if (sup() != null) {
			sup().childAttributeChanged(child, attr, oldValue, newValue);
			sup().notifyChildAttributeChanged(child, attr, oldValue, newValue);
		}
	}

	void childMoved(Accessory child, Accessory oldSup, Accessory newSup) {
	}

	void childAttributeChanged(Accessory child, Attr attr, String oldValue,
			String newValue) {
	}

	Manager manager() {
		return sup() == null ? null : sup().manager();
	}

}
