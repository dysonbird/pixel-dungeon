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

import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class TreasuryPainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );//房间填满墙
		fill( level, room, 1, Terrain.EMPTY );//挖空中间
		
		set( level, room.center(), Terrain.STATUE );//中间放置一个雕像
		
		Heap.Type heapType = Random.Int( 2 ) == 0 ? Heap.Type.CHEST : Heap.Type.HEAP;//随机一个类型
		
		int n = Random.IntRange( 2, 3 );
		for (int i=0; i < n; i++) {//随机一个箱子
			int pos;
			do {
				pos = room.random();
			} while (level.map[pos] != Terrain.EMPTY || level.heaps.get( pos ) != null);
			level.drop( new Gold().random(), pos ).type = (i == 0 && heapType == Heap.Type.CHEST ? Heap.Type.MIMIC : heapType);//把金币放在地上
		}
		
		if (heapType == Heap.Type.HEAP) {
			for (int i=0; i < 6; i++) {//再随机6次直接掉落金币
				int pos;
				do {
					pos = room.random();
				} while (level.map[pos] != Terrain.EMPTY);
				level.drop( new Gold( Random.IntRange( 1, 3 ) ), pos );//把金币放在地上
			}
		}
		
		room.entrance().set( Room.Door.Type.LOCKED );//宝库的房间为锁上的
		level.addItemToSpawn( new IronKey() );//宝库的层一定生成一把钥匙
	}
}
