package com.degrendel.reverserogue.common

import com.badlogic.ashley.core.Component

/** Indicates that this entity is currently visible to the Rogue. */
class VisibleComponent : Component

/** Indicates that this entity has been seen by the Rogue, even if it is not currently visible. */
class KnownComponent : Component

/** Tracks the position of this entity in the map. */
data class PositionComponent(var pos: Position) : Component

/** Indicates this entity is a square in the level. */
data class SquareTypeComponent(val type: SquareType) : Component
{

}
