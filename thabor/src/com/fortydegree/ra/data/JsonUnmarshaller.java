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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fortydegree.ra.model.Marker;

public class JsonUnmarshaller {

	public static final int MAX_JSON_OBJECTS = 1000;

	public static List<Marker> load(JSONObject root) throws JSONException {
		JSONObject jo = null;
		JSONArray dataArray = null;
		List<Marker> markers = new ArrayList<Marker>();

		if (root.has("results")) {
			dataArray = root.getJSONArray("results");

			int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

			for (int i = 0; i < top; i++) {
				jo = dataArray.getJSONObject(i);
				Marker ma = processGeoserviceJSONObject(jo);
				if (ma != null)
					markers.add(ma);
			}
		}

		return markers;
	}

	public static Marker processGeoserviceJSONObject(JSONObject jo) throws JSONException {
		String type = jo.getString("type");
		String metadata = jo.getString("metadata");
		JSONObject jsonMetadata = new JSONObject(metadata);
		String title = jsonMetadata.optString("title");

		Marker m = new Marker(jo.getDouble("latitude"), jo.getDouble("longitude"), jo.getDouble("altitude"));
		m.title = title;
		m.distance = jo.getDouble("distance");

		@SuppressWarnings("rawtypes")
		Iterator metadataIter = jsonMetadata.keys();
		while (metadataIter.hasNext()) {
			String key = metadataIter.next().toString();
			m.setData(key, jsonMetadata.getString(key));
		}
		m.setData("title", title);
		m.setData("type", type);

		return m;
	}

}