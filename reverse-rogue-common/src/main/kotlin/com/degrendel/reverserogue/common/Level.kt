package com.degrendel.reverserogue.common

interface Level
{
  companion object
  {
    const val HEIGHT = 32
    const val WIDTH = 64
  }

  fun getSquare(position: Position): Square

  fun inBounds(position: Position): Boolean

  fun forEachSquare(lambda: (square: Square) -> Unit)
}

data class Position(val x: Int, val y: Int)
{
  fun add(position: Position) = Position(x + position.x, y + position.y)
}

interface Square
{
  val position: Position
  val type: SquareType
  val wallDirection: WallDirection
  val blocked: Boolean
}

enum class SquareType
{
  BLOCKED, CORRIDOR, WALL, FLOOR, DOOR
}

enum class Cardinal(val x: Int, val y: Int)
{
  NORTH(0, -1), EAST(1, 0), SOUTH(0, 1), WEST(-1, 0)
}

enum class WallDirection
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
}