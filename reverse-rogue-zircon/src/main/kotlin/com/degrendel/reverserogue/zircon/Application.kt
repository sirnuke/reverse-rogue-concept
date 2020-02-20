package com.degrendel.reverserogue.zircon

import com.degrendel.reverserogue.agent.RogueSoarAgent
import com.degrendel.reverserogue.common.SoarAgent
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.SwingApplications
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.extensions.box
import org.hexworks.zircon.api.screen.Screen
import org.hexworks.zircon.api.uievent.MouseEventType
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Application(private val lock: ReentrantLock, private val condition: Condition)
{
  val agent: SoarAgent = RogueSoarAgent()

  fun launch()
  {
    val tileGrid = SwingApplications.startTileGrid()
    val screen = Screen.create(tileGrid)

    screen.onShutdown { lock.withLock { condition.signal() } }

    val panel = Components.panel()
        .withSize(Size.create(4, 5))
        .withDecorations(box())
        .build()

    screen.addComponent(panel)

    panel.processMouseEvents(MouseEventType.MOUSE_MOVED) { event, _ -> println(event) }

    screen.theme = ColorThemes.adriftInDreams()

    screen.display()
  }
}