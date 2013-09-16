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

import java.util.HashMap;
import java.util.Map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapCache {

	protected static Map<Integer, Bitmap> bitmaps = new HashMap<Integer, Bitmap>();

	public static Bitmap get(int id) {
		return bitmaps.get(id);
	}

	public static Bitmap add(Resources res, int id) {
		if (!bitmaps.containsKey(id)) {
			Bitmap b = BitmapFactory.decodeResource(res, id);

			if (b != null)
				bitmaps.put(id, b);
		}
		return get(id);
	}
}
