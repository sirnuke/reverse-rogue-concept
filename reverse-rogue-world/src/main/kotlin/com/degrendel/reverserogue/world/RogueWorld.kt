package com.degrendel.reverserogue.world

import com.badlogic.ashley.core.Engine
import com.degrendel.reverserogue.common.World
import com.degrendel.reverserogue.common.logger

class RogueWorld : World
{
  companion object
  {
    private val L by logger()
  }

  private var currentLevel: LevelState? = null

  override val ecs = Engine()

  override fun generateLevel(): LevelState
  {
    currentLevel?.removeFromECS()
    LevelState(this).let {
      currentLevel = it
      return it
    }
  }

  override fun update()
  {
    ecs.update(0.0f)
  }
}