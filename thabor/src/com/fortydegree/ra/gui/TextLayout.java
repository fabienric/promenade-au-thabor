/*
 * Promenade au Thabor
 * Copyright (C) 2011 40degree (Marc Haussaire & Fabien Ric)
 *
 * http://www.40degree.com
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package com.fortydegree.ra.gui;

import android.graphics.Canvas;
import android.graphics.Paint;

public class TextLayout {

	protected int x, y, fontsize;
	protected int lineSpace = 2;
	protected Paint mode;

	public TextLayout(Paint mode) {
		this.mode = mode;
	}

	public void start(int x, int y, int fontsize) {
		this.x = x;
		this.y = y;
		this.fontsize = fontsize;
	}

	public void drawline(Canvas canvas, String text) {
		if (canvas != null) {
			canvas.drawText(text, x, y, mode);
			y += fontsize + lineSpace;
		}
	}

}
