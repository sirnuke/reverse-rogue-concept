package com.degrendel.reverserogue.common

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.EntitySystem
import com.degrendel.reverserogue.common.components.getCreature
import java.util.*

class ActionQueueSystem(private val world: World) : EntitySystem(), EntityListener
{
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

  override fun entityRemoved(entity: Entity)
  {
    queue.add(entity)
  }

  override fun entityAdded(entity: Entity)
  {
    queue.remove(entity)
  }

  override fun update(deltaTime: Float)
  {
    world.updateCreature(queue.poll() ?: return)
  }
}