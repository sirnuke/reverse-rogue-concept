package com.degrendel.reverserogue.zircon

import com.degrendel.reverserogue.agent.RogueSoarAgent
import com.degrendel.reverserogue.common.SoarAgent
import com.degrendel.reverserogue.common.TileType
import com.degrendel.reverserogue.common.World
import com.degrendel.reverserogue.common.logger
import com.degrendel.reverserogue.world.RogueWorld
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.SwingApplications
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.application.DebugConfig
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.extensions.box
import org.hexworks.zircon.api.screen.Screen
import org.hexworks.zircon.api.uievent.MouseEventType
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Application(private val lock: ReentrantLock, private val condition: Condition)
{
  companion object
  {
    private val L by logger()
  }

  val agent: SoarAgent = RogueSoarAgent()
  val world: World = RogueWorld()

  private val tiles = EnumMap<TileType, Tile>(TileType::class.java)

  init
  {
    tiles[TileType.ROOM] = Tile.defaultTile().withCharacter('.')
    tiles[TileType.BLOCKED] = Tile.defaultTile().withCharacter('#')
    tiles[TileType.PASSAGEWAY] = Tile.empty()
  }

  fun launch(zirconDebug: Boolean, drawGrid: Boolean)
  {
    val level = world.generateLevel()

    val debugConfig = if (drawGrid)
      DebugConfig(displayGrid = true, displayCoordinates = true, displayFps = true)
    else
      DebugConfig(displayGrid = false, displayCoordinates = false, displayFps = true)

    val tileGrid = SwingApplications.startTileGrid(
        AppConfig.newBuilder()
            .withSize(64, 64)
            .withDefaultTileset(CP437TilesetResources.rexPaint16x16())
            .withDebugConfig(debugConfig)
            .withDebugMode(zirconDebug)
            .build())
    val screen = Screen.create(tileGrid)
    screen.onShutdown { lock.withLock { condition.signal() } }
    screen.theme = ColorThemes.adriftInDreams()

    screen.display()

    level.forEach { x, y, type ->
      tileGrid.draw(tiles[type]!!, Position.create(x, y))
    }

    /*
    val panel = Components.panel()
        .withSize(Size.create(4, 5))
        .withDecorations(box())
        .build()


    screen.addComponent(panel)

    panel.processMouseEvents(MouseEventType.MOUSE_MOVED) { event, _ -> println(event) }
     */
  }
}