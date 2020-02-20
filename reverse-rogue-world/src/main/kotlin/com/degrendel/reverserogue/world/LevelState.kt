package com.degrendel.reverserogue.world

import com.degrendel.reverserogue.common.Level
import com.degrendel.reverserogue.common.TileType
import com.degrendel.reverserogue.common.logger
import com.github.czyzby.noise4j.map.Grid
import com.github.czyzby.noise4j.map.generator.room.dungeon.DungeonGenerator

class LevelState : Level
{
  companion object
  {
    private val L by logger()
  }

  private val data = mutableListOf<MutableList<TileType>>()

  init
  {
    for (x in 0 until Level.WIDTH)
    {
      val row = mutableListOf<TileType>()
      for (y in 0 until Level.HEIGHT)
        row += TileType.BLOCKED
      data += row
    }
    val dungeonGenerator = DungeonGenerator()
    val grid = Grid(32)

    dungeonGenerator.generate(grid)

    grid.forEach { _, x, y, value ->
      data[x][y] = when (value)
      {
        1.0f -> TileType.BLOCKED
        0.5f -> TileType.ROOM
        0.0f -> TileType.PASSAGEWAY
        else -> throw IllegalStateException("Unexpected value $value")
      }
      false
    }
  }

  override fun get(x: Int, y: Int): TileType
  {
    return data[x][y]
  }

  override fun forEach(lambda: (x: Int, y: Int, type: TileType) -> Unit)
  {
    data.forEachIndexed { x, row ->
      row.forEachIndexed { y, tileType ->
        lambda(x, y, tileType)
      }
    }
  }
}

