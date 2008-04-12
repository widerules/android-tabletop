package com.baturinsky.tabletop.game;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class Deck extends Visible {

	//private final static String TAG = "Deck";
	private final static int cardShift = 10;
	private final static int border = 4;

	private Bitmap bitmap = null;

	private float w = 0;
	private float h = 0;
	private Presentation presentation = Presentation.LINE;

	enum Presentation {
		FAN, STACK, LINE
	}

	Deck() {
		setFlag(CONTAINER, true);
	};
	
	Deck parseCardList() {
		String faceRange = get("cards");				
		
		if (faceRange != null) {
			discard("cards");
			String backId = get("back");
			Visible back = (Visible) locate(backId);

			List<Accessory> r = range(faceRange);
			for (Accessory face : r) {
				if (face != null) {
					if (back == null)
						face.copyTo(this);
					else
						new Card().setFaceAndBack((Visible) face, back).into(this);
				}
			}
			setWH();
		}
		return this;
	}

	@Override
	Accessory interpretModel() {		
		super.interpretModel();		
		if (has("cardWidth"))
			w = getInt("cardWidth");
		if (has("cardHeight"))
			h = getInt("cardHeight");
		if (has("presentation")) {
			String p = get("presentation");
			try {
				presentation = Presentation.valueOf(p.toUpperCase());
			} catch (Exception e) {
			}
		}		
		return this;
	}

	public void setPresentation(Presentation p) {
		presentation = p;
		bitmap = null;
	}

	void setWH() {
		if (visibles().size() > 0) {
			Visible v = visibles().get(0);
			if (w <= 0)
				w = v.size[WIDTH];
			if (h <= 0)
				h = v.size[HEIGHT];
		}
	}

	protected void updateImage() {
		int n = visibles().size();
		if (n == 0)
			return;
		setWH();

		switch (presentation) {
		case FAN:
			size[WIDTH] = w + cardShift * n + h + border * 2;
			size[HEIGHT] = h + w / 2 + border * 2;
			break;
		default:
		case STACK:
			size[WIDTH] = w + border * 2;
			size[HEIGHT] = h + border * 2;
			break;
		case LINE:
			size[WIDTH] = w * n + border * 2;
			size[HEIGHT] = h + border * 2;
			break;
		}

		Bitmap newBitmap = Bitmap.createBitmap((int) size[WIDTH],
				(int) size[HEIGHT], true);
		Canvas canvas = new Canvas();
		canvas.setDevice(newBitmap);
		int i = 0;
		canvas.save();
		canvas.translate(border, border);
		switch (presentation) {
		case FAN:
			canvas.translate(size[WIDTH] / 2, h);
			for (Visible v : visibles()) {
				canvas.save();
				canvas.translate(-(n / 2 - i) * cardShift, w / 3);
				if (n > 1) {
					canvas.rotate(i * 60 / (n - 1) - 30, 0, 0);
				}
				canvas.translate(-w / 2, -h - w / 5);
				v.draw(canvas);
				canvas.restore();
				i++;
			}
			break;
		case STACK:
			if (visibles().size() > 0) {
				Visible card = visibles().get(0);
				card.draw(canvas);
			}
			break;
		case LINE:
			canvas.save();
			canvas.translate(w / 2, h / 2);
			for (Visible a : visibles()) {
				a.draw(canvas);
				canvas.translate(w, 0);
			}
			canvas.restore();
			break;
		}

		canvas.restore();
		bitmap = newBitmap;
	}

	@Override
	public void draw1(Canvas canvas) {
		canvas.save();
		canvas.concat(finalMatrix());
		drawFocus(canvas);
		if (bitmap == null)
			updateImage();
		if (bitmap != null) {
			canvas.drawBitmap(bitmap, 0, 0, null);
		}
		canvas.restore();
	}

	public void nextPresentation() {
		presentation = Presentation.values()[(presentation.ordinal() + 1) % 3];
		updateImage();
	}

	@Override
	Accessory construct(){
		return new Deck();
	}

	@Override
	void click(float[] at) {
		// nextPresentation();
	}

	@Override
	boolean drop(Visible item, float[] at) {
		if (this != item) {
			item.into(this).interpretModel();
			updateImage();
		} else {
			return false;
		}
		return true;
	}

	Visible at(float[] at) {		
		if (shallowAt(at) == null)
			return null;
		float b[];
		switch (presentation) {
		case STACK:
			return visibles().get(0);
		case LINE:
			calculateCtm();
			b = at.clone();
			Matrix inv = new Matrix();
			ctm.invert(inv);
			inv.mapPoints(b);
			int ind = (int)((b[X]+border) / w);
			calculateCtm();
			return visibles().get(ind);
		}
		return this;
	}

	@Override
	void childViewChanged(Accessory child) {
		bitmap = null;
		super.childViewChanged(child);
	}

	@Override
	void childMoved(Accessory child, Accessory from, Accessory to) {
		bitmap = null;
		super.childMoved(child, from, to);
	}

}
