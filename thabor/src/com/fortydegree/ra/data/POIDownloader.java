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
package com.fortydegree.ra.data;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.util.Log;

import com.fortydegree.ra.ApplicationConstants;
import com.fortydegree.ra.model.Marker;
import com.fortydegree.utils.webservice.Downloader;
import com.fortydegree.utils.webservice.WebService;
import com.fortydegree.utils.webservice.WebServiceException;

public class POIDownloader {

	private static final String TAG = POIDownloader.class.getName();
	protected Downloader downloader;

	public POIDownloader() {
		this.downloader = new Downloader(new WebService(ApplicationConstants.GEOSERVICE_BASE_URL,
				ApplicationConstants.GEOSERVICE_USER, ApplicationConstants.GEOSERVICE_PASSWORD));
	}

	public void start() {
		this.downloader.start();
	}

	public void close() {
		this.downloader.close();
	}

	public void download(Location l, int radius, int maxResults, final CallBack<List<Marker>> places)
			throws WebServiceException {

		List<String> parameters = Arrays.asList("latitude", Double.toString(l.getLatitude()), "longitude",
				Double.toString(l.getLongitude()), "radius", Integer.toString(radius), "max",
				Integer.toString(maxResults));

		this.downloader.download(parameters, new CallBack<String>() {
			public void execute(String input) {
				try {
					if (input != null && input.length() > 0) {
						List<Marker> list = JsonUnmarshaller.load(new JSONObject(input));
						places.execute(list);
					}
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage(), e);
					throw new Error(e);// should not happen
				}
			}
		}, new CallBack<Throwable>() {
			public void execute(Throwable input) {
				Log.e(TAG, input.getMessage(), input);

			}
		});

	}

}
