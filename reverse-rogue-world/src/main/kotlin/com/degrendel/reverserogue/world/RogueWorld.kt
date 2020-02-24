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
    _currentLevel?.removeFromECS()
    conjurer.remove(PositionComponent::class.java)
    rogue.remove(PositionComponent::class.java)

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
    conjurer.add(PositionComponent(Position(0, 0)))
    rogue.add(PositionComponent(Position(2, 2)))
  }

  override fun move(entity: Entity, direction: EightWay)
  {
    val position = entity.getPosition()
    // NOTE: explicitly removing position triggers an update on listeners
    entity.remove(PositionComponent::class.java)
    entity.add(PositionComponent(position.move(direction)))

    update()
  }
}