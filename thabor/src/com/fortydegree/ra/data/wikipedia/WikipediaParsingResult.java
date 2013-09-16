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
package com.fortydegree.ra.data.wikipedia;

public class WikipediaParsingResult {

	public boolean redirect = false;
	public String content;

	public WikipediaParsingResult(String content) {
		if (content.startsWith("#REDIRECT")) {
			redirect = true;
			int i = content.indexOf("[[");
			int j = content.indexOf("]]");
			this.content = content.substring(i + 2, j);
		} else {
			redirect = false;
			this.content = content;
		}
	}
}
