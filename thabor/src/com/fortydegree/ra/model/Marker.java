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
package com.fortydegree.ra.model;

import java.util.HashMap;
import java.util.Map;

import android.location.Location;

public class Marker {

	public String title;
	public Place place;
	// distance from user in meters
	public double distance;
	public Map<String, String> data = new HashMap<String, String>();

	public Marker() {
		this.title = "";
		this.place = new Place();
		this.distance = 0;
	}

	public Marker(double latitude, double longitude, double altitude) {
		this.title = "";
		this.place = new Place(latitude, longitude, altitude);
		this.distance = 0;
	}

	public Marker(Marker m) {
		this.title = m.title;
		this.place = new Place(m.place);
		this.distance = m.distance;
		this.data.putAll(m.data);
	}

	public void update(Location me) {
		this.distance = place.distanceTo(me);
	}

	public String getData(String key) {
		return data.get(key);
	}

	public void setData(String key, String value) {
		data.put(key, value);
	}

	public String[] getDataKeys() {
		return data.keySet().toArray(new String[data.size()]);
	}
}