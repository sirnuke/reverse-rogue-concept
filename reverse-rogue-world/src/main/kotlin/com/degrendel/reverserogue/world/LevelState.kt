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

  private val map = mutableListOf<MutableList<SquareImplementation>>()

  init
  {
    for (x in 0 until Level.WIDTH)
    {
      val row = mutableListOf<SquareImplementation>()
      for (y in 0 until Level.HEIGHT)
        row += SquareImplementation(Position(x, y), SquareType.BLOCKED)
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
      map[x][y] = SquareImplementation(Position(x, y), type)
      false
    }

    val walls = mutableListOf<Pair<Int, Int>>()

    val wallify = { x: Int, y: Int ->
      map[x][y] = if (map[x][y].blocked)
      {
        walls += Pair(x, y)
        SquareImplementation(Position(x, y), SquareType.WALL)
      }
      else SquareImplementation(Position(x, y), SquareType.DOOR)
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
        val check = Position(x + it.x, y + it.y)
        if (inBounds(check))
        {
          val neighbor = map[check.x][check.y]
          if (neighbor.type == SquareType.WALL || neighbor.type == SquareType.DOOR)
            map[x][y] = map[x][y].addWallDirection(it)
        }
      }
    }
  }

  override fun inBounds(position: Position) = (position.x >= 0 && position.y >= 0 && position.x < Level.WIDTH && position.y < Level.HEIGHT)

  override fun getSquare(position: Position): Square
  {
    return map[position.x][position.y]
  }

  override fun forEachSquare(lambda: (square: Square) -> Unit)
  {
    map.forEach { row ->
      row.forEach { square -> lambda(square) }
    }
  }
}

data class SquareImplementation(override val position: Position, override val type: SquareType, val wallDirections: Set<Cardinal> = setOf()) : Square
{
  override val blocked: Boolean = (type == SquareType.BLOCKED || type == SquareType.WALL)
  override val wallDirection: WallDirection = wallDirectionConversion.getValue(wallDirections)

  companion object
  {
    // Ick.  I'm guessing there's some math function to do this, but whatever
    // Could do this a bit more efficiently with bitmasks, but this lookup should still be fairly quick, and only
    // done once per level generation.  Compared to the memory hog that is Soar, probably not that expensive to just
    // keep them in memory for when the rogue starts going back up the stairs.
    val wallDirectionConversion = mapOf(
        setOf<Cardinal>() to WallDirection.NONE,
        setOf(Cardinal.NORTH) to WallDirection.NORTH_SOUTH,
        setOf(Cardinal.SOUTH) to WallDirection.NORTH_SOUTH,
        setOf(Cardinal.NORTH, Cardinal.SOUTH) to WallDirection.NORTH_SOUTH,
        setOf(Cardinal.EAST) to WallDirection.EAST_WEST,
        setOf(Cardinal.WEST) to WallDirection.EAST_WEST,
        setOf(Cardinal.EAST, Cardinal.WEST) to WallDirection.EAST_WEST,
        setOf(Cardinal.NORTH, Cardinal.EAST) to WallDirection.NORTH_EAST,
        setOf(Cardinal.EAST, Cardinal.SOUTH) to WallDirection.EAST_SOUTH,
        setOf(Cardinal.SOUTH, Cardinal.WEST) to WallDirection.SOUTH_WEST,
        setOf(Cardinal.WEST, Cardinal.NORTH) to WallDirection.WEST_NORTH,
        setOf(Cardinal.NORTH, Cardinal.EAST, Cardinal.SOUTH) to WallDirection.NORTH_EAST_SOUTH,
        setOf(Cardinal.EAST, Cardinal.SOUTH, Cardinal.WEST) to WallDirection.EAST_SOUTH_WEST,
        setOf(Cardinal.SOUTH, Cardinal.WEST, Cardinal.NORTH) to WallDirection.SOUTH_WEST_NORTH,
        setOf(Cardinal.WEST, Cardinal.NORTH, Cardinal.EAST) to WallDirection.WEST_NORTH_EAST,
        setOf(Cardinal.NORTH, Cardinal.EAST, Cardinal.SOUTH, Cardinal.WEST) to WallDirection.ALL
    )
  }

  fun addWallDirection(dir: Cardinal) = SquareImplementation(position, type, wallDirections.plus(dir))
}

