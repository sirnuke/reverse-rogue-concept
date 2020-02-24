package com.degrendel.reverserogue.zircon

import com.degrendel.reverserogue.agent.RogueSoarAgent
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.world.RogueWorld
import com.degrendel.reverserogue.zircon.views.InGameView
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.SwingApplications
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.application.DebugConfig
import org.hexworks.zircon.api.grid.TileGrid
import org.hexworks.zircon.api.uievent.*
import org.hexworks.zircon.internal.Zircon
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.system.exitProcess

class Application(lock: ReentrantLock, condition: Condition, soarDebugger: Boolean = false, zirconDebugMode: Boolean = false, drawGrid: Boolean = false)
{
  companion object
  {
    private val L by logger()
    // FYI: (80 , 45) * 16 == (1280, 720), which is what FTL uses.  Probably reasonable
    // If an another display (HTML5 compatible?) is added, should be moved to common
    const val SCREEN_WIDTH = 80
    const val SCREEN_HEIGHT = 45
  }

  val agent: SoarAgent = RogueSoarAgent()
  val world: World = RogueWorld()

  val tileGrid: TileGrid

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
    tileGrid.onShutdown { lock.withLock { condition.signal() } }

    val view = InGameView(this)

    if (zirconDebugMode)
    {
      view.screen.handleKeyboardEvents(KeyboardEventType.KEY_PRESSED) { event: KeyboardEvent, _: UIEventPhase ->
        if (event.code == KeyCode.ESCAPE)
          exitProcess(0)
        UIEventResponse.pass()
      }
    }

    world.generateLevel()
    world.spawn()

    tileGrid.dock(view)
  }
}