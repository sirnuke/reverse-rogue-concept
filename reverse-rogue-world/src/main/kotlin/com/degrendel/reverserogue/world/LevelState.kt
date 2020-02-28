package com.degrendel.reverserogue.world

import com.badlogic.ashley.core.Entity
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.common.components.*
import com.github.czyzby.noise4j.map.Grid
import com.github.czyzby.noise4j.map.generator.room.AbstractRoomGenerator.Room
import com.github.czyzby.noise4j.map.generator.room.RoomType
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator

class LevelState(private val world: RogueWorld, override val floor: Int) : Level
{
  companion object
  {
    private val L by logger()
  }

  // TODO: Doesn't need to be mutable
  private val map = mutableListOf<MutableList<SquareInfo>>()
  private val rooms = mutableListOf<Entity>()

  init
  {
    for (x in 0 until Level.WIDTH)
    {
      val row = mutableListOf<SquareInfo>()
      for (y in 0 until Level.HEIGHT)
        row += SquareInfo(Entity().add(PositionComponent(Position(x, y, floor))), creature = null)
      map += row
    }
    val dungeonGenerator = DungeonGenerator()
    dungeonGenerator.addRoomType(object : RoomType
    {
      override fun carve(room: Room, grid: Grid, value: Float)
      {
        L.debug("Carving room ({},{})->({}x{})", room.x, room.y, room.width, room.height)
        rooms += Entity().add(PositionComponent(Position(room.x, room.y, floor))).add(RoomComponent(rooms.size, room.width, room.height))
        room.fill(grid, value)
      }

      override fun isValid(room: Room) = true
    })
    dungeonGenerator.minRoomSize = Level.MINIMUM_ROOM_SIZE
    dungeonGenerator.maxRoomsAmount = Level.MAXIMUM_NUMBER_OF_ROOMS
    val grid = Grid(Level.WIDTH, Level.HEIGHT)

    dungeonGenerator.generate(grid)

    grid.forEach { _, x, y, value ->
      val position = Position(x, y, floor)
      val type = when (value)
      {
        1.0f -> SquareType.BLOCKED
        0.5f -> SquareType.FLOOR
        0.0f -> SquareType.CORRIDOR
        else -> throw IllegalStateException("Unexpected value $value")
      }
      val roomId = if (type == SquareType.FLOOR)
        rooms.firstOrNull { it.isWithinRoom(position) }?.getRoomData()?.id
      else null
      val visibleRooms = if (roomId == null) setOf() else setOf(roomId)
      map[x][y].square.add(SquareTypeComponent(type, roomId, visibleRooms))
      false
    }

    val walls = mutableListOf<Entity>()

    val wallify = { x: Int, y: Int ->
      val position = Position(x, y, floor)
      if (map[x][y].square.getSquare().type.blocked)
      {
        walls += map[x][y].square
        map[x][y].square.add(SquareTypeComponent(SquareType.WALL, null, setOf()))
      }
      else
      {
        val visibleRooms = mutableSetOf<Int>()
        Cardinal.values().forEach {
          val neighbor = position.move(it)
          if (inBounds(neighbor))
            map[neighbor.x][neighbor.y].square.getSquare().roomId?.let { id -> visibleRooms.add(id) }
        }
        map[x][y].square.add(SquareTypeComponent(SquareType.DOOR, null, visibleRooms))
      }
    }

    rooms.forEach { room ->
      val position = room.getPosition()
      val width: Int
      val height: Int
      room.getRoomData().let { width = it.width; height = it.height }
      // TODO: Gross
      for (x in (position.x - 1)..(position.x + width))
      {
        wallify(x, position.y - 1)
        wallify(x, position.y + height)
      }
      for (y in (position.y - 1)..(position.y + height))
      {
        wallify(position.x - 1, y)
        wallify(position.x + width, y)
      }
    }

    walls.forEach { wall ->
      val neighbors = mutableSetOf<Cardinal>()
      val position = wall.getPosition()
      Cardinal.values().forEach {
        val check = position.move(it)
        if (inBounds(check))
        {
          val neighbor = map[check.x][check.y].square.getSquare().type
          if (neighbor == SquareType.WALL || neighbor == SquareType.DOOR)
            neighbors.add(it)
        }
      }
      wall.add(WallOrientationComponent(WallOrientation.lookup.getValue(neighbors)))
    }

    map.forEach { row -> row.forEach { world.ecs.addEntity(it.square) } }
    rooms.forEach { world.ecs.addEntity(it) }
  }

  /**
   * Removes all entities tracked by this level from the ECS.
   *
   * Anything that should be retained in the system, such as the rogue, should probably be despawned before calling
   * this.
   */
  /*
  fun removeFromECS()
  {
    rooms.forEach { world.ecs.removeEntity(it) }
    map.forEach { row ->
      row.forEach {
        world.ecs.removeEntity(it.square)
        it.creature?.let { entity -> world.ecs.removeEntity(entity) }
      }
    }
  }
   */

  override fun getSquare(x: Int, y: Int) = map[x][y].square

  /**
   * Adds a creature to this level.
   *
   * Also adds to ECS engine and gives the entity a position component.
   */
  fun spawnCreature(entity: Entity, position: Position)
  {
    assert(inBounds(position))
    assert(map[position.x][position.y].creature == null)
    assert(position.floor == floor)
    map[position.x][position.y].creature = entity
    entity.add(PositionComponent(position))
    world.ecs.addEntity(entity)
  }

  /**
   * Removes a creature from this level.
   *
   * Also removes it from the ECS engine.  Does not remove the position component.
   */
  fun despawnCreature(entity: Entity)
  {
    val position = entity.getPosition()
    assert(inBounds(position))
    assert(map[position.x][position.y].creature == entity)
    assert(position.floor == floor)
    map[position.x][position.y].creature = null
    world.ecs.removeEntity(entity)
  }

  /**
   * Moves a creature to a different tile.
   *
   * Asserts that the creature is in the current position, and that the new position is free.  Removes the old position
   * component, and adds a new one, to trigger upstream listeners.
   */
  fun moveCreature(entity: Entity, direction: EightWay)
  {
    val from = entity.getPosition()
    assert(from.floor == floor)
    assert(canMoveTo(from, direction))
    assert(map[from.x][from.y].creature == entity)
    map[from.x][from.y].creature = null
    val to = from.move(direction)
    map[to.x][to.y].creature = entity
    // NOTE: Explicitly remove position to trigger refreshes upstream in Families
    // Actually don't want this lol
    // entity.remove(PositionComponent::class.java)
    entity.add(PositionComponent(to))
  }

  /**
   * Returns a list of randomly selected rooms, with no duplicates.
   *
   * Will probably blow up in your face if greater than the actual number of rooms, FYI.
   */
  fun getRandomRooms(count: Int): List<Entity> = rooms.shuffled().dropLast(rooms.size - count)

  /**
   * Returns a random location inside the given room.
   */
  fun getRandomPointInRoom(room: Entity): Position
  {
    // TODO: Needs to track treasure versus traps versus stairs versus creatures versus etc
    val data = room.getRoomData()
    return room.getPosition().random(data.width, data.height)
  }

  override fun getCreature(x: Int, y: Int) = map[x][y].creature

  override fun canMoveTo(from: Position, direction: EightWay): Boolean
  {
    val to = from.move(direction)
    if (!inBounds(to)) return false
    val square = map[to.x][to.y]
    return (!square.square.getSquare().type.blocked && square.creature == null)
  }

  override fun inBounds(position: Position) = (position.x >= 0 && position.y >= 0 && position.x < Level.WIDTH && position.y < Level.HEIGHT)
}

data class SquareInfo(val square: Entity, var creature: Entity?)

