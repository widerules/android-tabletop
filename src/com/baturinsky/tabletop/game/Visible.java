package com.baturinsky.tabletop.game;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

public class Visible extends Accessory {

	final static int REPLACED = 1, HOLE = 2, STATIC = 4, COPIED = 8,
			CONTAINER = 16, FOCUSABLE = 32;
	final static String[] Flags = new String[] { "replaced", "hole", "static",
			"copied" };

	private float[] place = new float[] { 0, 0, 0 };
	private float[] anchor = new float[] { 0, 0, 0 };
	private List<Visible> visibles = new ArrayList<Visible>();
	private long flags = 0;
	private boolean focus = false;

	float[] size = new float[] { 0, 0, 0 };
	Matrix matrix = new Matrix();
	Matrix ctm = new Matrix();

	void setFocus(boolean v) {
		focus = v;
		notifyChildViewChanged(this);
	}

	boolean focus() {
		return focus;
	}

	List<Visible> visibles() {
		return visibles;
	}

	void calculateMatrix() {
		matrix.setTranslate(place[X], place[Y]);
	}

	void calculateCtm() {
		calculateMatrix();
		if (!(sup() instanceof Visible) || (sup() instanceof Deck))
			ctm.set(matrix);
		else
			ctm.setConcat(((Visible) sup()).ctm, matrix);
		for (Visible v : visibles()) {
			v.calculateCtm();
		}
	}

	final void draw(Canvas canvas) {
		draw1(canvas);
	}

	void draw1(Canvas canvas) {
		for (Visible p : visibles()) {
			p.draw(canvas);
		}
	}

	Matrix finalMatrix() {
		float r = 1 / ctm.mapRadius(1);
		Matrix ctm1 = new Matrix(ctm);
		ctm1.preScale(r, r);
		ctm1.preTranslate(-anchor[X], -anchor[Y]);
		return ctm1;
	}

	boolean flag(long flag) {
		return (flags & flag) != 0;
	}

	void setFlag(long flag, boolean val) {
		//Log.i(TAG, "flag " + flag + " set to " + val);
		if (val)
			flags = flags | flag;
		else
			flags = flags & ~flag;
	}

	Accessory interpretAttributes() {
		super.interpretAttributes();
		for (int i = 0; i < 3; i++) {
			if (has(XNames[i])) {
				place[i] = getFloat(XNames[i]);
			}
			if (has("anchor_" + XNames[i])) {
				anchor[i] = getFloat("anchor_" + XNames[i]);
			}
			if (has(SNames[i]))
				size[i] = getFloat(SNames[i]);
		}
		int f = 1;
		for (int i = 0; i < Flags.length; i++) {
			if (has(Flags[i]))
				setFlag(f, getBoolean(Flags[i]));
			f = f * 2;
		}
		calculateMatrix();
		return this;
	}

	@Override
	Accessory copy() {
		return new Visible().copy(this);
	}

	public void setPlace(float[] place) {
		this.place = place;
		changeAttribute("x", Float.toString(place[X]));
		changeAttribute("y", Float.toString(place[Y]));
		// Log.i(TAG, "setPlace " + place[X] + "," + place[Y] + "," + place[Z]);
		calculateCtm();
	}

	public float[] place() {
		return place;
	}

	public void setSize(float[] size) {
		this.size = size;
	}

	public float[] getSize() {
		return size;
	}

	public Visible getSubPieceAt(float[] at) {
		Visible pmax = null;
		float dst = Float.NEGATIVE_INFINITY;
		for (Visible piece : visibles()) {
			Visible pieceAt = piece.at(at);
			if (pieceAt == null)
				continue;
			float d = pieceAt.place[Z];
			if (d > dst) {
				dst = d;
				pmax = pieceAt;
			}
		}
		return pmax;
	}

	Visible at(float[] at) {
		Visible sp = getSubPieceAt(at);
		if (sp != null)
			return sp;
		return shallowAt(at);
	}

	Visible shallowAt(float[] at) {
		RectF rect = new RectF(0, 0, size[WIDTH], size[HEIGHT]);
		finalMatrix().mapRect(rect);
		if (rect.contains(at[X], at[Y]))
			return this;
		return null;
	}

	Visible takeTo(Accessory to) {
		if (flag(STATIC))
			return null;
		if (flag(COPIED)) {
			if(to == null)
				return null;
			Visible v = (Visible) copy();
			v.setFlag(COPIED, false);			
			v.into(to);
			return v;
		} else {
			into(to);
			return this;
		}
	}

	void click(float[] at) {
	}

	boolean drop(Visible item, float[] at) {
		if (flag(HOLE)) {
			item.takeTo(null);
		} else if (flag(CONTAINER)) {
			Matrix inv = new Matrix();
			inv.invert(ctm);
			inv.mapPoints(at);
			Visible v = item.takeTo(this);			
			v.setPlace(at);
		} else if (flag(REPLACED)) {
			Visible v = item.takeTo(sup());			
			v.setPlace(place());
			into(null);
		} else {
			return false;
		}
		return true;
	}

	static Paint focusBorderPaint = new Paint() {
		{
			setStyle(Paint.Style.STROKE);
			setColor(0xffD4AF37);
			setStrokeWidth(2);
		}
	};

	static Paint focusFillPaint = new Paint() {
		{
			setStyle(Paint.Style.FILL);
			setColor(0x40D4AF37);
		}
	};

	void drawFocus(Canvas canvas) {
		if (focus) {
			canvas.drawRect(-3, -3, size[WIDTH] + 3, size[HEIGHT] + 3,
					focusFillPaint);
			canvas.drawRect(-3, -3, size[WIDTH] + 3, size[HEIGHT] + 3,
					focusBorderPaint);
		}
	}
	
	final void notifyChildViewChanged(Accessory child) {
		if (sup() != null && sup() instanceof Visible) {
			((Visible)sup()).childViewChanged(child);
			((Visible)sup()).notifyChildViewChanged(child);
		}
	}

	void childViewChanged(Accessory child) {
	}
	
	@Override
	void childMoved(Accessory child, Accessory from, Accessory to) {
		if(from == this)
			visibles.remove(child);
		if(to == this && child instanceof Visible)
			visibles().add((Visible)child);
		
		childViewChanged(child);
		super.childMoved(child, from, to);
	}
	
}
