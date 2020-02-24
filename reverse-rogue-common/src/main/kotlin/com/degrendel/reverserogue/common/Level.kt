package com.degrendel.reverserogue.common

import com.badlogic.ashley.core.Entity

interface Level
{
  companion object
  {
    const val HEIGHT = 32
    const val WIDTH = 64

    // NOTE: Must be odd (required by Noise4j)
    const val MINIMUM_ROOM_SIZE = 5
    const val MAXIMUM_NUMBER_OF_ROOMS = 10
  }

  fun getSquare(position: Position): Entity

  fun inBounds(position: Position): Boolean
}

data class Position(val x: Int, val y: Int)
{
  fun add(position: Position) = Position(x + position.x, y + position.y)

  fun move(direction: EightWay) = Position(x + direction.x, y + direction.y)
}

enum class SquareType(val blocked: Boolean)
{
  BLOCKED(true),
  CORRIDOR(false),
  WALL(true),
  FLOOR(false),
  DOOR(false)
}

enum class Cardinal(val x: Int, val y: Int)
{
  NORTH(0, -1), EAST(1, 0), SOUTH(0, 1), WEST(-1, 0)
}


enum class WallOrientation
{
  NONE,               //     ain't no wall
  NORTH_SOUTH,        //  |  vertical
  EAST_WEST,          // --  horizontal
  NORTH_EAST,         // ^>  corner upper right
  EAST_SOUTH,         // v>  corner lower right
  SOUTH_WEST,         // <v  corner lower left
  WEST_NORTH,         // <^  corner upper left
  NORTH_EAST_SOUTH,   // |>  vertical plus right
  EAST_SOUTH_WEST,    // --v horizontal plus down
  SOUTH_WEST_NORTH,   // <|  vertical plus left
  WEST_NORTH_EAST,    // ^-- horizontal plus up
  ALL,                // + all four (rare)
  ;

  companion object
  {
    // Ick.  I'm guessing there's some math function to do this, but whatever
    // Could do this a bit more efficiently with bitmasks, but this lookup should still be fairly quick, and only
    // done once per level generation.  Compared to the memory hog that is Soar, probably not that expensive to just
    // keep them in memory for when the rogue starts going back up the stairs.
    val lookup = mapOf(
        setOf<Cardinal>() to WallOrientation.NONE,
        setOf(Cardinal.NORTH) to WallOrientation.NORTH_SOUTH,
        setOf(Cardinal.SOUTH) to WallOrientation.NORTH_SOUTH,
        setOf(Cardinal.NORTH, Cardinal.SOUTH) to WallOrientation.NORTH_SOUTH,
        setOf(Cardinal.EAST) to WallOrientation.EAST_WEST,
        setOf(Cardinal.WEST) to WallOrientation.EAST_WEST,
        setOf(Cardinal.EAST, Cardinal.WEST) to WallOrientation.EAST_WEST,
        setOf(Cardinal.NORTH, Cardinal.EAST) to WallOrientation.NORTH_EAST,
        setOf(Cardinal.EAST, Cardinal.SOUTH) to WallOrientation.EAST_SOUTH,
        setOf(Cardinal.SOUTH, Cardinal.WEST) to WallOrientation.SOUTH_WEST,
        setOf(Cardinal.WEST, Cardinal.NORTH) to WallOrientation.WEST_NORTH,
        setOf(Cardinal.NORTH, Cardinal.EAST, Cardinal.SOUTH) to WallOrientation.NORTH_EAST_SOUTH,
        setOf(Cardinal.EAST, Cardinal.SOUTH, Cardinal.WEST) to WallOrientation.EAST_SOUTH_WEST,
        setOf(Cardinal.SOUTH, Cardinal.WEST, Cardinal.NORTH) to WallOrientation.SOUTH_WEST_NORTH,
        setOf(Cardinal.WEST, Cardinal.NORTH, Cardinal.EAST) to WallOrientation.WEST_NORTH_EAST,
        setOf(Cardinal.NORTH, Cardinal.EAST, Cardinal.SOUTH, Cardinal.WEST) to WallOrientation.ALL
    )
  }
}