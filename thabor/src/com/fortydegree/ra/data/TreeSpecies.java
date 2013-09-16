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

public class TreeSpecies {

	public static String getSpecieCode(String latinSpecie, String frenchSpecie) {
		String specie = latinSpecie;
		if (specie == null || specie.length() == 0) {
			specie = frenchSpecie;
			if (specie == null)
				return "";
		}
		return cleanCode(specie);
	}

	public static String getSpecieCodeFR(String latinSpecie, String frenchSpecie) {
		return getSpecieCode(frenchSpecie, latinSpecie);
	}

	public static String getScreenName(String code) {
		return cleanCode(code).toUpperCase().replace(" D ", " D'").replace(" L ", " L'");
	}

	public static String getWikipediaName(String code) {
		return capitalizeFirstLetter(cleanCode(code).replace("D'", "D ").replace("L'", "L ").replace(' ', '-')
				.replace("'", "").toLowerCase());
	}

	private static String cleanCode(String code) {
		return code.replace('?', ' ').replace('_', ' ').trim();
	}

	private static String capitalizeFirstLetter(String word) {
		if (word.length() == 0)
			return "";

		return String.valueOf(word.charAt(0)).toUpperCase() + word.substring(1);
	}
}
