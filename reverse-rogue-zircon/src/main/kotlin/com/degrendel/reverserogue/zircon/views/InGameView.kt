package com.degrendel.reverserogue.zircon.views

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.Family
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.common.components.*
import com.degrendel.reverserogue.zircon.Application
import com.degrendel.reverserogue.zircon.MouseButtons
import com.degrendel.reverserogue.zircon.components.DrawnAtComponent
import com.degrendel.reverserogue.zircon.components.getDrawnAt
import com.degrendel.reverserogue.zircon.events.PlayerActionInput
import com.degrendel.reverserogue.zircon.toPosition
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.graphics.Layer
import org.hexworks.zircon.api.uievent.*
import org.hexworks.zircon.api.view.base.BaseView
import org.hexworks.zircon.internal.Zircon
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class InGameView(private val application: Application) : BaseView(application.tileGrid)
{
  companion object
  {
    private val L by logger()

    const val MAP_OFFSET_X = 0
    const val MAP_OFFSET_Y = 0
  }

  private var floor = 0

  // TODO: Doesn't actually work the way I want, since it can increment/decrement way outside the bounds
  // Catch in refreshMap, which also resets the value, but still feels sketchy.
  // And are we even going to allow viewing random levels?
  private val scrollToFloor = AtomicInteger(floor)

  // All spawned creatures
  private val spawnedCreatures = Family.all(PositionComponent::class.java, CreatureTypeComponent::class.java).get()

  // All spawned creatures that have been drawn at least once
  private val drawnCreatures = Family.all(PositionComponent::class.java, CreatureTypeComponent::class.java, DrawnAtComponent::class.java).get()

  // Do we need a separate layer for stuff like visibility and known indicators?
  private val mapLayer = Layer.newBuilder()
      .withOffset(MAP_OFFSET_X, MAP_OFFSET_Y)
      .withSize(Level.WIDTH, Level.HEIGHT)
      .build()
  private val creatureLayer = Layer.newBuilder()
      .withOffset(MAP_OFFSET_X, MAP_OFFSET_Y)
      .withSize(Level.WIDTH, Level.HEIGHT)
      .build()

  private val creatureMap = mutableMapOf<Position, Tile>()

  init
  {
    screen.theme = ColorThemes.adriftInDreams()

    application.world.ecs.addEntityListener(spawnedCreatures, object : EntityListener
    {
      override fun entityAdded(entity: Entity)
      {
        L.info("Adding entity {}", entity)
        val position = entity.getPosition()
        val tile = when (entity.getCreature().type)
        {
          CreatureType.ROGUE -> Tiles.rogueTile
          CreatureType.CONJURER -> Tiles.conjurerTile
        }
        creatureMap[position.toPosition()] = tile
        entity.add(DrawnAtComponent(position))
      }

      override fun entityRemoved(entity: Entity)
      {
        val position = entity.getDrawnAt()
        L.info("Remove entity {} @ {}", entity, position)
        entity.remove(DrawnAtComponent::class.java)
      }
    })

    screen.handleMouseEvents(MouseEventType.MOUSE_MOVED) { event: MouseEvent, _ ->
      L.debug("Mouse move at {},{}", event.position.x, event.position.y)
      Pass
    }

    screen.handleMouseEvents(MouseEventType.MOUSE_CLICKED) { event: MouseEvent, _ ->
      when (event.button)
      {
        MouseButtons.LEFT.id ->
        {
          L.info("Button1, up!")
          scrollToFloor.incrementAndGet()
          Processed
        }
        MouseButtons.RIGHT.id ->
        {
          L.info("Button2, down!")
          scrollToFloor.decrementAndGet()
          Processed
        }
        else -> Pass
      }
    }

    screen.handleKeyboardEvents(KeyboardEventType.KEY_PRESSED) { event: KeyboardEvent, _: UIEventPhase ->
      // TODO: Would be nice to have this be configurable
      when (event.code)
      {
        KeyCode.LEFT, KeyCode.KEY_A, KeyCode.NUMPAD_4 ->
        {
          Zircon.eventBus.publish(PlayerActionInput(Move(application.world.conjurer, 100L, EightWay.WEST), this))
          Processed
        }
        KeyCode.RIGHT, KeyCode.KEY_D, KeyCode.NUMPAD_6 ->
        {
          Zircon.eventBus.publish(PlayerActionInput(Move(application.world.conjurer, 100L, EightWay.EAST), this))
          Processed
        }
        KeyCode.UP, KeyCode.KEY_W, KeyCode.NUMPAD_8 ->
        {
          Zircon.eventBus.publish(PlayerActionInput(Move(application.world.conjurer, 100L, EightWay.NORTH), this))
          Processed
        }
        KeyCode.DOWN, KeyCode.KEY_X, KeyCode.NUMPAD_2 ->
        {
          Zircon.eventBus.publish(PlayerActionInput(Move(application.world.conjurer, 100L, EightWay.SOUTH), this))
          Processed
        }
        KeyCode.HOME, KeyCode.KEY_Q, KeyCode.NUMPAD_7 ->
        {
          Zircon.eventBus.publish(PlayerActionInput(Move(application.world.conjurer, 100L, EightWay.NORTH_WEST), this))
          Processed
        }
        KeyCode.PAGE_UP, KeyCode.KEY_E, KeyCode.NUMPAD_9 ->
        {
          Zircon.eventBus.publish(PlayerActionInput(Move(application.world.conjurer, 100L, EightWay.NORTH_EAST), this))
          Processed
        }
        KeyCode.END, KeyCode.KEY_Z, KeyCode.NUMPAD_1 ->
        {
          Zircon.eventBus.publish(PlayerActionInput(Move(application.world.conjurer, 100L, EightWay.SOUTH_WEST), this))
          Processed
        }
        KeyCode.PAGE_DOWN, KeyCode.KEY_C, KeyCode.NUMPAD_3 ->
        {
          Zircon.eventBus.publish(PlayerActionInput(Move(application.world.conjurer, 100L, EightWay.SOUTH_EAST), this))
          Processed
        }
        KeyCode.KEY_S, KeyCode.NUMPAD_5 ->
        {
          Zircon.eventBus.publish(PlayerActionInput(Sleep(application.world.conjurer), this))
          Processed
        }
        else -> Pass
      }
    }
  }

  fun runGameLoop()
  {
    L.info("Starting game loop")
    mapLayer.clear()
    creatureLayer.clear()
    // TODO: Far too many creature type lookups
    application.world.ecs.getEntitiesFor(spawnedCreatures).forEach { creature ->
      val tile = when (creature.getCreature().type)
      {
        CreatureType.ROGUE -> Tiles.rogueTile
        CreatureType.CONJURER -> Tiles.conjurerTile
      }
      creatureMap[creature.getPosition().toPosition()] = tile
      creature.add(DrawnAtComponent(creature.getPosition()))
    }

    val job = application.world.runGame()
  }

  fun refreshMap()
  {
    L.debug("Refreshing map!")
    val targetFloor = scrollToFloor.get().coerceIn(0, Level.FLOORS - 1)
    scrollToFloor.set(targetFloor)
    if (targetFloor != floor)
    {
      L.info("Scrolling to {}", targetFloor)
      floor = targetFloor
      mapLayer.clear()
      creatureMap.clear()
      creatureLayer.clear()
    }
    // TODO: To add blocking animations, make a list of suspendCoroutines
    // val blockingAnimations = mutableListOf()
    // TODO: Might be nice to have this be a map as well
    val level = application.world.getLevel(floor)
    (0 until Level.WIDTH).forEach { x ->
      (0 until Level.HEIGHT).forEach { y ->
        // TODO: Update visible, known variants
        val square = level.getSquare(x, y)
        val tile = when (square.getSquare().type)
        {
          SquareType.BLOCKED -> Tiles.blockedTile
          SquareType.CORRIDOR -> Tiles.corridorTile
          SquareType.FLOOR -> Tiles.floorTile
          SquareType.WALL -> Tiles.wallTiles.getValue(square.getWallOrientation())
          SquareType.DOOR -> Tiles.doorTile
        }
        mapLayer.draw(tile, Position.create(x, y))
      }
    }
    application.world.ecs.getEntitiesFor(drawnCreatures).forEach { creature ->
      // TODO: This will need to be completely rethought if a creature can have multiple tiles
      // TODO: This is so nasty
      val position = creature.getPosition()
      if (position.floor != floor) return@forEach
      val drawn = creature.getDrawnAt()
      // TODO: This short circuit is supposed to skip updating the tile if it hasn't moved, however it breaks changing
      //  levels.  Fix is probably confirming the tile is in creatureMap, but not a significant performance boost anyway
      //if (drawn == position) return@forEach
      val tile = when (creature.getCreature().type)
      {
        CreatureType.ROGUE -> Tiles.rogueTile
        CreatureType.CONJURER -> Tiles.conjurerTile
      }
      // TODO: If E1 is drawn on A moves to B, and E2 is drawn on B moves to C, need to make sure they don't step on
      //    each other for B -- is this logic sufficient?
      val drawnZircon = drawn.toPosition()
      // TODO: Adding an Tile.empty() is necessary to clear the old creature, but there is a bit of a performance hit.
      //      Every time something moves, there's an extra tile added.  Maybe not bad since the world isn't that big.
      //      I think the best approach is either 1. clear out any Tile.empty()s after they've been drawn, or 2. preload
      //      the map with empties so the performance hit is consistent
      if (creatureMap[drawnZircon] == tile)
        creatureMap[drawnZircon] = Tile.empty()
      creatureMap[position.toPosition()] = tile
      creature.add(DrawnAtComponent(position))
      // if (drawn != position && drawn.floor == position.floor)
      // Add movement animation?
    }

    creatureLayer.draw(creatureMap)

    // Wait on list of firing animations
  }

  override fun onDock()
  {
    L.info("Docking InGameView")

    screen.addLayer(mapLayer)
    screen.addLayer(creatureLayer)

    // TODO: Log view, and side bar controls
  }

  fun enablePlayerInputGUI()
  {
    // TODO: Enable player input GUI (or shade?)
  }

  fun disablePlayerInputGUI()
  {
    // TODO: Disable player input GUI (or shade?)
  }

}

object Tiles
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

