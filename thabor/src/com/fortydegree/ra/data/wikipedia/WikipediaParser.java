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

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

import com.fortydegree.utils.parsing.api.simple.FastMapHandler;
import com.fortydegree.utils.parsing.api.simple.SimpleSaxParser;

public class WikipediaParser {

	private static class PageHandler extends FastMapHandler {
		public String getPageContent() {
			return current.get("rev");
		}
	}

	public static WikipediaParsingResult parse(InputStream file) throws SAXException, IOException {
		WikipediaParsingResult res = null;

		PageHandler handler = new PageHandler();
		SimpleSaxParser p = new SimpleSaxParser(file, handler);
		p.parse();
		String content = handler.getPageContent();

		if (null != content) {
			res = new WikipediaParsingResult(content);
		}

		return res;
	}
}
