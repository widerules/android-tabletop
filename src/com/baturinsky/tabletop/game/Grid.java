package com.baturinsky.tabletop.game;

import android.graphics.Matrix;

public class Grid extends Visible {

	float delta[][];

	@Override
	void calculateMatrix() {
		if (matrix == null)
			return;
		matrix.setTranslate(place()[X], place()[Y]);
		if (delta != null) {
			Matrix m = new Matrix();
			m.setPolyToPoly(new float[] { 0, 0, 1, 0, 0, 1 }, 0, new float[] {
					0, 0, delta[X][X], delta[X][Y], delta[Y][X], delta[Y][Y] },
					0, 3);
			matrix.setConcat(matrix, m);
			/*
			 * Log .i(TAG, "Grid " + delta[X][X] + "," + delta[X][Y] + " " +
			 * delta[Y][X] + "," + delta[Y][Y] + " " + matrix.toString());
			 */
		}
	}

	@Override
	Accessory interpretModel() {
		super.interpretModel();
		delta = new float[3][3];
		for (int i = 0; i < 3; i++) {
			String d1 = get("d_" + XNames[i]);
			if (d1 == null)
				continue;
			String[] d2 = d1.split(",");
			int j = 0;
			for (String d3 : d2) {
				delta[i][j++] = Float.parseFloat(d3);
			}
		}
		return this;
	}

	Grid() {
		changeAttribute("child_hole", "false");
		changeAttribute("container", "true");
	};

	@Override
	Accessory construct(){
		return new Grid();
	}

	Visible at(float[] at) {
		Visible v = getSubPieceAt(at);
		if (v == null)
			return super.at(at);
		else
			return v;
	}

	boolean drop(Visible item, float[] at) {
		Matrix inv = new Matrix();
		ctm.invert(inv);
		inv.mapPoints(at);
		for (int i = 0; i < at.length; i++)
			at[i] = Math.round(at[i]);
		Visible v = item.takeTo(this);		
		v.setPlace(at);
		v.interpretId();
		// Log.i(TAG, v.toString());
		return true;
	}

}
