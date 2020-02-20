package com.degrendel.reverserogue.common

interface Level
{
  companion object
  {
    const val SIZE = 32
  }

  fun get(x: Int, y: Int): TileType

  fun forEach(lambda: (x: Int, y: Int, type: TileType) -> Unit)
}

enum class TileType
{
  BLOCKED, PASSAGEWAY, ROOM
}
