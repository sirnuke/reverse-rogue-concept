package com.degrendel.reverserogue.common

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import org.hexworks.cobalt.events.api.EventBus

interface World
{
  val ecs: Engine
  val currentLevel: Level?

  val conjurer: Entity
  val rogue: Entity

  val eventBus: EventBus

  fun generateLevel(): Level

  fun update()

  fun spawn()
  fun move(entity: Entity, direction: EightWay)

  fun updateCreature(creature: Entity)
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