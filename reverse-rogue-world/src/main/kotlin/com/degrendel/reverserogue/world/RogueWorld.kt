package com.degrendel.reverserogue.world

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.common.components.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RogueWorld(override val frontend: Frontend, override val agent: SoarAgent) : World
{
  companion object
  {
    private val L by logger()
  }

  override val ecs = Engine()

  private val allSpawnedEntities = Family.all(CreatureTypeComponent::class.java, PositionComponent::class.java).get()
  private val levels = mutableMapOf<Int, LevelState>()

  init
  {
    (0 until Level.FLOORS).forEach { floor -> levels[floor] = LevelState(this, floor) }
  }

  private var nextCreatureId = 1
  private var clock = 0L

  override val conjurer: Entity = Entity()
      .add(CreatureTypeComponent(getNextCreatureId(), CreatureType.CONJURER, ControllerType.PLAYER, 0L))
      .add(AllegianceComponent(Allegiance.CONJURER))
  override val rogue: Entity = ecs.createEntity()
      // TODO: This is agent controlled, once that is wired up
      .add(CreatureTypeComponent(getNextCreatureId(), CreatureType.ROGUE, ControllerType.SIMPLE_AI, 0L))
      .add(AllegianceComponent(Allegiance.ROGUE))

  private val actionQueueSystem = ActionQueueSystem(this)

  init
  {
    ecs.addEntityListener(allSpawnedEntities, actionQueueSystem)
    levels.getValue(0).let {
      val rooms = it.getRandomRooms(3)
      assert(rooms.size == 3)
      // TODO: Rogue will eventually spawn in different rooms (up versus down)
      // TODO: Use this to spawn the staircases
      // TODO: Should conjurer spawn in the opposite staircase?
      it.spawnCreature(conjurer, it.getRandomPointInRoom(rooms[0]))
      it.spawnCreature(rogue, it.getRandomPointInRoom(rooms[1]))
    }
    updateWorld()
  }

  override fun updateWorld()
  {
    ecs.update(0.0f)
  }

  override fun runGame(): Job = GlobalScope.launch {
    while (true)
    {
      updateWorld()
      actionQueueSystem.execute()
      frontend.refreshMap()
      delay(100L)
    }
  }

  override fun isValidAction(action: Action): Boolean
  {
    val level = getLevel(action.entity.getPosition().floor)
    return when (action)
    {
      is Sleep -> true
      is Move -> level.canMoveTo(action.entity.getPosition(), action.direction)
    }
  }

  override fun computeCost(action: Action): Long
  {
    // TODO: Make these configurable plus action values
    // TODO: Should a diagonal move cost more than straight?
    return when (action)
    {
      is Sleep -> 100L
      is Move -> 120L
    }
  }

  override fun getLevel(floor: Int) = levels.getValue(floor)

  private fun getNextCreatureId(): Int
  {
    val id = nextCreatureId
    nextCreatureId++
    return id
  }
}