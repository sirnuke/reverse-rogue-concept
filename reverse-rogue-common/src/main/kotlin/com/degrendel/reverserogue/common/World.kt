package com.degrendel.reverserogue.common

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity

interface World
{
  val ecs: Engine
  val currentLevel: Level?

  val conjurer: Entity
  val rogue: Entity

  fun generateLevel(): Level

  fun update()
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