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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.levels.painters.*;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Graph;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;

public class Room extends Rect implements Graph.Node, Bundlable {
	
	public HashSet<Room> neigbours = new HashSet<Room>();
	/**
	 * 邻居房间的门
	 */
	public HashMap<Room, Door> connected = new HashMap<Room, Door>();
	
	public int distance;
	public int price = 1;
	
	public static enum Type {
		/**
		 * 空
		 */
		NULL( null ),
		/**
		 * 基本标准房间
		 */
		STANDARD	( StandardPainter.class ),
		/**
		 * 入口房间
		 */
		ENTRANCE	( EntrancePainter.class ),
		/**
		 * 出口房间
		 */
		EXIT		( ExitPainter.class ),
		/**
		 * boss层出口房间
		 */
		BOSS_EXIT	( BossExitPainter.class ),
		/**
		 * 隧道
		 */
		TUNNEL		( TunnelPainter.class ),
		/**
		 * 通道(贴墙顺时针连通的路径)
		 */
		PASSAGE		( PassagePainter.class ),
		/**
		 * 商店
		 */
		SHOP		( ShopPainter.class ),
		/**
		 * 铁匠铺
		 */
		BLACKSMITH	( BlacksmithPainter.class ),
		/**
		 * 宝库
		 */
		TREASURY	( TreasuryPainter.class ),
		/**
		 * 军械库
		 */
		ARMORY		( ArmoryPainter.class ),
		/**
		 * 图书馆
		 */
		LIBRARY		( LibraryPainter.class ),
		/**
		 * 实验室
		 */
		LABORATORY	( LaboratoryPainter.class ),
		/**
		 * 墓穴
		 */
		VAULT		( VaultPainter.class ),
		/**
		 * 陷阱
		 */
		TRAPS		( TrapsPainter.class ),
		/**
		 * 仓库
		 */
		STORAGE		( StoragePainter.class ),
		/**
		 * 魔法泉
		 */
		MAGIC_WELL	( MagicWellPainter.class ),
		/**
		 * 花园
		 */
		GARDEN		( GardenPainter.class ),
		/**
		 * 地窖
		 */
		CRYPT		( CryptPainter.class ),
		/**
		 * 雕像
		 */
		STATUE		( StatuePainter.class ),
		/**
		 * 水池
		 */
		POOL		( PoolPainter.class ),
		/**
		 * 鼠王
		 */
		RAT_KING	( RatKingPainter.class ),
		/**
		 * 弱大厅
		 */
		WEAK_FLOOR	( WeakFloorPainter.class ),
		/**
		 * 地穴
		 */
		PIT			( PitPainter.class ),
		/**
		 * 祭坛
		 */
		ALTAR		( AltarPainter.class );
		
		private Method paint;
		
		private Type( Class<? extends Painter> painter ) {
			try {
				paint = painter.getMethod( "paint", Level.class, Room.class );
			} catch (Exception e) {
				paint = null;
			}
		}
		
		public void paint( Level level, Room room ) {
			try {
				paint.invoke( null, level, room );
			} catch (Exception e) {
				PixelDungeon.reportException( e );
			}
		}
	};
	
	public static final ArrayList<Type> SPECIALS = new ArrayList<Type>( Arrays.asList(
		Type.ARMORY, Type.WEAK_FLOOR, Type.MAGIC_WELL, Type.CRYPT, Type.POOL, Type.GARDEN, Type.LIBRARY,
		Type.TREASURY, Type.TRAPS, Type.STORAGE, Type.STATUE, Type.LABORATORY, Type.VAULT, Type.ALTAR
	) );
	
	public Type type = Type.NULL;
	
	/**
	 * 返回房间内除了围墙的随机格子下标
	 * @return
	 */
	public int random() {
		return random( 0 );
	}
	
	/**
	 * 在room中随机一块有效方块出来 不包括墙
	 * @param m
	 * @return
	 */
	public int random( int m ) {
		int x = Random.Int( left + 1 + m, right - m );
		int y = Random.Int( top + 1 + m, bottom - m );
		return x + y * Level.WIDTH;
	}
	
	/**
	 * 把相连的房间记录下来(相连规则为至少有三块格子相交)
	 * @param other
	 */
	public void addNeigbour( Room other ) {
		
		Rect i = intersect( other );
		if ((i.width() == 0 && i.height() >= 3) || 
			(i.height() == 0 && i.width() >= 3)) {
			neigbours.add( other );
			other.neigbours.add( this );
		}
		
	}
	
	/**
	 * 记录到出口的路径 (Room,Door)
	 * @param room 
	 */
	public void connect( Room room ) {
		if (!connected.containsKey( room )) {	
			connected.put( room, null );
			room.connected.put( this, null );			
		}
	}
	
	public Door entrance() {
		return connected.values().iterator().next();
	}
	
	public boolean inside( int p ) {
		int x = p % Level.WIDTH;
		int y = p / Level.WIDTH;
		return x > left && y > top && x < right && y < bottom;
	}
	
	public Point center() {
		return new Point( 
			(left + right) / 2 + (((right - left) & 1) == 1 ? Random.Int( 2 ) : 0),
			(top + bottom) / 2 + (((bottom - top) & 1) == 1 ? Random.Int( 2 ) : 0) );
	}
	
	// **** Graph.Node interface ****
	
	/**
	 * 到出口的距离
	 */
	@Override
	public int distance() {
		return distance;
	}

	@Override
	public void distance( int value ) {
		distance = value;
	}
	
	@Override
	public int price() {
		return price;
	}

	@Override
	public void price( int value ) {
		price = value;
	}

	/**
	 * 邻居
	 */
	@Override
	public Collection<Room> edges() {
		return neigbours;
	} 
	
	// FIXME: use proper string constants
	
	@Override
	public void storeInBundle( Bundle bundle ) {	
		bundle.put( "left", left );
		bundle.put( "top", top );
		bundle.put( "right", right );
		bundle.put( "bottom", bottom );
		bundle.put( "type", type.toString() );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		left = bundle.getInt( "left" );
		top = bundle.getInt( "top" );
		right = bundle.getInt( "right" );
		bottom = bundle.getInt( "bottom" );		
		type = Type.valueOf( bundle.getString( "type" ) );
	}
	
	public static void shuffleTypes() {
		int size = SPECIALS.size();
		for (int i=0; i < size - 1; i++) {
			int j = Random.Int( i, size );
			if (j != i) {
				Type t = SPECIALS.get( i );
				SPECIALS.set( i, SPECIALS.get( j ) );
				SPECIALS.set( j, t );
			}
		}
	}
	
	/**
	 * 用于验证type可用？
	 * @param type
	 */
	public static void useType( Type type ) {
		if (SPECIALS.remove( type )) {
			SPECIALS.add( type );
		}
	}
	
	private static final String ROOMS	= "rooms";
	
	public static void restoreRoomsFromBundle( Bundle bundle ) {
		if (bundle.contains( ROOMS )) {
			SPECIALS.clear();
			for (String type : bundle.getStringArray( ROOMS )) {
				SPECIALS.add( Type.valueOf( type ));
			}
		} else {
			shuffleTypes();
		}
	}
	
	public static void storeRoomsInBundle( Bundle bundle ) {
		String[] array = new String[SPECIALS.size()];
		for (int i=0; i < array.length; i++) {
			array[i] = SPECIALS.get( i ).toString();
		}
		bundle.put( ROOMS, array );
	}
	
	public static class Door extends Point {
		
		public static enum Type {
			/**空门*/
			EMPTY, 
			/**隧道*/
			TUNNEL, 
			/**普通*/
			REGULAR, 
			/**开启的*/
			UNLOCKED, 
			/**隐藏的门*/
			HIDDEN, 
			/**栅栏*/
			BARRICADE, 
			/**有锁的门*/
			LOCKED
		}
		public Type type = Type.EMPTY;
		
		public Door( int x, int y ) {
			super( x, y );
		}
		
		/**
		 * 设置door的类型,高优先级的会覆盖低优先级的
		 * @param type
		 */
		public void set( Type type ) {
			if (type.compareTo( this.type ) > 0) {//根据优先级设置房间类型
				this.type = type;
			}
		}
	}
}
