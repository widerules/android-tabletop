package com.baturinsky.tabletop.game;

import android.graphics.Canvas;

public class Card extends Visible {

	Visible face, back;

	Card setFaceAndBack(Visible face, Visible back) {
		if(!has("name"))
			changeAttribute("name", face.name());
		this.face = face;
		this.back = back;		
		size = face.size.clone();
		return this;
	}

	void draw1(Canvas canvas) {
		if (face != null) {
			canvas.save();
			canvas.concat(finalMatrix());
			drawFocus(canvas);
			face.draw(canvas);
			canvas.restore();
		}
	}

	@Override
	Accessory construct(){
		return new Card();
	}

}
