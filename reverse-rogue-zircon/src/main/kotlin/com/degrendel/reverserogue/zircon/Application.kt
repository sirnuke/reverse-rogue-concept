package com.degrendel.reverserogue.zircon

import com.degrendel.reverserogue.agent.RogueSoarAgent
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.world.RogueWorld
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.SwingApplications
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.application.DebugConfig
import org.hexworks.zircon.api.builder.graphics.LayerBuilder
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.grid.TileGrid
import org.hexworks.zircon.api.screen.Screen
import org.hexworks.zircon.api.uievent.MouseEvent
import org.hexworks.zircon.api.uievent.MouseEventType
import org.hexworks.zircon.api.uievent.UIEventPhase
import org.hexworks.zircon.api.uievent.UIEventResponse
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Application(lock: ReentrantLock, condition: Condition, soarDebugger: Boolean = false, zirconDebugMode: Boolean = false, drawGrid: Boolean = false)
{
  companion object
  {
    private val L by logger()
    // FYI: (80 , 45) * 16 == (1280, 720), which is what FTL uses.  Probably reasonable
    // If an another display (HTML5 compatible?) is added, should be moved to common
    const val SCREEN_WIDTH = 80
    const val SCREEN_HEIGHT = 45

    const val MAP_OFFSET_X = 0
    const val MAP_OFFSET_Y = 0
  }

  val agent: SoarAgent = RogueSoarAgent()
  val world: World = RogueWorld()


  private val tileGrid: TileGrid
  private val screen: Screen

  private val mapComponent: MapComponent

  init
  {
    assert(SCREEN_WIDTH >= Level.WIDTH)
    assert(SCREEN_HEIGHT >= Level.HEIGHT)

    if (soarDebugger)
      agent.openDebugger()

    val debugConfig = if (drawGrid)
      DebugConfig(displayGrid = true, displayCoordinates = true, displayFps = true)
    else
      DebugConfig(displayGrid = false, displayCoordinates = false, displayFps = true)


    tileGrid = SwingApplications.startTileGrid(
        AppConfig.newBuilder()
            .withSize(SCREEN_WIDTH, SCREEN_HEIGHT)
            .withDefaultTileset(CP437TilesetResources.rexPaint16x16())
            .withDebugConfig(debugConfig)
            .withDebugMode(zirconDebugMode)
            .build())
    screen = Screen.create(tileGrid)
    screen.onShutdown { lock.withLock { condition.signal() } }
    screen.theme = ColorThemes.adriftInDreams()

    screen.display()

    val mapLayer = LayerBuilder.newBuilder()
        .withOffset(MAP_OFFSET_X, MAP_OFFSET_Y)
        .withSize(Level.WIDTH, Level.HEIGHT)
        .build()

    tileGrid.addLayer(mapLayer)

    mapComponent = MapComponent(this, mapLayer)

    screen.handleMouseEvents(MouseEventType.MOUSE_CLICKED) { mouseEvent: MouseEvent, uiEventPhase: UIEventPhase ->
      mapComponent.newLevel()
      UIEventResponse.processed()
    }
  }

  private fun drawLevel()
  {
  }
}