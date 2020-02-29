package com.degrendel.reverserogue.common

sealed class Controller
object PlayerControlled : Controller()
data class SimpleAI(val behaviors: List<Behavior>): Controller()
data class AgentAI(val agent: SoarAgent) : Controller()

sealed class Behavior
{
  abstract val weightMin: Int
  abstract val weightMax: Int
}
data class MoveTowardsRogue(override val weightMin: Int = 0, override val weightMax: Int = 0): Behavior()
data class MoveTowardsConjurer(override val weightMin: Int = 0, override val weightMax: Int = 0): Behavior()
