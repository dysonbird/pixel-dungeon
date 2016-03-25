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
package com.watabou.pixeldungeon.levels;

/**
 * 地形
 * @author Administrator
 *
 */
public class Terrain {
	/**裂缝*/
	public static final int CHASM			= 0;
	/**空*/
	public static final int EMPTY			= 1;
	/**草块*/
	public static final int GRASS			= 2;
	/**空墙*/
	public static final int EMPTY_WELL		= 3;
	/**墙*/
	public static final int WALL			= 4;
	/**门*/
	public static final int DOOR			= 5;
	/**打开的门*/
	public static final int OPEN_DOOR		= 6;
	/**入口*/
	public static final int ENTRANCE		= 7;
	/**出口*/
	public static final int EXIT			= 8;
	/**余火*/
	public static final int EMBERS			= 9;
	/**锁上的门*/
	public static final int LOCKED_DOOR		= 10;
	/**底座*/
	public static final int PEDESTAL		= 11;
	/**墙装饰*/
	public static final int WALL_DECO		= 12;
	/**栅栏*/
	public static final int BARRICADE		= 13;
	/***/
	public static final int EMPTY_SP		= 14;
	/**高草*/
	public static final int HIGH_GRASS		= 15;
	/**空装饰*/
	public static final int EMPTY_DECO		= 24;
	/**锁上的出口*/
	public static final int LOCKED_EXIT		= 25;
	/**打开的出口*/
	public static final int UNLOCKED_EXIT	= 26;
	/**标记*/
	public static final int SIGN			= 29;
	/**泉*/
	public static final int WELL			= 34;
	/**雕像*/
	public static final int STATUE			= 35;
	/***/
	public static final int STATUE_SP		= 36;
	/**书架*/
	public static final int BOOKSHELF		= 41;
	/**魔力*/
	public static final int ALCHEMY			= 42;
	/**裂缝层*/
	public static final int CHASM_FLOOR		= 43;
	/***/
	public static final int CHASM_FLOOR_SP	= 44;
	/**裂缝墙*/
	public static final int CHASM_WALL		= 45;
	/**裂缝水*/
	public static final int CHASM_WATER		= 46;
	
	/**隐藏的门*/
	public static final int SECRET_DOOR				= 16;
	/**毒素陷阱*/
	public static final int TOXIC_TRAP				= 17;
	/**隐藏的毒素陷阱*/
	public static final int SECRET_TOXIC_TRAP		= 18;
	/**火陷阱*/
	public static final int FIRE_TRAP				= 19;
	/**隐藏的火陷阱*/
	public static final int SECRET_FIRE_TRAP		= 20;
	/**麻痹陷阱*/
	public static final int PARALYTIC_TRAP			= 21;
	/**隐藏的麻痹陷阱*/
	public static final int SECRET_PARALYTIC_TRAP	= 22;
	/**休眠陷阱*/
	public static final int INACTIVE_TRAP			= 23;
	/**毒陷阱*/
	public static final int POISON_TRAP				= 27;
	/**隐藏的毒陷阱*/
	public static final int SECRET_POISON_TRAP		= 28;
	/**警报陷阱*/
	public static final int ALARM_TRAP				= 30;
	/**隐藏的警报陷阱*/
	public static final int SECRET_ALARM_TRAP		= 31;
	/**雷电陷阱*/
	public static final int LIGHTNING_TRAP			= 32;
	/**隐藏的雷电陷阱*/
	public static final int SECRET_LIGHTNING_TRAP	= 33;
	/**夹子陷阱*/
	public static final int GRIPPING_TRAP			= 37;
	/**隐藏的夹子陷阱*/
	public static final int SECRET_GRIPPING_TRAP	= 38;
	/**召唤陷阱*/
	public static final int SUMMONING_TRAP			= 39;
	/**隐藏的召唤陷阱*/
	public static final int SECRET_SUMMONING_TRAP	= 40;
	
	/**水方块*/
	public static final int WATER_TILES	= 48;
	/**水*/
	public static final int WATER		= 63;
	
	/***/
	public static final int PASSABLE		= 0x01;
	/***/
	public static final int LOS_BLOCKING	= 0x02;
	/***/
	public static final int FLAMABLE		= 0x04;
	/***/
	public static final int SECRET			= 0x08;
	/***/
	public static final int SOLID			= 0x10;
	/***/
	public static final int AVOID			= 0x20;
	/***/
	public static final int LIQUID			= 0x40;
	/***/
	public static final int PIT				= 0x80;
	/***/
	public static final int UNSTITCHABLE	= 0x100; 
	
	public static final int[] flags = new int[256];
	static {
		flags[CHASM]		= AVOID	| PIT									| UNSTITCHABLE;
		flags[EMPTY]		= PASSABLE;
		flags[GRASS]		= PASSABLE | FLAMABLE;
		flags[EMPTY_WELL]	= PASSABLE;
		flags[WATER]		= PASSABLE | LIQUID 							| UNSTITCHABLE;
		flags[WALL]			= LOS_BLOCKING | SOLID 							| UNSTITCHABLE;
		flags[DOOR]			= PASSABLE | LOS_BLOCKING | FLAMABLE | SOLID	| UNSTITCHABLE;
		flags[OPEN_DOOR]	= PASSABLE | FLAMABLE 							| UNSTITCHABLE;
		flags[ENTRANCE]		= PASSABLE/* | SOLID*/;
		flags[EXIT]			= PASSABLE;
		flags[EMBERS]		= PASSABLE;
		flags[LOCKED_DOOR]	= LOS_BLOCKING | SOLID 							| UNSTITCHABLE;
		flags[PEDESTAL]		= PASSABLE 										| UNSTITCHABLE;
		flags[WALL_DECO]	= flags[WALL];
		flags[BARRICADE]	= FLAMABLE | SOLID | LOS_BLOCKING;
		flags[EMPTY_SP]		= flags[EMPTY]									| UNSTITCHABLE;
		flags[HIGH_GRASS]	= PASSABLE | LOS_BLOCKING | FLAMABLE;
		flags[EMPTY_DECO]	= flags[EMPTY];
		flags[LOCKED_EXIT]	= SOLID;
		flags[UNLOCKED_EXIT]= PASSABLE;
		flags[SIGN]			= PASSABLE | FLAMABLE;
		flags[WELL]			= AVOID;
		flags[STATUE]		= SOLID;
		flags[STATUE_SP]	= flags[STATUE] 								| UNSTITCHABLE;
		flags[BOOKSHELF]	= flags[BARRICADE]								| UNSTITCHABLE;
		flags[ALCHEMY]		= PASSABLE;
		
		flags[CHASM_WALL]		= flags[CHASM];
		flags[CHASM_FLOOR]		= flags[CHASM];
		flags[CHASM_FLOOR_SP]	= flags[CHASM];
		flags[CHASM_WATER]		= flags[CHASM];
		
		flags[SECRET_DOOR]				= flags[WALL] | SECRET				| UNSTITCHABLE;
		flags[TOXIC_TRAP]				= AVOID;
		flags[SECRET_TOXIC_TRAP]		= flags[EMPTY] | SECRET;
		flags[FIRE_TRAP]				= AVOID;
		flags[SECRET_FIRE_TRAP]			= flags[EMPTY] | SECRET;
		flags[PARALYTIC_TRAP]			= AVOID;
		flags[SECRET_PARALYTIC_TRAP]	= flags[EMPTY] | SECRET;
		flags[POISON_TRAP]				= AVOID;
		flags[SECRET_POISON_TRAP]		= flags[EMPTY] | SECRET;
		flags[ALARM_TRAP]				= AVOID;
		flags[SECRET_ALARM_TRAP]		= flags[EMPTY] | SECRET;
		flags[LIGHTNING_TRAP]			= AVOID;
		flags[SECRET_LIGHTNING_TRAP]	= flags[EMPTY] | SECRET;
		flags[GRIPPING_TRAP]			= AVOID;
		flags[SECRET_GRIPPING_TRAP]		= flags[EMPTY] | SECRET;
		flags[SUMMONING_TRAP]			= AVOID;
		flags[SECRET_SUMMONING_TRAP]	= flags[EMPTY] | SECRET;
		flags[INACTIVE_TRAP]			= flags[EMPTY];
		
		for (int i=WATER_TILES; i < WATER_TILES + 16; i++) {
			flags[i] = flags[WATER];
		}
	};
	
	public static int discover( int terr ) {
		switch (terr) {
		case SECRET_DOOR:
			return DOOR;
		case SECRET_FIRE_TRAP:
			return FIRE_TRAP;
		case SECRET_PARALYTIC_TRAP:
			return PARALYTIC_TRAP;
		case SECRET_TOXIC_TRAP:
			return TOXIC_TRAP;
		case SECRET_POISON_TRAP:
			return POISON_TRAP;
		case SECRET_ALARM_TRAP:
			return ALARM_TRAP;
		case SECRET_LIGHTNING_TRAP:
			return LIGHTNING_TRAP;
		case SECRET_GRIPPING_TRAP:
			return GRIPPING_TRAP;
		case SECRET_SUMMONING_TRAP:
			return SUMMONING_TRAP;
		default:
			return terr;
		}
	}
}
