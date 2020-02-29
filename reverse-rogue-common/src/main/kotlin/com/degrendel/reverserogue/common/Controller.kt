package com.degrendel.reverserogue.common

sealed class Controller
object PlayerControlled : Controller()
data class SimpleAI(val behaviors: List<Behavior>, val weightMin: Int = 0, val weightMax: Int = 0): Controller()
data class AgentAI(val agent: SoarAgent) : Controller()

sealed class Behavior
{
  abstract val weight: Int
}
data class MoveTowardsRogue(override val weight: Int = 0): Behavior()
data class MoveTowardsConjurer(override val weight: Int = 0): Behavior()
