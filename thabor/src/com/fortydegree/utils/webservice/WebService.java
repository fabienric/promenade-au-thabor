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
package com.fortydegree.utils.webservice;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import com.fortydegree.utils.conversion.Converters;

public class WebService {

	protected String endpoint;
	protected String user;
	protected String password;

	public WebService(String endpoint) {
		this(endpoint, null, null);
	}

	public WebService(String endpoint, String user, String password) {
		this.endpoint = endpoint;
		this.user = user;
		this.password = password;
	}

	public InputStream get(List<String> parameters) throws WebServiceException {
		try {
			String urlString = getURL(parameters);
			URL url = new URL(urlString);
			URLConnection uc = url.openConnection();
			if (null != user && null != password) {
				String authString = Base64.encodeToString((user + ":" + password).getBytes(), Base64.NO_WRAP);
				uc.setRequestProperty("Authorization", "Basic " + authString);
			}
			return uc.getInputStream();

		} catch (Exception e) {
			throw new WebServiceException(e);
		}
	}

	public String getURL(List<String> parameters) throws WebServiceException {
		if (null == endpoint || endpoint.length() == 0) {
			throw new WebServiceException("Endpoint must not be empty");
		}

		if (null != parameters && parameters.size() % 2 > 0) {
			throw new WebServiceException("The number of parameters must be even");
		}

		StringBuffer url = new StringBuffer();
		url.append(endpoint);
		if (null != parameters) {
			url.append("?");
			for (int i = 0; i < parameters.size(); i += 2) {
				if (i > 0) {
					url.append("&");
				}
				url.append(parameters.get(i));
				url.append("=");
				url.append(parameters.get(i + 1));
			}
		}

		return Converters.URL.encode(url.toString());
	}

}
