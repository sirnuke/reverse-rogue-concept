package com.degrendel.reverserogue.world

import com.badlogic.ashley.core.Entity
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.common.components.*
import com.github.czyzby.noise4j.map.Grid
import com.github.czyzby.noise4j.map.generator.room.AbstractRoomGenerator.Room
import com.github.czyzby.noise4j.map.generator.room.RoomType
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator

class LevelState(private val world: RogueWorld) : Level
{
  companion object
  {
    private val L by logger()
  }

  // TODO: Doesn't need to be mutable
  private val map = mutableListOf<MutableList<Entity>>()
  private val rooms = mutableListOf<Entity>()

  init
  {
    for (x in 0 until Level.WIDTH)
    {
      val row = mutableListOf<Entity>()
      for (y in 0 until Level.HEIGHT)
        row += Entity().add(PositionComponent(Position(x, y)))
      map += row
    }
    val dungeonGenerator = DungeonGenerator()
    dungeonGenerator.addRoomType(object : RoomType
    {
      override fun carve(room: Room, grid: Grid, value: Float)
      {
        L.debug("Carving room ({},{})->({}x{})", room.x, room.y, room.width, room.height)
        rooms += Entity().add(PositionComponent(Position(room.x, room.y))).add(RoomComponent(rooms.size, room.width, room.height))
        room.fill(grid, value)
      }

      override fun isValid(room: Room) = true
    })
    dungeonGenerator.minRoomSize = Level.MINIMUM_ROOM_SIZE
    dungeonGenerator.maxRoomsAmount = Level.MAXIMUM_NUMBER_OF_ROOMS
    val grid = Grid(Level.WIDTH, Level.HEIGHT)

    dungeonGenerator.generate(grid)

    grid.forEach { _, x, y, value ->
      val type = when (value)
      {
        1.0f -> SquareType.BLOCKED
        0.5f -> SquareType.FLOOR
        0.0f -> SquareType.CORRIDOR
        else -> throw IllegalStateException("Unexpected value $value")
      }
      map[x][y].add(SquareTypeComponent(type))
      false
    }

    val walls = mutableListOf<Entity>()

    val wallify = { x: Int, y: Int ->
      if (map[x][y].getSquareType().blocked)
      {
        walls += map[x][y]
        map[x][y].add(SquareTypeComponent(SquareType.WALL))
      }
      else map[x][y].add(SquareTypeComponent(SquareType.DOOR))
    }

    rooms.forEach { room ->
      val position = room.getPosition()
      val width: Int
      val height: Int
      room.getRoomData().let { width = it.width; height = it.height }
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
        val check = Position(position.x + it.x, position.y + it.y)
        if (inBounds(check))
        {
          val neighbor = map[check.x][check.y].getSquareType()
          if (neighbor == SquareType.WALL || neighbor == SquareType.DOOR)
            neighbors.add(it)
        }
      }
      wall.add(WallOrientationComponent(WallOrientation.lookup.getValue(neighbors)))
    }

    map.forEach { row -> row.forEach { world.ecs.addEntity(it) } }
    rooms.forEach { world.ecs.addEntity(it) }
  }

  fun removeFromECS()
  {
    rooms.forEach { world.ecs.removeEntity(it) }
    map.forEach { row -> row.forEach { world.ecs.removeEntity(it) } }
  }

  override fun getSquare(position: Position) = map[position.x][position.y]

  override fun inBounds(position: Position) = (position.x >= 0 && position.y >= 0 && position.x < Level.WIDTH && position.y < Level.HEIGHT)
}

