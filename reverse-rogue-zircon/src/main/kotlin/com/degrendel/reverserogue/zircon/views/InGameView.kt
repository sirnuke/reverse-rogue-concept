package com.degrendel.reverserogue.zircon.views

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.Family
import com.degrendel.reverserogue.common.Level
import com.degrendel.reverserogue.common.SquareType
import com.degrendel.reverserogue.common.WallOrientation
import com.degrendel.reverserogue.common.components.*
import com.degrendel.reverserogue.common.logger
import com.degrendel.reverserogue.zircon.Application
import kotlinx.collections.immutable.persistentHashMapOf
import org.hexworks.zircon.api.GameComponents
import org.hexworks.zircon.api.builder.game.GameAreaBuilder
import org.hexworks.zircon.api.data.Position3D
import org.hexworks.zircon.api.data.Size3D
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.data.base.BaseBlock
import org.hexworks.zircon.api.grid.TileGrid
import org.hexworks.zircon.api.uievent.*
import org.hexworks.zircon.api.view.base.BaseView
import java.util.*

class InGameView(private val application: Application, tileGrid: TileGrid) : BaseView(tileGrid)
{
  companion object
  {
    private val L by logger()
  }

  // TODO: Probably want three cases:
  //  1. position + square type and not known or visible
  //  2. position + square type and known but not visible
  //  3. position + square type and visible
  // All squares
  private val levelSquares = Family.all(PositionComponent::class.java, SquareTypeComponent::class.java).get()

  init
  {
    application.world.ecs.addEntityListener(levelSquares, object : EntityListener
    {
      override fun entityAdded(entity: Entity)
      {
        L.info("New entity! {}", entity)
        val tile = when (entity.getSquareType())
        {
          SquareType.BLOCKED -> TileBlock.blockedTile
          SquareType.CORRIDOR -> TileBlock.corridorTile
          SquareType.FLOOR -> TileBlock.floorTile
          SquareType.WALL -> TileBlock.wallTiles.getValue(entity.getWallOrientation())
          SquareType.DOOR -> TileBlock.doorTile
        }
        val position = entity.getPosition()
        val tileBlock = TileBlock()
        tileBlock.top = tile
        gameArea.setBlockAt(Position3D.create(position.x, position.y, 0), tileBlock)
      }

      override fun entityRemoved(entity: Entity)
      {
      }
    })

    screen.handleMouseEvents(MouseEventType.MOUSE_CLICKED) { _: MouseEvent, _: UIEventPhase ->
      L.info("Mouse clicked!")
      application.world.generateLevel()
      //levelView = LevelView(this, levelLayer, world.generateLevel())
      UIEventResponse.processed()
    }
  }

  private val gameArea = GameAreaBuilder.newBuilder<Tile, TileBlock>()
      .withActualSize(Size3D.create(Level.WIDTH, Level.HEIGHT, 1))
      .withVisibleSize(Size3D.create(Level.WIDTH, Level.HEIGHT, 1))
      .build()

  override fun onDock()
  {
    L.info("InGameView docked!")
    screen.handleKeyboardEvents(KeyboardEventType.KEY_PRESSED) { event: KeyboardEvent, _: UIEventPhase ->
      // TODO: Handle text input
      Pass
    }

    val levelComponent = GameComponents.newGameComponentBuilder<Tile, TileBlock>().withGameArea(gameArea).build()

    screen.addComponent(levelComponent)
  }
}

class TileBlock : BaseBlock<Tile>(emptyTile = Tile.empty(), tiles = persistentHashMapOf())
{
  companion object
  {
    val floorTile = Tile.defaultTile().withCharacter('.')
    val blockedTile = Tile.defaultTile().withCharacter(' ')
    val corridorTile = Tile.defaultTile().withCharacter('#')
    val doorTile = Tile.defaultTile().withCharacter('+')
    val rogueTile = Tile.defaultTile().withCharacter(0x263A.toChar())

    val wallTiles = EnumMap<WallOrientation, Tile>(WallOrientation::class.java)

    init
    {
      wallTiles[WallOrientation.NORTH_SOUTH] = Tile.defaultTile().withCharacter(0x2551.toChar())
      wallTiles[WallOrientation.EAST_WEST] = Tile.defaultTile().withCharacter(0x2550.toChar())
      wallTiles[WallOrientation.NORTH_EAST] = Tile.defaultTile().withCharacter(0x255A.toChar())
      wallTiles[WallOrientation.EAST_SOUTH] = Tile.defaultTile().withCharacter(0x2554.toChar())
      wallTiles[WallOrientation.SOUTH_WEST] = Tile.defaultTile().withCharacter(0x2557.toChar())
      wallTiles[WallOrientation.WEST_NORTH] = Tile.defaultTile().withCharacter(0x255D.toChar())
      wallTiles[WallOrientation.NORTH_EAST_SOUTH] = Tile.defaultTile().withCharacter(0x2560.toChar())
      wallTiles[WallOrientation.EAST_SOUTH_WEST] = Tile.defaultTile().withCharacter(0x2566.toChar())
      wallTiles[WallOrientation.SOUTH_WEST_NORTH] = Tile.defaultTile().withCharacter(0x2563.toChar())
      wallTiles[WallOrientation.WEST_NORTH_EAST] = Tile.defaultTile().withCharacter(0x2569.toChar())
      wallTiles[WallOrientation.ALL] = Tile.defaultTile().withCharacter(0x256C.toChar())
    }
  }
}