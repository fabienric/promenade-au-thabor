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

import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.fortydegree.ra.ApplicationConstants;
import com.fortydegree.ra.model.mvc.BaseEventPublisher;
import com.fortydegree.ra.model.mvc.IEventPublisher;
import com.fortydegree.ra.model.mvc.IEventSubscriber;

/**
 * Location and orientation sensor
 * 
 */
public final class PositionSensor implements LocationListener, SensorEventListener, IEventPublisher {

	// Sensor update frequency
	private final static int SENSOR_REFRESH_MS = 500;
	private float angle = 0;
	private long lastOrientationUpdate = System.currentTimeMillis();
	private long lastGpsUpdate = System.currentTimeMillis();
	private boolean gpsEnabled = false;
	private boolean hasGps = false;
	private boolean hasOrientationSensor = false;

	protected Location location;

	protected IEventPublisher eventPublisher = new BaseEventPublisher(this);

	public void close(SensorManager m, LocationManager locationManager) {
		m.unregisterListener(this);
		locationManager.removeUpdates(this);
	}

	public void init(SensorManager m, LocationManager locationManager) {

		// init orientation sensor
		Sensor orientationSensor = m.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		if (orientationSensor != null) {
			hasOrientationSensor = m.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_UI);
		}

		// init location sensor
		if (null != locationManager) {
			List<String> providers = locationManager.getAllProviders();

			if (providers.size() > 0) {
				// GPS is preferred
				String locationProvider = LocationManager.GPS_PROVIDER;
				hasGps = providers.contains(locationProvider);

				if (!hasGps) {
					locationProvider = providers.get(0);
				}

				boolean isProviderEnabled = (null != locationProvider)
						&& (locationManager.isProviderEnabled(locationProvider));
				gpsEnabled = isProviderEnabled && hasGps;

				if (isProviderEnabled) {
					locationManager.requestLocationUpdates(locationProvider,
							ApplicationConstants.GEOSERVICE_GPS_UPDATE_TIME,
							ApplicationConstants.GEOSERVICE_GPS_UPDATE_SPACE, this);
					setLocation(locationManager.getLastKnownLocation(locationProvider));

				}
			}
		}
	}

	public void onLocationChanged(Location location) {

		long currTime = System.currentTimeMillis();
		if (currTime - lastGpsUpdate < SENSOR_REFRESH_MS)
			return;
		lastGpsUpdate = System.currentTimeMillis();

		setLocation(location);
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {

		long currTime = System.currentTimeMillis();

		float values[] = event.values;

		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			if (currTime - lastOrientationUpdate >= SENSOR_REFRESH_MS) {
				lastOrientationUpdate = System.currentTimeMillis();
				setAngle(values[0]);
			}
		}
	}

	public void setAngle(float angle) {
		this.angle = angle;
		publish("angle");
	}

	public void setLocation(Location location) {
		if (location == null)
			return;
		this.location = location;
		publish("location");
	}

	public Location getLocation() {
		return location;
	}

	public float getAngle() {
		return angle;
	}

	public void register(IEventSubscriber subscriber) {
		eventPublisher.register(subscriber);
	}

	public void unregister(IEventSubscriber subscriber) {
		eventPublisher.unregister(subscriber);
	}

	public void publish(String eventName) {
		eventPublisher.publish(eventName);
	}

	public boolean isGpsEnabled() {
		return gpsEnabled;
	}

	public boolean hasGps() {
		return hasGps;
	}

	public boolean hasOrientationSensor() {
		return hasOrientationSensor;
	}
}