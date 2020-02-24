package com.degrendel.reverserogue.world

import com.badlogic.ashley.core.Entity
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.common.components.*
import com.github.czyzby.noise4j.map.Grid
import com.github.czyzby.noise4j.map.generator.room.AbstractRoomGenerator.Room
import com.github.czyzby.noise4j.map.generator.room.RoomType
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator

// TODO: almost certainly going to have to store this longer term, if only for the agent
data class RoomDetails(val x: Int, val y: Int, val width: Int, val height: Int)

class LevelState(private val world: RogueWorld) : Level
{
  companion object
  {
    private val L by logger()
  }

  // TODO: Doesn't need to be mutable
  private val map = mutableListOf<MutableList<Entity>>()

  init
  {
    for (x in 0 until Level.WIDTH)
    {
      val row = mutableListOf<Entity>()
      for (y in 0 until Level.HEIGHT)
        row += Entity().add(PositionComponent(Position(x, y)))
      map += row
    }
    val rooms = mutableListOf<Room>()
    val dungeonGenerator = DungeonGenerator()
    dungeonGenerator.addRoomType(object : RoomType
    {
      override fun carve(room: Room, grid: Grid, value: Float)
      {
        L.info("Carving room {}x{}", room.width, room.height)
        rooms += room
        room.fill(grid, value)
      }

      override fun isValid(room: Room) = true
    })
    dungeonGenerator.minRoomSize = 5
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
      L.info("({},{}) size ({}x{})", room.x, room.y, room.width, room.height)
      for (x in (room.x - 1)..(room.x + room.width))
      {
        wallify(x, room.y - 1)
        wallify(x, room.y + room.height)
      }
      for (y in (room.y - 1)..(room.y + room.height))
      {
        wallify(room.x - 1, y)
        wallify(room.x + room.width, y)
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
  }

  fun removeFromECS()
  {
    map.forEach { row -> row.forEach { world.ecs.removeEntity(it) } }
  }

  override fun getSquare(position: Position) = map[position.x][position.y]

  override fun inBounds(position: Position) = (position.x >= 0 && position.y >= 0 && position.x < Level.WIDTH && position.y < Level.HEIGHT)
}

