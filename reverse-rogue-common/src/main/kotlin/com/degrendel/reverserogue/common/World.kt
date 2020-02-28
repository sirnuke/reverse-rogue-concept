package com.degrendel.reverserogue.common

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import kotlinx.coroutines.Job

interface World
{
  val ecs: Engine

  val conjurer: Entity
  val rogue: Entity

  val agent: SoarAgent
  val frontend: Frontend

  fun computeCost(action: Action): Long

  fun updateWorld()

  fun runGame(): Job

  fun isValidAction(action: Action): Boolean

  fun getLevel(floor: Int): Level
}


enum class CreatureType
{
  ROGUE,
  CONJURER,
  ;
}

enum class Allegiance
{
  ROGUE,
  CONJURER,
  NEUTRAL,
  ;
}