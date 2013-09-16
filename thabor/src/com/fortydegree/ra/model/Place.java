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

import android.location.Location;

public class Place {

	public double latitude;
	public double longitude;
	public double altitude;

	public static Place NULL = new Place(0, 0, 0);

	public Place() {

	}

	public Place(Place pl) {
		this(pl.latitude, pl.longitude, pl.altitude);
	}

	public Place(double latitude, double longitude, double altitude) {
		this.setTo(latitude, longitude, altitude);
	}

	public void setTo(double latitude, double longitude, double altitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}

	public void setTo(Place pl) {
		this.latitude = pl.latitude;
		this.longitude = pl.longitude;
		this.altitude = pl.altitude;
	}

	@Override
	public String toString() {
		return "(lat=" + latitude + ", lng=" + longitude + ", alt=" + altitude + ")";
	}

	public float distanceTo(Location l) {
		float[] z = new float[1];
		z[0] = 0;
		Location.distanceBetween(l.getLatitude(), l.getLongitude(), this.latitude, this.longitude, z);
		return z[0];
	}
}
