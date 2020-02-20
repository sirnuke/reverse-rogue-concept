package com.degrendel.reverserogue.common

interface Level
{
  companion object
  {
    const val HEIGHT = 32
    const val WIDTH = 64
  }

  fun get(x: Int, y: Int): Square

  fun inBounds(x: Int, y: Int): Boolean

  fun forEach(lambda: (square: Square) -> Unit)
}

data class Square(val x: Int, val y: Int, val type: SquareType, val wallDirections: Set<Cardinal> = setOf())
{
  val blocked: Boolean = (type == SquareType.BLOCKED || type == SquareType.WALL)
  val wallDirection: WallDirection = wallDirectionConversion.getValue(wallDirections)

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

  fun addWallDirection(dir: Cardinal) = Square(x, y, type, wallDirections.plus(dir))
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