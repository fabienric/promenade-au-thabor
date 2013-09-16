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
package com.fortydegree.utils.parsing.api.simple;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

public class FastMapHandler implements SimpleHandler {

	protected Map<String, String> current = new HashMap<String, String>();

	public void end(String name) {
		// nothing to do
	}

	public void start(String name, Attributes attrs) {
		for (int i = 0; i < attrs.getLength(); i++) {
			current.put(attrs.getLocalName(i), attrs.getValue(i));
		}
	}

	public void text(String name, String text) {
		current.put(name, text);
	}

}
