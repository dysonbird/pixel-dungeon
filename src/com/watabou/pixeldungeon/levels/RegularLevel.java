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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import android.util.Log;

import com.watabou.pixeldungeon.Bones;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.levels.Room.Type;
import com.watabou.pixeldungeon.levels.painters.*;
import com.watabou.utils.Bundle;
import com.watabou.utils.Graph;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;

public abstract class RegularLevel extends Level {

	protected HashSet<Room> rooms;// 房间
	
	protected Room roomEntrance;
	protected Room roomExit;
	
	protected ArrayList<Room.Type> specials;
	
	public int secretDoors;
	
	@Override
	protected boolean build() {
		
		if (!initRooms()) {// 切分房间
			return false;
		}
		
		/*-----初始化出入口-----begin*/
		int distance;
		int retry = 0;
		int minDistance = (int)Math.sqrt( rooms.size() );
		do {
			do {
				roomEntrance = Random.element( rooms );// 入口
			} while (roomEntrance.width() < 4 || roomEntrance.height() < 4);
			
			do {
				roomExit = Random.element( rooms );// 出口
			} while (roomExit == roomEntrance || roomExit.width() < 4 || roomExit.height() < 4);
	
			Graph.buildDistanceMap( rooms, roomExit );// 计算每个房间到出口房间的距离
			distance = roomEntrance.distance();
			
			if (retry++ > 10) {// 超过10次 生产的地图房间都不能满足条件 重新build
				return false;
			}
			
		} while (distance < minDistance);// 入口和出口的距离不能少于总房间个数的开方
		/*-----初始化出入口-----end*/
		
		// 标记出入口房间类型
		roomEntrance.type = Type.ENTRANCE;
		roomExit.type = Type.EXIT;
		
		/*----计算两次路径,这样可以造成到出口不止一种走法-------begin*/
		HashSet<Room> connected = new HashSet<Room>();// 入口到出口的最短路径,必须有door连接的房间列表
		connected.add( roomEntrance );
		
		Graph.buildDistanceMap( rooms, roomExit );// 为什么又重新计算离出口的距离？
		List<Room> path = Graph.buildPath( rooms, roomEntrance, roomExit );
		
		/*第一条入口到出口的路径*/
		Room room = roomEntrance;
		for (Room next : path) {
			room.connect( next );
			room = next;
			connected.add( room );
		}
		
		Graph.setPrice(path, roomEntrance.distance );// 最短路径权重设置为入口的到出口的距离
		
		Graph.buildDistanceMap( rooms, roomExit);// 权重发生变化,重新计算各个房间到出口的距离
		path = Graph.buildPath( rooms, roomEntrance, roomExit );//权重变化后 再计算出一条最短路径
		
		/*第二条入口到出口的路径*/
		room = roomEntrance;
		for (Room next : path) {
			room.connect( next );
			room = next;
			connected.add( room );
		}
		
		/*随机打通两个房间 Random.Float( 0.5f, 0.7f )保证最少50%的房间连通，最多70%*/
		int nConnected = (int)(rooms.size() * Random.Float( 0.5f, 0.7f ));
		while (connected.size() < nConnected) {// 把能到达出口的路径的随机房间的邻居随机加入到路劲
			Room cr = Random.element( connected );
			Room or = Random.element( cr.neigbours );
			if (!connected.contains( or )) {
				cr.connect( or );
				connected.add( or );
			}
		}
		/*------------end*/
		
		if (Dungeon.shopOnLevel()) {
			Room shop = null;
			for (Room r : roomEntrance.connected.keySet()) {//在去出口的路径上选一个足够大的房间作为商店 保证了商店可达
				if (r.connected.size() == 1 && r.width() >= 5 && r.height() >= 5) {
					shop = r;
					break;
				}
			}
			
			if (shop == null) {
				return false;
			} else {
				shop.type = Room.Type.SHOP;// 修改房间类型为商店
			}
		}
		
		specials = new ArrayList<Room.Type>( Room.SPECIALS );
		if (Dungeon.bossLevel( Dungeon.depth + 1 )) {//下一层为boss层就去掉 weak_floor的房间类型
			specials.remove( Room.Type.WEAK_FLOOR );
		}
		assignRoomType();
		
		paint();
		paintWater();
		paintGrass();
		
		placeTraps();
		
		return true;
	}
	
	/**
	 * 在地图中划分房间并计算房间的连通性(即邻居)
	 * @return
	 */
	protected boolean initRooms() {
		rooms = new HashSet<Room>();
		split( new Rect( 0, 0, WIDTH - 1, HEIGHT - 1 ) );
		
		if (rooms.size() < 8) {
			return false;
		}
		
		// 计算每个房间到达
		Room[] ra = rooms.toArray( new Room[0] );
		for (int i=0; i < ra.length-1; i++) {
			for (int j=i+1; j < ra.length; j++) {
				ra[i].addNeigbour( ra[j] );
			}
		}
		
		return true;
	}
	
	/**
	 * 房间类型赋值
	 */
	protected void assignRoomType() {
		
		int specialRooms = 0;
		for (Room r : rooms) {
			if (r.type == Type.NULL && r.connected.size() == 1) {//单一入口的房间
				if (specials.size() > 0 
						&& r.width() > 3 
						&& r.height() > 3 
						&& Random.Int( specialRooms * specialRooms + 2 ) == 0) {//对足够大的房间的类型进行随机赋值

					if (pitRoomNeeded) {

						r.type = Type.PIT;
						pitRoomNeeded = false;

						specials.remove( Type.ARMORY );
						specials.remove( Type.CRYPT );
						specials.remove( Type.LABORATORY );
						specials.remove( Type.LIBRARY );
						specials.remove( Type.STATUE );
						specials.remove( Type.TREASURY );
						specials.remove( Type.VAULT );
						specials.remove( Type.WEAK_FLOOR );
						
					} else if (Dungeon.depth % 5 == 2 && specials.contains( Type.LABORATORY )) {
						
						r.type = Type.LABORATORY;
						
					} else {
						
						int n = specials.size();
						r.type = specials.get( Math.min( Random.Int( n ), Random.Int( n ) ) );
						if (r.type == Type.WEAK_FLOOR) {
							weakFloorCreated = true;
						}
					}
					
					Room.useType( r.type );
					specials.remove( r.type );
					specialRooms++;
					
				} else if (Random.Int(2) == 0){//三分之一的机会 随机连通周围一个不是pit的邻居
					HashSet<Room> neigbours = new HashSet<Room>();
					for (Room n : r.neigbours) {
						if (!r.connected.containsKey( n ) 
								&& !Room.SPECIALS.contains( n.type ) 
								&& n.type != Type.PIT) {
							
							neigbours.add( n );
						}
					}
					if (neigbours.size() > 1) {
						r.connect( Random.element( neigbours ) );
					}
				}
			}
		}
		
		int count = 0;
		for (Room r : rooms) {
			if (r.type == Type.NULL) {//把null的房间 随机修改为标准房间或者tunnel
				int connections = r.connected.size();
				if (connections == 0) {
					
				} else if (Random.Int( connections * connections ) == 0) {
					r.type = Type.STANDARD;
					count++;
				} else {
					r.type = Type.TUNNEL; 
				}
			}
		}
		
		while (count < 4) {//标准房间少于4个时 随机把tunnel改成标准房间
			Room r = randomRoom( Type.TUNNEL, 1 );
			if (r != null) {
				r.type = Type.STANDARD;
				count++;
			}
		}
	}
	
	/**
	 * map水块赋值
	 */
	protected void paintWater() {
		boolean[] lake = water();
		for (int i=0; i < LENGTH; i++) {
			if (map[i] == Terrain.EMPTY && lake[i]) {
				map[i] = Terrain.WATER;
			}
		}
	}
	
	/**
	 * map草块赋值
	 */
	protected void paintGrass() {
		boolean[] grass = grass();
		
		if (feeling == Feeling.GRASS) {//判断层是否是偏向草地
			
			for (Room room : rooms) {
				//把room的四个角变成草地
				if (room.type != Type.NULL && room.type != Type.PASSAGE && room.type != Type.TUNNEL) {
					grass[(room.left + 1) + (room.top + 1) * WIDTH] = true;
					grass[(room.right - 1) + (room.top + 1) * WIDTH] = true;
					grass[(room.left + 1) + (room.bottom - 1) * WIDTH] = true;
					grass[(room.right - 1) + (room.bottom - 1) * WIDTH] = true;
				}
			}
		}

		for (int i=WIDTH+1; i < LENGTH-WIDTH-1; i++) {
			if (map[i] == Terrain.EMPTY && grass[i]) {
				int count = 1;
				for (int n : NEIGHBOURS8) {//统计周围8个格子,即九宫格周围是否为草
					if (grass[i + n]) {
						count++;
					}
				}
				//随机出普通的草或者高草
				map[i] = (Random.Float() < count / 12f) ? Terrain.HIGH_GRASS : Terrain.GRASS;
			}
		}
	}
	
	/**生成水块的随机方法*/
	protected abstract boolean[] water();
	/**生成草块的随机方法*/
	protected abstract boolean[] grass();
	
	/**
	 * map陷阱赋值,都是隐藏陷阱
	 */
	protected void placeTraps() {
		
		int nTraps = nTraps();
		float[] trapChances = trapChances();
		
		for (int i=0; i < nTraps; i++) {
			
			int trapPos = Random.Int( LENGTH );
			
			if (map[trapPos] == Terrain.EMPTY) {
				switch (Random.chances( trapChances )) {
				case 0:
					map[trapPos] = Terrain.SECRET_TOXIC_TRAP;
					break;
				case 1:
					map[trapPos] = Terrain.SECRET_FIRE_TRAP;
					break;
				case 2:
					map[trapPos] = Terrain.SECRET_PARALYTIC_TRAP;
					break;
				case 3:
					map[trapPos] = Terrain.SECRET_POISON_TRAP;
					break;
				case 4:
					map[trapPos] = Terrain.SECRET_ALARM_TRAP;
					break;
				case 5:
					map[trapPos] = Terrain.SECRET_LIGHTNING_TRAP;
					break;
				case 6:
					map[trapPos] = Terrain.SECRET_GRIPPING_TRAP;
					break;
				case 7:
					map[trapPos] = Terrain.SECRET_SUMMONING_TRAP;
					break;
				}
			}
		}
	}
	
	/**
	 * 返回陷阱数量
	 * @return
	 */
	protected int nTraps() {
		return Dungeon.depth <= 1 ? 0 : Random.Int( 1, rooms.size() + Dungeon.depth );
	}
	
	/**
	 * 8种陷阱的概率
	 * @return
	 */
	protected float[] trapChances() {
		float[] chances = { 1, 1, 1, 1, 1, 1, 1, 1 };
		return chances;
	}
	
	protected int minRoomSize = 7;
	protected int maxRoomSize = 9;
	
	/**
	 * 划分房间
	 * @param rect
	 */
	protected void split( Rect rect ) {
		
		int w = rect.width();
		int h = rect.height();
		
		if (w > maxRoomSize && h < minRoomSize) {// 横分割,高度已满足最小空间
			int vw = Random.Int( rect.left + 3, rect.right - 3 );
			split( new Rect( rect.left, rect.top, vw, rect.bottom ) );// 左边空间
			split( new Rect( vw, rect.top, rect.right, rect.bottom ) );// 右边空间
			
		} else 
		if (h > maxRoomSize && w < minRoomSize) {// 竖分割 ,宽已满足最小空间
			int vh = Random.Int( rect.top + 3, rect.bottom - 3 );
			split( new Rect( rect.left, rect.top, rect.right, vh ) );// 上边空间
			split( new Rect( rect.left, vh, rect.right, rect.bottom ) );// 下边空间
			
		} else if ((Math.random() <= (minRoomSize * minRoomSize / rect.square()) // 最小面积/空间面积=大于0或者大于等于1, 大于等于1时,肯定是足够小空间可以作为房间
				&& w <= maxRoomSize // 宽少于等于最大值
				&& h <= maxRoomSize) // 高少于等于最大值
				|| w < minRoomSize // 宽少于最小值
				|| h < minRoomSize) {// 高少于最小值

			rooms.add( (Room)new Room().set( rect ) );// 分割出一个房间
			
		} else {
			float r = (float)(w - 2) / (w + h - 4);
			if (Random.Float() < r) {// 按概率(r的规律：当w比较小,h比较大的时候,r值会比较小,这时候会大概率的出现竖分割,相反情况也一样), 横或竖分割房间,
				Log.i("横分割","w:"+ w + " h:" + h + " r:" + r);
				int vw = Random.Int( rect.left + 3, rect.right - 3 );// 横分段
				split( new Rect( rect.left, rect.top, vw, rect.bottom ) );// 左边空间
				split( new Rect( vw, rect.top, rect.right, rect.bottom ) );// 右边空间
			} else {
				Log.i("竖分割","w:"+ w + " h:" + h + " r:" + r);
				int vh = Random.Int( rect.top + 3, rect.bottom - 3 );// 竖分段
				split( new Rect( rect.left, rect.top, rect.right, vh ) );// 上边空间
				split( new Rect( rect.left, vh, rect.right, rect.bottom ) );// 下边空间
			}
			
		}
	}
	
	/**
	 * map赋值,用不同类型方块填充房间
	 */
	protected void paint() {
		for (Room r : rooms) {
			if (r.type != Type.NULL) {
				placeDoors( r );
				r.type.paint( this, r );//根据房间类型填充块并未不同类型房间的door设置类型
			} else {//房间类型没有定的 根据层的地形来随机填充
				if (feeling == Feeling.CHASM && Random.Int( 2 ) == 0) {
					Painter.fill( this, r, Terrain.WALL );//填充墙
				}
			}
		}
		
		for (Room r : rooms) {
			paintDoors( r );
		}
	}
	
	/**
	 * 房间和连通房间之间新建一个door,未具体反映到map上的
	 * @param r
	 */
	private void placeDoors( Room r ) {
		for (Room n : r.connected.keySet()) {
			Room.Door door = r.connected.get(n);
			if (door == null) {
				Rect i = r.intersect( n );//找出相交部位
				if (i.width() == 0) {//竖相交
					door = new Room.Door(i.left, 
						Random.Int( i.top + 1, i.bottom ) );
				} else {//横相交
					door = new Room.Door( 
						Random.Int( i.left + 1, i.right ), i.top);
				}

				r.connected.put( n, door );
				n.connected.put( r, door );
			}
		}
	}
	
	/**
	 * map赋值,把门赋值到地图上
	 * @param r
	 */
	protected void paintDoors( Room r ) {
		for (Room n : r.connected.keySet()) {//会重复赋值？可以优化？

			if (joinRooms( r, n )) {
				continue;
			}
			
			Room.Door d = r.connected.get( n );
			int door = d.x + d.y * WIDTH;//计算门在map数组中的位置
			
			switch (d.type) {
			case EMPTY:
				map[door] = Terrain.EMPTY;
				break;
			case TUNNEL:
				map[door] =  tunnelTile();
				break;
			case REGULAR:
				if (Dungeon.depth <= 1) {
					map[door] = Terrain.DOOR;
				} else {
					boolean secret = (Dungeon.depth < 6 ? Random.Int( 12 - Dungeon.depth ) : Random.Int( 6 )) == 0;
					map[door] = secret ? Terrain.SECRET_DOOR : Terrain.DOOR;
					if (secret) {
						secretDoors++;
					}
				}
				break;
			case UNLOCKED:
				map[door] = Terrain.DOOR;
				break;
			case HIDDEN:
				map[door] = Terrain.SECRET_DOOR;
				secretDoors++;
				break;
			case BARRICADE:
				map[door] = Random.Int( 3 ) == 0 ? Terrain.BOOKSHELF : Terrain.BARRICADE;
				break;
			case LOCKED:
				map[door] = Terrain.LOCKED_DOOR;
				break;
			}
		}
	}
	
	/**
	 * 判断两个房间是否能放置door,挖空能放置门的墙体,map赋值
	 * @param r
	 * @param n
	 * @return false 不能放置 true 可以放置
	 */
	protected boolean joinRooms( Room r, Room n ) {
		// 非标准房间,至少一个房间为标准房间才能放置门
		if (r.type != Room.Type.STANDARD || n.type != Room.Type.STANDARD) {
			return false;
		}
		
		Rect w = r.intersect( n );//计算相交矩形
		if (w.left == w.right) {//竖相交,直接比较不用width()少一次计算
			
			if (w.bottom - w.top < 3) {//相交不超过3个方块时
				return false;
			}
			
			if (w.height() == Math.max( r.height(), n.height() )) {//竖方向完全贴在一起的
				return false;
			}
			
			if (r.width() + n.width() > maxRoomSize) {//超大房间不能放置门
				return false;
			}
			
			w.top += 1;//下移一格
			w.bottom -= 0;//意义何在?
			
			w.right++;//意义何在?
			
			Painter.fill( this, w.left, w.top, 1, w.height(), Terrain.EMPTY );//挖空相交方块 最顶 最底的不挖空
			
		} else {
			
			if (w.right - w.left < 3) {
				return false;
			}
			
			if (w.width() == Math.max( r.width(), n.width() )) {
				return false;
			}
			
			if (r.height() + n.height() > maxRoomSize) {
				return false;
			}
			
			w.left += 1;
			w.right -= 0;//意义何在?
			
			w.bottom++;//意义何在?
			
			Painter.fill( this, w.left, w.top, w.width(), 1, Terrain.EMPTY );//挖空相交方块 最左 最右的不挖空
		}
		
		return true;
	}
	
	@Override
	public int nMobs() {
		return 2 + Dungeon.depth % 5 + Random.Int( 3 );
	}
	
	@Override
	protected void createMobs() {
		int nMobs = nMobs();
		for (int i=0; i < nMobs; i++) {
			Mob mob = Bestiary.mob( Dungeon.depth );
			do {
				mob.pos = randomRespawnCell();
			} while (mob.pos == -1);
			mobs.add( mob );
			Actor.occupyCell( mob );
		}
	}
	
	@Override
	public int randomRespawnCell() {
		int count = 0;
		int cell = -1;
		
		while (true) {
			
			if (++count > 10) {
				return -1;
			}
			
			Room room = randomRoom( Room.Type.STANDARD, 10 );
			if (room == null) {
				continue;
			}
			
			cell = room.random();
			if (!Dungeon.visible[cell] && Actor.findChar( cell ) == null && Level.passable[cell]) {
				return cell;
			}
			
		}
	}
	
	@Override
	public int randomDestination() {
		
		int cell = -1;
		
		while (true) {
			
			Room room = Random.element( rooms );
			if (room == null) {
				continue;
			}
			
			cell = room.random();
			if (Level.passable[cell]) {
				return cell;
			}
			
		}
	}
	
	/**
	 * 创建Item,至少三个Item
	 */
	@Override
	protected void createItems() {
		
		int nItems = 3;//至少3个item
		while (Random.Float() < 0.4f) {
			nItems++;
		}
		
		for (int i=0; i < nItems; i++) {
			Heap.Type type = null;
			switch (Random.Int( 20 )) {
			case 0:
				type = Heap.Type.SKELETON;
				break;
			case 1:
			case 2:
			case 3:
			case 4:
				type = Heap.Type.CHEST;
				break;
			case 5:
				type = Dungeon.depth > 1 ? Heap.Type.MIMIC : Heap.Type.CHEST;
				break;
			default:
				type = Heap.Type.HEAP;
			}
			Item item = Generator.random();
			drop( item, randomDropCell() ).type = type;
			Log.i("房间生成的箱子", item.info());
		}

		for (Item item : itemsToSpawn) {
			int cell = randomDropCell();
			if (item instanceof ScrollOfUpgrade) {
				while (map[cell] == Terrain.FIRE_TRAP || map[cell] == Terrain.SECRET_FIRE_TRAP) {
					cell = randomDropCell();
				}
			}
			drop( item, cell ).type = Heap.Type.HEAP;
		}
		
		Item item = Bones.get();
		if (item != null) {
			drop( item, randomDropCell() ).type = Heap.Type.SKELETON;
		}
	}
	
	protected Room randomRoom( Room.Type type, int tries ) {
		for (int i=0; i < tries; i++) {
			Room room = Random.element( rooms );
			if (room.type == type) {
				return room;
			}
		}
		return null;
	}
	
	public Room room( int pos ) {
		for (Room room : rooms) {
			if (room.type != Type.NULL && room.inside( pos )) {
				return room;
			}
		}
		
		return null;
	}
	
	protected int randomDropCell() {
		while (true) {
			Room room = randomRoom( Room.Type.STANDARD, 1 );
			if (room != null) {
				int pos = room.random();
				if (passable[pos]) {
					return pos;
				}
			}
		}
	}
	
	@Override
	public int pitCell() {
		for (Room room : rooms) {
			if (room.type == Type.PIT) {
				return room.random();
			}
		}
		
		return super.pitCell();
	}
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( "rooms", rooms );
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		
		rooms = new HashSet<Room>( (Collection<? extends Room>) bundle.getCollection( "rooms" ) );
		for (Room r : rooms) {
			if (r.type == Type.WEAK_FLOOR) {
				weakFloorCreated = true;
				break;
			}
		}
	}
	
}
