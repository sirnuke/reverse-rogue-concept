package com.degrendel.reverserogue.zircon

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
  private val lock = ReentrantLock()
  private val condition = lock.newCondition()

  @Option(names = ["--soar-debugger"])
  private var soarDebugger = false

  override fun call(): Int
  {
    val application = Application(lock, condition)

    if (soarDebugger)
      application.agent.openDebugger()

    application.launch()
    lock.withLock { condition.await() }
    return 0
  }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine(Main()).execute(*args))
