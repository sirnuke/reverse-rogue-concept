package com.degrendel.reverserogue.common

import java.util.*

class NavigationMap(val level: Level, val sources: List<Position>)
{
  private val _data: MutableList<MutableList<Int>> = (0..Level.WIDTH).map { (0..Level.HEIGHT).map { Int.MAX_VALUE }.toMutableList() }.toMutableList()
  val data: List<List<Int>> get() = _data

  init
  {
    sources.forEach { _data[it.x][it.y] = 0 }
    val toCheck = LinkedList<Position>().also { it.addAll(sources) }
    while (toCheck.isNotEmpty())
    {
      // We functional now (mostly)
      // TODO: This assumes there is no diagonal cost.  If that changes, this will probably need to be a float
      val cost: Int
      getNeighbors(toCheck.removeFirst().also { cost = _data[it.x][it.y] + 1 }).forEach {
        _data[it.x][it.y] = cost
        toCheck.addLast(it)
      }
    }
  }

  private fun getNeighbors(position: Position) = position
      .eightWayNeighbors()
      .filter { _data[it.x][it.y] == Int.MAX_VALUE }
      .filter { level.isNavigable(it) }
}