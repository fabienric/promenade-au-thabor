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

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.util.Log;

import com.fortydegree.ra.ApplicationConstants;
import com.fortydegree.ra.data.CallBack;
import com.fortydegree.ra.data.POIDownloader;
import com.fortydegree.ra.model.mvc.BaseEventPublisher;
import com.fortydegree.ra.model.mvc.IEventPublisher;
import com.fortydegree.ra.model.mvc.IEventSubscriber;
import com.fortydegree.utils.webservice.WebServiceException;

public class POIFinder implements IEventPublisher, IEventSubscriber {

	private static String TAG = POIFinder.class.getName();

	protected PositionSensor sensor;
	protected Location lastDownloadLoc;
	protected POIDownloader downloader;
	protected List<Marker> places;

	public static enum FindInfo {
		OK, TOO_NEAR, TOO_FAR
	};

	protected Marker theGoodPlace;
	protected FindInfo info;

	protected boolean stopped = false;

	protected IEventPublisher eventPublisher = new BaseEventPublisher(this);
	protected CalcResult resultinfo = new CalcResult();

	public CalcResult getResultinfo() {
		return resultinfo;
	}

	public class CalcResult {
		public boolean result = false;
		public float angle;
		public float distance;
		public float radius;
		public FindInfo info;
	}

	public POIFinder(PositionSensor sensor) {
		this.sensor = sensor;
		this.sensor.register(this);
		this.downloader = new POIDownloader();
	}

	public void start() {
		this.downloader.start();
		update(this.sensor.getLocation());
	}

	public void close() {
		this.downloader.close();
	}

	protected void update(final Location newLoc) {
		boolean needDownload = false;

		if (lastDownloadLoc == null) {
			lastDownloadLoc = newLoc;
			needDownload = true;
		} else {
			needDownload = newLoc.distanceTo(lastDownloadLoc) > ApplicationConstants.GEOSERVICE_RADIUS_DOWNLOAD;
		}

		if (needDownload) {
			try {
				download(newLoc);
			} catch (WebServiceException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	protected void download(final Location newLoc) throws WebServiceException {

		lastDownloadLoc = newLoc;

		if (newLoc != null) {
			downloader.download(newLoc, ApplicationConstants.GEOSERVICE_RADIUS_OBJECTS,
					ApplicationConstants.GEOSERVICE_MAXRESULTS, new CallBack<List<Marker>>() {
						public void execute(List<Marker> input) {
							POIFinder.this.places = input;
						}
					});
		}
	}

	protected void updateDistances(Location me) {
		if (places == null)
			return;

		// update distances
		double min = Double.MAX_VALUE;
		Marker nearest = null;
		Marker best = null;
		CalcResult bestinfo = null;
		CalcResult nearestInfo = null;
		info = FindInfo.TOO_FAR;
		for (Marker m : places) {
			m.update(me);
			bestinfo = isPointInCam(me, m.place, this.sensor.getAngle());
			if (m.distance < min) {
				nearest = m;
				nearestInfo = bestinfo;
				min = m.distance;
			}

			if (bestinfo.result) {
				best = m;
				info = FindInfo.OK;
				resultinfo = bestinfo;
				break;
			}
		}
		if (info != FindInfo.OK && nearest != null) {
			resultinfo = nearestInfo;
			if (isNear(me, nearest.place)) {
				info = FindInfo.TOO_NEAR;
			} else {
				info = FindInfo.TOO_FAR;
			}
			best = nearest;
		}
		setTheGoodPlace(best);
	}

	protected float getRadiusAccuracy(float distance) {
		return (float) (Math.atan(1 / distance) * 2 * 180 / Math.PI);
	}

	protected float getAngle(Location me, double latitude, double longitude) {
		return (float) ((Math.atan2(latitude - me.getLatitude(), longitude - me.getLongitude())) * 180 / Math.PI);
	}

	protected float getDistance(Location me, Location l) {
		return me.distanceTo(l);
	}

	public float getTrigAngle(float bAngle) {
		float trigAngle = (360 - bAngle);
		if (trigAngle >= 360)
			trigAngle -= 360;
		return trigAngle;
	}

	public CalcResult isPointInCam(Location me, Place l, float bAngle) {
		CalcResult res = new CalcResult();
		if (!l.equals(Place.NULL)) {
			res.distance = l.distanceTo(me);
			res.angle = getAngle(me, l.latitude, l.longitude);

			float angletrigo = getTrigAngle(bAngle);
			res.radius = (int) diff(angletrigo, res.angle);

			boolean distanceok = res.distance < 30;
			boolean angleok = Math.abs(res.radius) < 40;

			res.result = distanceok && angleok;
		}
		return res;
	}

	protected double diff(double firstAngle, double secondAngle) {
		double difference = secondAngle - firstAngle;
		while (difference < -180)
			difference += 360;
		while (difference > 180)
			difference -= 360;
		return difference;
	}

	public float addToAngle(float a, float b) {
		float res = a + b;
		if (res < 0)
			return 360 + res;
		else if (res > 360)
			return res - 360;
		return res;
	}

	public boolean isNear(Location me, Place pl) {
		if (!pl.equals(Place.NULL)) {
			float distance = pl.distanceTo(me);
			return distance < 30;
		}
		return false;
	}

	public Marker getTheGoodPlace() {
		return theGoodPlace;
	}

	public void setTheGoodPlace(Marker theGoodPlace) {
		this.theGoodPlace = theGoodPlace;
		publish("update");
	}

	public FindInfo getInfo() {
		return info;
	}

	public void setInfo(FindInfo info) {
		this.info = info;
	}

	public void eventReceived(String eventName, Object source) {
		if (source.equals(this.sensor) && "location".equals(eventName)) {
			Location newLoc = this.sensor.getLocation();
			update(newLoc);
		}
		if (source.equals(this.sensor) && "angle".equals(eventName)) {
			Location newLoc = this.sensor.getLocation();
			updateDistances(newLoc);
		}
	}

	public void register(IEventSubscriber subscriber) {
		eventPublisher.register(subscriber);
	}

	public void publish(String eventName) {
		eventPublisher.publish(eventName);
	}

	public void unregister(IEventSubscriber subscriber) {
		eventPublisher.unregister(subscriber);
	}

	public Marker[] getMarkers() {
		if (places.size() == 0)
			return new Marker[0];
		Marker[] res = new Marker[places.size()];
		for (int i = 0; i < places.size(); i++) {
			res[i] = new Marker(places.get(i));
		}
		return res;
	}

	public void setMarkers(Marker[] markers) {
		if (places == null) {
			places = new ArrayList<Marker>();
		} else {
			places.clear();
		}

		for (int i = 0; i < markers.length; i++) {
			places.add(markers[i]);
		}
	}
}
