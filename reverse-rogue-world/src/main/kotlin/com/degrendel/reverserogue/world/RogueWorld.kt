package com.degrendel.reverserogue.world

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.common.components.AllegianceComponent
import com.degrendel.reverserogue.common.components.CreatureTypeComponent
import com.degrendel.reverserogue.common.components.PositionComponent
import com.degrendel.reverserogue.common.components.getPosition

class RogueWorld : World
{
  companion object
  {
    private val L by logger()
  }

  private var _currentLevel: LevelState? = null
  override val currentLevel get() = _currentLevel

  override val ecs = Engine()

  override val conjurer: Entity = Entity()
      .add(CreatureTypeComponent(CreatureType.CONJURER))
      .add(AllegianceComponent(Allegiance.CONJURER))
      .let { ecs.addEntity(it); it }
  override val rogue: Entity = ecs.createEntity()
      .add(CreatureTypeComponent(CreatureType.ROGUE))
      .add(AllegianceComponent(Allegiance.ROGUE))
      .let { ecs.addEntity(it); it }

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
      val entryPoints = it.getRandomRooms(3)
      assert(entryPoints.size == 3)
      // TODO: Rogue will eventually spawn in different rooms (up versus down)
      // TODO: Use this to spawn the staircases
      // TODO: Select random spot in the room
      // TODO: Should conjurer spawn in the opposite staircase?
      it.spawnCreature(conjurer, entryPoints[0].getPosition())
      it.spawnCreature(rogue, entryPoints[1].getPosition())
    }
  }

  override fun move(entity: Entity, direction: EightWay)
  {
    val old = entity.getPosition()
    val new = old.move(direction)

    _currentLevel!!.let {
      if (!it.canMoveTo(new)) return
      it.moveCreature(entity, new)
    }

    update()
  }
}