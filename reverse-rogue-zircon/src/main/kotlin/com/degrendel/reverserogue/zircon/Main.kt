package com.degrendel.reverserogue.zircon

import com.degrendel.reverserogue.common.logger
import org.hexworks.zircon.api.application.DebugConfig
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.util.concurrent.Callable
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.system.exitProcess

@Command(name = "ReverseRogue", mixinStandardHelpOptions = true)
class Main : Callable<Int>
{
  companion object
  {
    private val L by logger()
  }

  private val lock = ReentrantLock()
  private val condition = lock.newCondition()

  @Option(names = ["--soar-debugger"])
  private var soarDebugger = false

  @Option(names = ["--zircon-debug-mode"])
  private var zirconDebugMode = false

  @Option(names = ["--draw-zircon-grid"])
  private var drawZirconGrid = false

  override fun call(): Int
  {
    L.info("Launching; soar debugger? {}; zircon debug mode? {}; draw grid?", soarDebugger, zirconDebugMode,
        drawZirconGrid)
    Application(lock, condition, soarDebugger = soarDebugger, zirconDebugMode = zirconDebugMode,
        drawGrid = drawZirconGrid)
    lock.withLock { condition.await() }
    L.info("Quiting...")
    return 0
  }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine.call(Main(), *args))
