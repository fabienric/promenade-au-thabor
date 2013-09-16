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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fortydegree.utils.webservice.WebService;

public class WikipediaClient {

	private static final int SEARCH_LIMIT = 10; // default is 10 (see
												// https://www.mediawiki.org/wiki/API:Opensearch)
	protected WebService wikipediaService = null;
	protected Map<String, String> cache = new HashMap<String, String>();
	protected boolean cacheEnabled = true;

	public WikipediaClient(String wikipediaEndpoint) {
		this(new WebService(wikipediaEndpoint), true);
	}

	public WikipediaClient(WebService wikipediaService) {
		this(wikipediaService, true);
	}

	public WikipediaClient(WebService wikipediaService, boolean cacheEnabled) {
		this.wikipediaService = wikipediaService;
		this.cacheEnabled = cacheEnabled;
	}

	public String getPage(String name) {
		return getPage(name, true);
	}

	public String getFromCache(String name) {
		String key = getCacheKey("page", name);
		return getFromCacheWithKey(key);
	}

	protected String getFromCacheWithKey(String key) {
		if (isCacheEnabled() && cache.containsKey(key)) {
			return cache.get(key);
		}
		return null;
	}

	protected String getPage(String name, boolean search) {
		try {

			String key = getCacheKey("page", name);
			String cached = getFromCacheWithKey(key);
			if (cached != null)
				return cached;

			String res = null;

			// on recherche la 1ere page correspondante
			String key2 = null;
			if (search) {
				List<String> searchResults = search(name, 1);
				if (searchResults != null && searchResults.size() > 0) {
					// on recupere le premier resultat
					String searchRes = searchResults.get(0);
					if (searchRes != null && searchRes.length() > 0) {
						name = searchRes;
						key2 = getCacheKey("page", name);
					}
				}
			}

			String[] parameters = new String[] { "action", "query", "prop", "revisions", "rvprop", "content",
					"redirects", "true", "format", "xml", "titles", name };

			InputStream s = wikipediaService.get(Arrays.asList(parameters));
			if (s != null) {
				WikipediaParsingResult r = WikipediaParser.parse(s);
				s.close();

				if (r != null) {
					if (r.redirect) {
						res = getPage(r.content, false);
					} else {
						res = r.content;
						if (res != null) {
							res = WikipediaFormatter.formatHtml(res);
						}
					}
				}
			}

			// Log.debug(this, " content for " + name);

			if (isCacheEnabled())
				cache.put(key, res);

			if (search && key2 != null)
				cache.put(key2, res);

			return res;
		} catch (Exception e) {
			return null;
		}

	}

	public List<String> search(String name) {
		return search(name, SEARCH_LIMIT);
	}

	public List<String> search(String name, int limit) {
		try {
			List<String> parameters = Arrays.asList("action", "opensearch", "search", name, "limit",
					Integer.toString(limit));
			InputStream s = wikipediaService.get(parameters);
			String searchResult = getString(s);
			s.close();

			List<String> results = new ArrayList<String>();
			int i = searchResult.indexOf(",[") + 2;
			int j = searchResult.indexOf("]", i);
			String[] split = searchResult.substring(i, j).split(",");
			for (int k = 0; k < split.length; k++) {
				String result = split[k];
				results.add(result.substring(1, result.length() - 1));
			}

			return results;
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

	public void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;

		if (!cacheEnabled)
			clearCache();
	}

	public void clearCache() {
		cache.clear();
	}

	protected static String getCacheKey(String... parameters) {
		String key = "";
		for (int i = 0; i < parameters.length; i++) {
			key += (i > 0 ? "," : "") + i + ":" + parameters[i];
		}
		return key;
	}

	protected static String getString(InputStream s) throws IOException {
		if (s == null)
			return null;

		StringWriter sw = new StringWriter();
		java.io.InputStreamReader isr = new java.io.InputStreamReader(s);
		char[] buffer = new char[1024];

		while (isr.read(buffer) > 0)
			sw.write(buffer);

		return sw.toString();
	}
}
