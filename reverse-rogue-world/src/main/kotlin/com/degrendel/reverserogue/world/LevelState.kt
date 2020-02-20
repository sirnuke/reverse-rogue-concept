package com.degrendel.reverserogue.world

import com.degrendel.reverserogue.common.*
import com.github.czyzby.noise4j.map.Grid
import com.github.czyzby.noise4j.map.generator.room.AbstractRoomGenerator.Room
import com.github.czyzby.noise4j.map.generator.room.RoomType
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator

// TODO: almost certainly going to have to store this longer term, if only for the agent
data class RoomDetails(val x: Int, val y: Int, val width: Int, val height: Int)

class LevelState : Level
{
  companion object
  {
    private val L by logger()
  }

  private val map = mutableListOf<MutableList<Square>>()

  init
  {
    for (x in 0 until Level.WIDTH)
    {
      val row = mutableListOf<Square>()
      for (y in 0 until Level.HEIGHT)
        row += Square(x, y, SquareType.BLOCKED)
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
      map[x][y] = Square(x, y, type)
      false
    }

    val walls = mutableListOf<Pair<Int, Int>>()

    val wallify = { x: Int, y: Int ->
      map[x][y] = if (map[x][y].blocked)
      {
        walls += Pair(x, y)
        Square(x, y, SquareType.WALL)
      }
      else Square(x, y, SquareType.DOOR)
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

    walls.forEach { (x, y) ->
      Cardinal.values().forEach {
        val checkX = x + it.x
        val checkY = y + it.y
        if (inBounds(checkX, checkY))
        {
          val neighbor = map[checkX][checkY]
          if (neighbor.type == SquareType.WALL || neighbor.type == SquareType.DOOR)
            map[x][y] = map[x][y].addWallDirection(it)
        }
      }
    }
  }

  override fun inBounds(x: Int, y: Int) = (x >= 0 && y >= 0 && x < Level.WIDTH && y < Level.HEIGHT)

  override fun get(x: Int, y: Int): Square
  {
    return map[x][y]
  }

  override fun forEach(lambda: (square: Square) -> Unit)
  {
    map.forEach { row ->
      row.forEach { square -> lambda(square) }
    }
  }
}

