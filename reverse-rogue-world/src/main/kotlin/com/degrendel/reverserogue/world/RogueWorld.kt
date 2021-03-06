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

  val simpleAI = BehaviorAI(this)

  override val ecs = Engine()
  private val actionQueueSystem = ActionQueueSystem(this)

  private val allSpawnedEntities = Family.all(CreatureComponent::class.java, PositionComponent::class.java).get()
  private val levels = mutableMapOf<Int, LevelState>()

  init
  {
    ecs.addEntityListener(allSpawnedEntities, actionQueueSystem)
    (0 until Level.FLOORS).forEach { floor -> levels[floor] = LevelState(this, floor) }
  }

  private var nextCreatureId = 1
  private var clock = 0L

  override val conjurer: Entity = Entity()
      .add(CreatureComponent(getNextCreatureId(), CreatureType.CONJURER, PlayerControlled, active = true, cooldown = 0L))
      .add(AllegianceComponent(Allegiance.CONJURER))
  override val rogue: Entity = ecs.createEntity()
      // TODO: This is agent controlled, once that is wired up
      .add(CreatureComponent(getNextCreatureId(), CreatureType.ROGUE, SimpleAI(listOf(MoveTowardsConjurer())), active = true, cooldown = 0L))
      .add(AllegianceComponent(Allegiance.ROGUE))

  init
  {
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
    // TODO: If we need a lot more performance with drawing, this /should/ work as expected
    //      the frontend access the levels, but the actual modifications shouldn't impact it?
    //      It desyncs the drawing from the turn execution, which is visible noticable, however
    /*
    launch {
      while (true)
      {
        frontend.refreshMap()
        delay(100L)
      }
    }
     */
    frontend.refreshMap()
    while (true)
    {
      updateWorld()
      val action = actionQueueSystem.execute()
      executeAction(action)
      // TODO: Alternatively for performance, offer a 'peak ahead' in actionQueueSystem.  If the next action is a
      //       simple AI (i.e. should be near immediately), skip refreshing the map.  Could also have a timer that
      //       asserts it hasn't been too long.
      frontend.refreshMap()
    }
  }

  private fun executeAction(action: Action)
  {
    assert(isValidAction(action))
    when (action)
    {
      is Sleep -> L.debug("Sleeping...")
      is Move ->
      {
        val position = action.entity.getPosition()
        val level = levels.getValue(position.floor)
        level.moveCreature(action.entity, action.direction)
      }
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
    // TODO: Weight move and whatnot by an armor cost?
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