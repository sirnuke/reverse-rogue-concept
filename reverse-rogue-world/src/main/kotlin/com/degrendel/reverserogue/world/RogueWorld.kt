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

  override val ecs = Engine()

  override fun generateLevel() = LevelState()
}