package com.degrendel.reverserogue.common

import com.badlogic.ashley.core.Entity

enum class EightWay(val x: Int, val y: Int, val diagonal: Boolean)
{
  NORTH(0, -1, false),
  NORTH_EAST(1, -1, true),
  EAST(1, 0, false),
  SOUTH_EAST(1, 1, true),
  SOUTH(0, 1, false),
  SOUTH_WEST(-1, 1, true),
  WEST(-1, 0, false),
  NORTH_WEST(-1, -1, true),
  ;
}

// TODO: Cost should be computed somewhere in World, not passed in
sealed class Action
{
  abstract val entity: Entity
  abstract val cost: Long
}

/** This entity wants to move. */
data class Move(override val entity: Entity, override val cost: Long, val direction: EightWay) : Action()

/** This entity wants to skip their turn. */
data class Sleep(override val entity: Entity) : Action()
{
  // TODO: Get the default value from somewhere else (GameConfig?)
  override val cost: Long = 100L
}