package com.degrendel.reverserogue.common

interface Level
{
  companion object
  {
    const val HEIGHT = 32
    const val WIDTH = 64
  }

  fun get(x: Int, y: Int): TileType

  fun forEach(lambda: (x: Int, y: Int, type: TileType) -> Unit)
}

enum class TileType
{
  BLOCKED, PASSAGEWAY, ROOM
}
