package com.degrendel.reverserogue.world

import com.badlogic.ashley.core.Entity
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.common.components.getPosition

class BehaviorAI(private val world: World)
{
  // TODO: Lots of room for caching cost maps
  fun compute(behaviors: List<Behavior>, creature: Entity): Action
  {
    val targets = mutableListOf<Position>()
    val position = creature.getPosition()
    val level = world.getLevel(position.floor)
    behaviors.forEach {
      when (it)
      {
        is MoveTowardsRogue -> targets.add(world.rogue.getPosition())
        is MoveTowardsConjurer -> targets.add(world.conjurer.getPosition())
      }
    }

    val costMap = NavigationMap(level, targets)
    val moveTo = costMap.getMove(position)
    return if (moveTo == null)
      Sleep(creature)
    else
      Move(creature, moveTo)
  }
}