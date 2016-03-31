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

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.blobs.SacrificialFire;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Point;

public class AltarPainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );// 一圈墙
		fill( level, room, 1, Dungeon.bossLevel( Dungeon.depth + 1 ) ? Terrain.HIGH_GRASS : Terrain.CHASM );// 其余填上裂缝或者高草
		
		Point center = room.center();
		Room.Door door = room.entrance();
		/*从门口铺木地板到祭坛位置 祭坛就在房间的中间*/
		if (door.x == room.left || door.x == room.right) {
			Point p = drawInside( level, room, door, Math.abs( door.x - center.x ) - 2, Terrain.EMPTY_SP );
			for (; p.y != center.y; p.y += p.y < center.y ? +1 : -1) {
				set( level, p, Terrain.EMPTY_SP );
			}
		} else {
			Point p = drawInside( level, room, door, Math.abs( door.y - center.y ) - 2, Terrain.EMPTY_SP );
			for (; p.x != center.x; p.x += p.x < center.x ? +1 : -1) {
				set( level, p, Terrain.EMPTY_SP );
			}
		}

		fill( level, center.x - 1, center.y - 1, 3, 3, Terrain.EMBERS );// 放置一块3X3的余烬格子
		set( level, center, Terrain.PEDESTAL );// 底座充当祭坛 放在房间中间
		
		SacrificialFire fire = (SacrificialFire)level.blobs.get( SacrificialFire.class );
		if (fire == null) {
			fire = new SacrificialFire();
		}
		fire.seed( center.x + center.y * Level.WIDTH, 5 + Dungeon.depth * 5 );// 火焰位置和数量 
		level.blobs.put( SacrificialFire.class, fire );
		
		door.set( Room.Door.Type.EMPTY );
	}
}
