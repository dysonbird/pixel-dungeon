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

import java.util.ArrayList;
import java.util.Collections;

import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.utils.Point;

public class PassagePainter extends Painter {

	private static int pasWidth;
	private static int pasHeight;
	
	/**
	 * <p>_ _ _ _ _ _ D _ _ _</p>
	 * <p>| 1 * 6 5 4 3 2 1 |</P>
	 * <p>| 2 # # # # # # 6 |</P>
	 * <p>| 3 # # # # # # 5 |</P>
	 * <p>| 4 # # # # # # 4 |</P>
	 * <p>D 5 # # # # # # 3 |</P>
	 * <p>| 6 # # # # # # 2 D</P>
	 * <p>| 1 2 3 4 5 6 7 1 |</P>
	 * <p>| _ _ _ _ _ _ D _ |</P>
	 * 以上Room pasWidth = 7 pasHeight = 6</p>
	 * joints 为逆时针到星号的格子数</p>
	 * 所以 left.D = 2 + 2 * pasWidth + pasHeight = 22</p>
	 * bottom.D = 1 + pasWidth + pasHeight = 14</p>
	 * right.D = 5 + pasWidth = 12</p>
	 * top.D = 5</p>
	 * perimeter = 26</p>
	 * start = 0</p>
	 * end = 3 </p>
	 * maxD = 9</p>
	 * 最终的连通路径(顺时针的)为 top.D-->right.D-->bottom.D-->left.D</p>
	 * <p>_ _ _ _ _ _ D _ _ _</p>
	 * <p>| 1 * 6 5 4 ~ ~ ~ |</P>
	 * <p>| 2 # # # # # # ~ |</P>
	 * <p>| 3 # # # # # # ~ |</P>
	 * <p>| 4 # # # # # # ~ |</P>
	 * <p>D ~ # # # # # # ~ |</P>
	 * <p>| ~ # # # # # # ~ D</P>
	 * <p>| ~ ~ ~ ~ ~ ~ ~ ~ |</P>
	 * <p>| _ _ _ _ _ _ D _ |</P>
	 * @param level
	 * @param room
	 */
	public static void paint( Level level, Room room ) {
		
		pasWidth = room.width() - 2;
		pasHeight = room.height() - 2;
		
		int floor = level.tunnelTile();
		
		ArrayList<Integer> joints = new ArrayList<Integer>();
		for (Point door : room.connected.values()) {
			joints.add( xy2p( room, door ) );
		}
		Collections.sort( joints );//升序
		
		int nJoints = joints.size();
		int perimeter = pasWidth * 2 + pasHeight * 2;//边界 墙 贴墙连通一圈door的最大格子数
		
		int start = 0;
		int maxD = joints.get( 0 ) + perimeter - joints.get( nJoints - 1 );
		for (int i=1; i < nJoints; i++) {
			int d = joints.get( i ) - joints.get( i - 1 );
			if (d > maxD) {
				maxD = d;
				start = i;
			}
		}
		
		int end = (start + nJoints - 1) % nJoints;
		
		int p = joints.get( start );
		do {
			set( level, p2xy( room, p ), floor );
			p = (p + 1) % perimeter;
		} while (p != joints.get( end ));
		
		set( level, p2xy( room, p ), floor );
		
		for (Room.Door door : room.connected.values()) {
			door.set( Room.Door.Type.TUNNEL );
		}
	}
	
	/**
	 * door贴墙逆时针计算 到(room.left+2,room.top+1)的格子数
	 * @param room
	 * @param xy
	 * @return
	 */
	private static int xy2p( Room room, Point xy ) {
		if (xy.y == room.top) {//上边
			
			return (xy.x - room.left - 1);
			
		} else if (xy.x == room.right) {//右边
			
			return (xy.y - room.top - 1) + pasWidth;
			
		} else if (xy.y == room.bottom) {//下边
			
			return (room.right - xy.x - 1) + pasWidth + pasHeight;
			
		} else /*if (xy.x == room.left)*/ {//左边
			
			if (xy.y == room.top + 1) {
				return 0;
			} else {
				return (room.bottom - xy.y - 1) + pasWidth * 2 + pasHeight;
			}
			
		}
	}
	
	private static Point p2xy( Room room, int p ) {
		if (p < pasWidth) {//贴着上边墙的格子
			
			return new Point( room.left + 1 + p, room.top + 1);
			
		} else if (p < pasWidth + pasHeight) {//贴着右边墙的格子
			
			return new Point( room.right - 1, room.top + 1 + (p - pasWidth) );
			
		} else if (p < pasWidth * 2 + pasHeight) {//贴着下边墙的格子
			
			return new Point( room.right - 1 - (p - (pasWidth + pasHeight)), room.bottom - 1 );
			
		} else {//贴着左边墙的格子

			return new Point( room.left + 1, room.bottom - 1 - (p - (pasWidth * 2 + pasHeight)) );
			
		}
	}
}
