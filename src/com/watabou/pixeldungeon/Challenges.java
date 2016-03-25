/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon;

public class Challenges {
	
	/**无食物*/
	public static final int NO_FOOD				= 1;
	/**无武器*/
	public static final int NO_ARMOR			= 2;
	/**不能治疗*/
	public static final int NO_HEALING			= 4;
	/**无草药*/
	public static final int NO_HERBALISM		= 8;
	/**虫群*/
	public static final int SWARM_INTELLIGENCE	= 16;
	/**黑暗*/
	public static final int DARKNESS			= 32;
	/**无卷轴*/
	public static final int NO_SCROLLS			= 64;
	
	public static final String[] NAMES = {
		"On diet",
		"Faith is my armor",
		"Pharmacophobia",
		"Barren land",
		"Swarm intelligence",
		"Into darkness",
		"Forbidden runes"
	};
	
	public static final int[] MASKS = {
		NO_FOOD, NO_ARMOR, NO_HEALING, NO_HERBALISM, SWARM_INTELLIGENCE, DARKNESS, NO_SCROLLS
	};
	
}
