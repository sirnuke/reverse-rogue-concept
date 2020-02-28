package com.degrendel.reverserogue.zircon

import com.degrendel.reverserogue.agent.RogueSoarAgent
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.world.RogueWorld
import com.degrendel.reverserogue.zircon.events.PlayerActionInput
import com.degrendel.reverserogue.zircon.views.InGameView
import org.hexworks.cobalt.events.api.DisposeSubscription
import org.hexworks.cobalt.events.api.KeepSubscription
import org.hexworks.cobalt.events.api.subscribeTo
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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.system.exitProcess

class Application(lock: ReentrantLock, condition: Condition, soarDebugger: Boolean = false, zirconDebugMode: Boolean = false, drawGrid: Boolean = false) : Frontend
{
  companion object
  {
    private val L by logger()
    // FYI: (80 , 45) * 16 == (1280, 720), which is what FTL uses.  Probably reasonable
    // If an another display (HTML5 compatible?) is added, should be moved to common
    const val SCREEN_WIDTH = 80
    const val SCREEN_HEIGHT = 45
  }

  private val inGameView: InGameView

  val agent: SoarAgent = RogueSoarAgent()
  val world: World = RogueWorld(this)

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

    inGameView = InGameView(this)

    if (zirconDebugMode)
    {
      inGameView.screen.handleKeyboardEvents(KeyboardEventType.KEY_PRESSED) { event: KeyboardEvent, _: UIEventPhase ->
        if (event.code == KeyCode.ESCAPE)
          exitProcess(0)
        UIEventResponse.pass()
      }
    }

    tileGrid.dock(inGameView)

    inGameView.runGameLoop()
  }

  override suspend fun getPlayerInput(): Action = suspendCoroutine { continuation ->
    // TODO: this feels a bit race conditiony - could disable input fire before enable input?
    // Alternatively, it's almost certainly fine to always enable the GUI, but ignore events if not the players turn
    inGameView.enablePlayerInputGUI()
    Zircon.eventBus.subscribeTo<PlayerActionInput> { it ->
      if (world.isValidAction(it.action))
      {
        inGameView.disablePlayerInputGUI()
        continuation.resume(it.action)
        DisposeSubscription
      }
      else
        KeepSubscription
    }
  }

  override suspend fun refreshMap()
  {
    inGameView.refreshMap()
  }
}