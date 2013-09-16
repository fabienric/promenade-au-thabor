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

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;
import android.view.View;

public class ViewHandler extends Handler {
	private final WeakReference<View> viewRef;

	ViewHandler(View view) {
		viewRef = new WeakReference<View>(view);
	}

	@Override
	public void handleMessage(Message msg) {
		View view = viewRef.get();
		if (view != null) {
			view.invalidate();
		}
	}
}