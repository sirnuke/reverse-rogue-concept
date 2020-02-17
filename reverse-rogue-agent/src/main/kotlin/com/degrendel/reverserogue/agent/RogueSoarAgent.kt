package com.degrendel.reverserogue.agent

import com.degrendel.reverserogue.common.logger
import org.jsoar.kernel.SoarException
import org.jsoar.runtime.ThreadedAgent
import org.jsoar.util.commands.SoarCommands
import kotlin.system.exitProcess

class RogueSoarAgent
{
  companion object
  {
    val L by logger()
  }

  private val agent = ThreadedAgent.create("Wanbot")!!

  init
  {
    try
    {
      SoarCommands.source(agent.interpreter, javaClass.getResource("/com/degrendel/reverserogue/soar/load.soar"))
    }
    catch (e: SoarException)
    {
      L.error("Unable to source the agent", e)
      exitProcess(-1)
    }
  }
}