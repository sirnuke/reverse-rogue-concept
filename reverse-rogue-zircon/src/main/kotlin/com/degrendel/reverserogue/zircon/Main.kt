package com.degrendel.reverserogue.zircon

import picocli.CommandLine
import picocli.CommandLine.Command
import java.util.concurrent.Callable
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.system.exitProcess

@Command(name = "ReverseRogue", mixinStandardHelpOptions = true)
class Main : Callable<Int>
{
  private val lock = ReentrantLock()
  private val condition = lock.newCondition()

  override fun call(): Int
  {
    Application(lock, condition).launch()
    lock.withLock { condition.await() }
    return 0
  }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine(Main()).execute(*args))
