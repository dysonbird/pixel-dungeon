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
import com.watabou.pixeldungeon.items.keys.GoldenKey;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

/**
 * 墓穴
 * @author 
 *
 */
public class VaultPainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY_SP );
		fill( level, room, 2, Terrain.EMPTY );
		
		int cx = (room.left + room.right) / 2;
		int cy = (room.top + room.bottom) / 2;
		int c = cx + cy * Level.WIDTH;// center
		
		switch (Random.Int( 3 )) {
		
		case 0:
			level.drop( prize(), c ).type = Type.LOCKED_CHEST;// 锁上的箱子 需要金钥匙
			level.addItemToSpawn( new GoldenKey() );// 加入金钥匙
			break;
			
		case 1:
			Item i1, i2;// 两个不同的物品
			do {
				i1 = prize();
				i2 = prize();
			} while (i1.getClass() == i2.getClass());
			level.drop( i1, c ).type = Type.CRYSTAL_CHEST;// 水晶箱子
			level.drop( i2, c + Level.NEIGHBOURS8[Random.Int( 8 )]).type = Type.CRYSTAL_CHEST;// 在邻居房间中放置一个水晶箱子
			level.addItemToSpawn( new GoldenKey() );// 金钥匙
			break;
			
		case 2:
			level.drop( prize(), c );
			set( level, c, Terrain.PEDESTAL );// 放置一个底座 打爆会掉落物品
			break;
		}
		
		room.entrance().set( Room.Door.Type.LOCKED );// 锁上
		level.addItemToSpawn( new IronKey() );// 加入铁钥匙
	}
	
	/**
	 * 随机返回魔杖 或者 戒指
	 * @param level
	 * @return
	 */
	private static Item prize() {
		return Generator.random( Random.oneOf(  
			Generator.Category.WAND, 
			Generator.Category.RING 
		) );
	}
}
