package com.degrendel.reverserogue.world

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.degrendel.reverserogue.common.Action
import com.degrendel.reverserogue.common.Sleep
import com.degrendel.reverserogue.common.World
import com.degrendel.reverserogue.common.components.ControllerType
import com.degrendel.reverserogue.common.components.getCreature
import com.degrendel.reverserogue.common.logger
import java.util.*

class ActionQueueSystem(private val world: World) : EntityListener
{
  companion object
  {
    private val L by logger()
  }

  private val queue = PriorityQueue<Entity>(10, Comparator<Entity> { e1, e2 ->
    val c1 = e1.getCreature()
    val c2 = e2.getCreature()
    assert(c1 != c2)
    assert(c1.id != c2.id)
    if (c1.cooldown == c2.cooldown)
      c1.id - c2.id
    else
      (c1.cooldown - c2.cooldown).toInt()
  })

  override fun entityAdded(entity: Entity)
  {
    L.info("Adding entity {}", entity)
    queue.add(entity)
  }

  override fun entityRemoved(entity: Entity)
  {
    queue.remove(entity)
  }

  suspend fun execute(): Action
  {
    val entity = queue.poll()
    val creature = entity.getCreature()
    L.info("Executing turn for {}, {}", entity, creature)
    val action = when (creature.controller)
    {
      ControllerType.AGENT -> TODO("Agent isn't supported yet")
      // TODO: Write actual simple AI
      ControllerType.SIMPLE_AI -> Sleep(entity)
      ControllerType.PLAYER -> world.frontend.getPlayerInput()
    }
    creature.cooldown += world.computeCost(action)
    queue.add(entity)
    return action
  }
}