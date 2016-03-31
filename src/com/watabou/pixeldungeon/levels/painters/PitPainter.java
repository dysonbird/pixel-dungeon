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
package com.watabou.pixeldungeon.levels.painters;

import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.Heap.Type;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * 有一个干枯泉水口 一个骷髅的房间 骷髅会掉落不少东西
 * @author 
 *
 */
public class PitPainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY );// 随机地板
		
		Room.Door entrance = room.entrance();
		entrance.set( Room.Door.Type.LOCKED );
		
		Point well = null;
		if (entrance.x == room.left) {
			well = new Point( room.right-1, Random.Int( 2 ) == 0 ? room.top + 1 : room.bottom - 1 );
		} else if (entrance.x == room.right) {
			well = new Point( room.left+1, Random.Int( 2 ) == 0 ? room.top + 1 : room.bottom - 1 );
		} else if (entrance.y == room.top) {
			well = new Point( Random.Int( 2 ) == 0 ? room.left + 1 : room.right - 1, room.bottom-1 );
		} else if (entrance.y == room.bottom) {
			well = new Point( Random.Int( 2 ) == 0 ? room.left + 1 : room.right - 1, room.top+1 );
		}
		set( level, well, Terrain.EMPTY_WELL );// 泉水口在门的对边随机一个角
		
		int remains = room.random();
		while (level.map[remains] == Terrain.EMPTY_WELL) {
			remains = room.random();// 随机一个不是泉水口的位置摆放骷髅
		}
		
		level.drop( new IronKey(), remains ).type = Type.SKELETON;// 骷髅会掉落一把钥匙
		
		if (Random.Int( 5 ) == 0) {
			level.drop( Generator.random( Generator.Category.RING ), remains );// 五分一几率掉落戒指
		} else {
			level.drop( Generator.random( Random.oneOf( 
				Generator.Category.WEAPON, 
				Generator.Category.ARMOR
			) ), remains );// 掉落武器或者护甲
		}
		
		int n = Random.IntRange( 1, 2 );
		for (int i=0; i < n; i++) {// 掉落随机个数的随机物品
			level.drop( prize( level ), remains );
		}
	}
	
	private static Item prize( Level level ) {
		
		Item prize = level.itemToSpanAsPrize();
		if (prize != null) {
			return prize;
		}
		
		return Generator.random( Random.oneOf( 
			Generator.Category.POTION, 
			Generator.Category.SCROLL,
			Generator.Category.FOOD, 
			Generator.Category.GOLD
		) );
	}
}
