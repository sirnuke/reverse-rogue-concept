package com.degrendel.reverserogue.zircon

import picocli.CommandLine
import picocli.CommandLine.Command
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@Command(name = "ReverseRogue", mixinStandardHelpOptions = true)
class Main() : Callable<Int>
{
  override fun call(): Int
  {
    return 0;
  }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine(Main()).execute(*args))
