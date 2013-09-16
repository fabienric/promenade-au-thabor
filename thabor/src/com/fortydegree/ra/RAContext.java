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
package com.fortydegree.ra;

import android.hardware.SensorManager;
import android.location.LocationManager;

import com.fortydegree.ra.model.POIFinder;
import com.fortydegree.ra.model.PositionSensor;

public class RAContext {

	protected POIFinder finder;
	protected PositionSensor sensor;

	public RAContext() {
		this.sensor = new PositionSensor();
		this.finder = new POIFinder(sensor);
		if (finder != null)
			finder.start();
	}

	public void init(SensorManager sensorManager, LocationManager locationManager) {
		if (sensor != null)
			sensor.init(sensorManager, locationManager);
	}

	public void pause(SensorManager sensorManager, LocationManager locationManager) {
		if (sensor != null)
			sensor.close(sensorManager, locationManager);

	}

	public void close() {
		if (finder != null)
			finder.close();
	}

	public POIFinder getFinder() {
		return finder;
	}

	public PositionSensor getSensor() {
		return sensor;
	}

}