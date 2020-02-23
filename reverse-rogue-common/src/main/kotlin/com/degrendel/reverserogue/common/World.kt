package com.degrendel.reverserogue.common

import com.badlogic.ashley.core.Engine

interface World
{
  val ecs: Engine
  fun generateLevel(): Level
}
