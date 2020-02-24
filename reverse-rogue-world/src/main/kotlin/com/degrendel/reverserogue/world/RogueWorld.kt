package com.degrendel.reverserogue.world

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.degrendel.reverserogue.common.Allegiance
import com.degrendel.reverserogue.common.CreatureType
import com.degrendel.reverserogue.common.World
import com.degrendel.reverserogue.common.components.AllegianceComponent
import com.degrendel.reverserogue.common.components.CreatureTypeComponent
import com.degrendel.reverserogue.common.components.PositionComponent
import com.degrendel.reverserogue.common.logger

class RogueWorld : World
{
  companion object
  {
    private val L by logger()
  }

  private var _currentLevel: LevelState? = null
  override val currentLevel get() = _currentLevel

  override val ecs = Engine()

  override val conjurer: Entity = ecs.createEntity()
      .add(CreatureTypeComponent(CreatureType.CONJURER))
      .add(AllegianceComponent(Allegiance.CONJURER))
  override val rogue: Entity = ecs.createEntity()
      .add(CreatureTypeComponent(CreatureType.ROGUE))
      .add(AllegianceComponent(Allegiance.ROGUE))

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
}