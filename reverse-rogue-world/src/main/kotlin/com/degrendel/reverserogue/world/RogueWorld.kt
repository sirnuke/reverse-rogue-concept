package com.degrendel.reverserogue.world

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.common.components.*
import org.hexworks.cobalt.events.api.EventBus

class RogueWorld(override val eventBus: EventBus) : World
{
  companion object
  {
    private val L by logger()
  }

  private val allSpawnedEntities = Family.all(CreatureTypeComponent::class.java, PositionComponent::class.java)

  private var _currentLevel: LevelState? = null
  override val currentLevel get() = _currentLevel

  private var nextCreatureId = 1
  private var clock = 0L

  override val ecs = Engine()

  override val conjurer: Entity = Entity()
      .add(CreatureTypeComponent(getNextCreatureId(), CreatureType.CONJURER, 0L))
      .add(AllegianceComponent(Allegiance.CONJURER))
      .let { ecs.addEntity(it); it }
  override val rogue: Entity = ecs.createEntity()
      .add(CreatureTypeComponent(getNextCreatureId(), CreatureType.ROGUE, 0L))
      .add(AllegianceComponent(Allegiance.ROGUE))
      .let { ecs.addEntity(it); it }

  private val actionQueueSystem = ActionQueueSystem(this)

  init
  {
    ecs.addSystem(actionQueueSystem)
  }

  override fun generateLevel(): LevelState
  {
    _currentLevel?.let {
      it.despawnCreature(conjurer)
      it.despawnCreature(rogue)
      it.removeFromECS()
    }

    val nextLevel = LevelState(this)
    _currentLevel = nextLevel
    update()
    return nextLevel
  }

  override fun update()
  {
    ecs.update(0.0f)
  }

  override fun spawn()
  {
    _currentLevel!!.let {
      val rooms = it.getRandomRooms(3)
      assert(rooms.size == 3)
      // TODO: Rogue will eventually spawn in different rooms (up versus down)
      // TODO: Use this to spawn the staircases
      // TODO: Should conjurer spawn in the opposite staircase?
      it.spawnCreature(conjurer, it.getRandomPointInRoom(rooms[0]))
      it.spawnCreature(rogue, it.getRandomPointInRoom(rooms[1]))
    }
  }

  override fun move(entity: Entity, direction: EightWay)
  {
    _currentLevel!!.let {
      if (!it.canMoveTo(entity.getPosition(), direction)) return
      it.moveCreature(entity, direction)
    }

    update()
  }

  override fun updateCreature(creature: Entity)
  {
    TODO("not implemented")
  }

  private fun getNextCreatureId(): Int
  {
    val id = nextCreatureId
    nextCreatureId++
    return id
  }
}