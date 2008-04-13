package com.baturinsky.tabletop.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Picture extends Visible {
	private Drawable image;
	private boolean antialiased;
	private Rect bounds;

	Picture() {
	}

	@Override
	void draw1(Canvas canvas) {
		canvas.save();
		canvas.concat(finalMatrix());
		drawFocus(canvas);
		Drawable img = image();
		if(img != null)
			image().draw(canvas);
		canvas.restore();
	}

	@Override
	Accessory interpretModel() {
		super.interpretModel();
		if (has("antialiased"))
			antialiased = getBoolean("antialiased");
		return this;
	}

	@Override
	Accessory construct(){
		return new Picture();
	}

	void parseImage() {
		Bitmap bmp;
		String uri = get("file");
		if (uri != null) {
			try {
				Manager m = manager();
				bmp = m.getBmp(uri);
				BitmapDrawable bd = new BitmapDrawable(bmp);
				bd.setAntiAlias(antialiased);
				setImage(bd);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void setImage(Drawable image) {
		this.image = image;
		if (size[WIDTH] == 0)
			size[WIDTH] = image.getIntrinsicWidth();
		if (size[HEIGHT] == 0)
			size[HEIGHT] = image.getIntrinsicHeight();
		bounds = new Rect(0, 0, (int) size[WIDTH], (int) size[HEIGHT]);
		image.setBounds(bounds);
	}

	Drawable image() {
		if (image == null)
			parseImage();
		//TODO: Find error and return assert
		//assert image != null : "Image not found\n" + toString();
		return image;
	}

	void setBmpFromZip(String name, Bitmap bmp) {
		manager().parsedBitmaps.put(name, bmp);
		changeAttribute("file", name);
	}
}
