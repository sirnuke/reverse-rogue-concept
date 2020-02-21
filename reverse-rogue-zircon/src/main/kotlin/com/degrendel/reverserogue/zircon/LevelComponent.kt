package com.degrendel.reverserogue.zircon

import com.degrendel.reverserogue.common.Level
import com.degrendel.reverserogue.common.SquareType
import com.degrendel.reverserogue.common.WallDirection
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.graphics.Layer
import java.util.*

class LevelComponent(private val app: Application, private val layer: Layer, private val level: Level)
{
  companion object
  {
    private val floorTile = Tile.defaultTile().withCharacter('.')
    private val blockedTile = Tile.defaultTile().withCharacter(' ')
    private val corridorTile = Tile.defaultTile().withCharacter('#')
    private val doorTile = Tile.defaultTile().withCharacter('+')
    private val rogueTile = Tile.defaultTile().withCharacter(0x263A.toChar())

    private val wallTiles = EnumMap<WallDirection, Tile>(WallDirection::class.java)

    init
    {
      wallTiles[WallDirection.NORTH_SOUTH] = Tile.defaultTile().withCharacter(0x2551.toChar())
      wallTiles[WallDirection.EAST_WEST] = Tile.defaultTile().withCharacter(0x2550.toChar())
      wallTiles[WallDirection.NORTH_EAST] = Tile.defaultTile().withCharacter(0x255A.toChar())
      wallTiles[WallDirection.EAST_SOUTH] = Tile.defaultTile().withCharacter(0x2554.toChar())
      wallTiles[WallDirection.SOUTH_WEST] = Tile.defaultTile().withCharacter(0x2557.toChar())
      wallTiles[WallDirection.WEST_NORTH] = Tile.defaultTile().withCharacter(0x255D.toChar())
      wallTiles[WallDirection.NORTH_EAST_SOUTH] = Tile.defaultTile().withCharacter(0x2560.toChar())
      wallTiles[WallDirection.EAST_SOUTH_WEST] = Tile.defaultTile().withCharacter(0x2566.toChar())
      wallTiles[WallDirection.SOUTH_WEST_NORTH] = Tile.defaultTile().withCharacter(0x2563.toChar())
      wallTiles[WallDirection.WEST_NORTH_EAST] = Tile.defaultTile().withCharacter(0x2569.toChar())
      wallTiles[WallDirection.ALL] = Tile.defaultTile().withCharacter(0x256C.toChar())
    }
  }

  init
  {
    refresh()
  }

  fun refresh()
  {
    level.forEachSquare { square ->
      val tile = when (square.type)
      {
        SquareType.BLOCKED -> blockedTile
        SquareType.CORRIDOR -> corridorTile
        SquareType.FLOOR -> floorTile
        SquareType.WALL -> wallTiles.getValue(square.wallDirection)
        SquareType.DOOR -> doorTile
      }
      layer.draw(tile, Position.create(square.position.x, square.position.y))
    }

    level.forEachEntity { entity ->
      TODO("Stub!")
    }
  }

}