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

public class WikipediaFormatter {

	public static String formatHtml(String wikipediaString) {
		if (wikipediaString.indexOf("== Références") > 0)
			wikipediaString = wikipediaString.substring(0, wikipediaString.indexOf("== Références"));

		if (wikipediaString.indexOf("== Liens") > 0)
			wikipediaString = wikipediaString.substring(0, wikipediaString.indexOf("== Liens"));

		wikipediaString = remove("{{", "}}", wikipediaString);
		wikipediaString = remove("[[Fichier:", "]]", wikipediaString);
		wikipediaString = replace("[[", "]]", "|", wikipediaString);
		wikipediaString = wikipediaString.replace("''", "");
		return wikipediaString.replace("== ", "<br><h2>").replace(" ==", "</h2><br>");
	}

	private static String remove(String start, String end, String s) {
		int i = s.indexOf(start);
		if (i >= 0) {
			int j = s.indexOf(end, i);
			if (j > i) {
				return remove(start, end, s.substring(0, i) + s.substring(j + end.length()));
			}
		}
		return s;
	}

	private static String replace(String start, String end, String rep, String s) {
		int i = s.indexOf(start);
		if (i >= 0) {
			int j = s.indexOf(end, i);
			if (j > i) {
				int k = s.indexOf(rep, i);
				if (k < i || k > j)
					k = i + start.length();
				else
					k = k + rep.length();

				return replace(start, end, rep, s.substring(0, i) + s.substring(k, j) + s.substring(j + end.length()));
			}
		}
		return s;
	}
}
