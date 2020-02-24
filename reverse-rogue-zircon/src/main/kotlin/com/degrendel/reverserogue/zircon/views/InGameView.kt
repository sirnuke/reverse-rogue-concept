package com.degrendel.reverserogue.zircon.views

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.Family
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.common.components.*
import com.degrendel.reverserogue.zircon.Application
import com.degrendel.reverserogue.zircon.components.DrawnAtComponent
import com.degrendel.reverserogue.zircon.components.getDrawnAt
import kotlinx.collections.immutable.persistentHashMapOf
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.GameComponents
import org.hexworks.zircon.api.builder.game.GameAreaBuilder
import org.hexworks.zircon.api.data.Position3D
import org.hexworks.zircon.api.data.Size3D
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.data.base.BaseBlock
import org.hexworks.zircon.api.uievent.*
import org.hexworks.zircon.api.view.base.BaseView
import java.util.*

class InGameView(private val application: Application) : BaseView(application.tileGrid)
{
  companion object
  {
    private val L by logger()

    const val MAP_OFFSET_X = 0
    const val MAP_OFFSET_Y = 0
  }

  // All squares
  private val levelSquares = Family.all(PositionComponent::class.java, SquareTypeComponent::class.java).get()

  // TODO: Probably want two cases:
  //  1. position + known but not visible -> set to known variant
  //  2. position + rogue visible -> set to visible variant

  // All spawned creatures
  private val spawnedCreatures = Family.all(PositionComponent::class.java, CreatureTypeComponent::class.java).get()

  init
  {
    screen.theme = ColorThemes.adriftInDreams()
    // TODO: tbh, this is all kinda hacky, and it's probably faster/easier to just 'redraw' the screen after each action
    application.world.ecs.addEntityListener(levelSquares, object : EntityListener
    {
      override fun entityAdded(entity: Entity)
      {
        L.debug("New square! {}", entity)
        val tile = when (entity.getSquare().type)
        {
          SquareType.BLOCKED -> TileBlock.blockedTile
          SquareType.CORRIDOR -> TileBlock.corridorTile
          SquareType.FLOOR -> TileBlock.floorTile
          SquareType.WALL -> TileBlock.wallTiles.getValue(entity.getWallOrientation())
          SquareType.DOOR -> TileBlock.doorTile
        }
        val position = entity.getPosition()
        val tileBlock = TileBlock()
        tileBlock.update { it.squareTile = tile }
        gameArea.setBlockAt(Position3D.create(position.x, position.y, 0), tileBlock)
        entity.add(DrawnAtComponent(position))
      }

      override fun entityRemoved(entity: Entity)
      {
        val position = entity.getDrawnAt()
        gameArea.fetchBlockAt(Position3D.create(position.x, position.y, 0)).get().update { it.squareTile = null }
        entity.remove(DrawnAtComponent::class.java)
      }
    })

    application.world.ecs.addEntityListener(spawnedCreatures, object : EntityListener
    {
      override fun entityAdded(entity: Entity)
      {
        L.debug("Updating entity {}", entity)
        val position = entity.getPosition()
        val tile = when (entity.getCreature().type)
        {
          CreatureType.ROGUE -> TileBlock.rogueTile
          CreatureType.CONJURER -> TileBlock.conjurerTile
        }
        gameArea.fetchBlockAt(Position3D.create(position.x, position.y, 0)).get().update { it.creatureTile = tile }

        entity.add(DrawnAtComponent(position))
      }

      override fun entityRemoved(entity: Entity)
      {
        val position = entity.getDrawnAt()
        L.debug("Remove entity {} @ {}", entity, position)
        gameArea.fetchBlockAt(Position3D.create(position.x, position.y, 0)).get().update { it.creatureTile = null }
        entity.remove(DrawnAtComponent::class.java)
      }
    })

    screen.handleMouseEvents(MouseEventType.MOUSE_CLICKED) { _: MouseEvent, _: UIEventPhase ->
      L.info("Mouse clicked!")
      application.world.generateLevel()
      application.world.spawn()

      UIEventResponse.processed()
    }

    screen.handleKeyboardEvents(KeyboardEventType.KEY_PRESSED) { event: KeyboardEvent, _: UIEventPhase ->
      // TODO: Would be nice to have this be configurable
      when (event.code)
      {
        KeyCode.LEFT, KeyCode.KEY_A, KeyCode.NUMPAD_4 ->
        {
          application.world.move(application.world.conjurer, EightWay.WEST)
          Processed
        }
        KeyCode.RIGHT, KeyCode.KEY_D, KeyCode.NUMPAD_6 ->
        {
          application.world.move(application.world.conjurer, EightWay.EAST)
          Processed
        }
        KeyCode.UP, KeyCode.KEY_W, KeyCode.NUMPAD_8 ->
        {
          application.world.move(application.world.conjurer, EightWay.NORTH)
          Processed
        }
        KeyCode.DOWN, KeyCode.KEY_X, KeyCode.NUMPAD_2 ->
        {
          application.world.move(application.world.conjurer, EightWay.SOUTH)
          Processed
        }
        KeyCode.HOME, KeyCode.KEY_Q, KeyCode.NUMPAD_7 ->
        {
          application.world.move(application.world.conjurer, EightWay.NORTH_WEST)
          Processed
        }
        KeyCode.PAGE_UP, KeyCode.KEY_E, KeyCode.NUMPAD_9 ->
        {
          application.world.move(application.world.conjurer, EightWay.NORTH_EAST)
          Processed
        }
        KeyCode.END, KeyCode.KEY_Z, KeyCode.NUMPAD_1 ->
        {
          application.world.move(application.world.conjurer, EightWay.SOUTH_WEST)
          Processed
        }
        KeyCode.PAGE_DOWN, KeyCode.KEY_C, KeyCode.NUMPAD_3 ->
        {
          application.world.move(application.world.conjurer, EightWay.SOUTH_EAST)
          Processed
        }
        KeyCode.KEY_S, KeyCode.NUMPAD_5 ->
        {
          L.info("TODO: Sleep!")
          Processed
        }
        else -> Pass
      }
    }
  }

  private val gameArea = GameAreaBuilder.newBuilder<Tile, TileBlock>()
      .withActualSize(Size3D.create(Level.WIDTH, Level.HEIGHT, 1))
      .withVisibleSize(Size3D.create(Level.WIDTH, Level.HEIGHT, 1))
      .build()

  override fun onDock()
  {
    L.info("Docking InGameView")

    val levelComponent = GameComponents.newGameComponentBuilder<Tile, TileBlock>()
        .withGameArea(gameArea)
        .withPosition(MAP_OFFSET_X, MAP_OFFSET_Y)
        .build()

    screen.addComponent(levelComponent)
  }
}

class TileBlock : BaseBlock<Tile>(emptyTile = Tile.empty(), tiles = persistentHashMapOf())
{
  var squareTile: Tile? = null
  var creatureTile: Tile? = null

  fun update(lambda: (TileBlock) -> Unit)
  {
    lambda.invoke(this)
    creatureTile?.let { top = it; return }
    squareTile?.let { top = it; return }
    top = Tile.empty()
  }

  companion object
  {
    val floorTile = Tile.defaultTile().withCharacter('.')
    val blockedTile = Tile.defaultTile().withCharacter(' ')
    val corridorTile = Tile.defaultTile().withCharacter('#')
    val doorTile = Tile.defaultTile().withCharacter('+')
    val rogueTile = Tile.defaultTile().withCharacter(0x263A.toChar())
    val conjurerTile = Tile.defaultTile().withCharacter('@')

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