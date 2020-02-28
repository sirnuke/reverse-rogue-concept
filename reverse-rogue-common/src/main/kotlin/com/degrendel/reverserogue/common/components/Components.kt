package com.degrendel.reverserogue.common.components

import com.badlogic.ashley.core.Component
import com.degrendel.reverserogue.common.*

/** Indicates that this entity is currently visible to the Rogue. */
class VisibleComponent : Component

/** Indicates that this entity has been seen by the Rogue, even if it is not currently visible. */
class KnownComponent : Component

/** Tracks the position of this entity in the map. */
data class PositionComponent(val position: Position) : Component

/** Indicates this entity is a square in the level. */
data class SquareTypeComponent(val type: SquareType, val roomId: Int?, val visibleRooms: Set<Int>) : Component

/** Records the orientation of this wall square. */
data class WallOrientationComponent(val wallOrientation: WallOrientation) : Component

/** Tracks the id and dimensions of the room. */
data class RoomComponent(val id: Int, val width: Int, val height: Int) : Component

/** Indicates this entity is a creature. */
data class CreatureTypeComponent(val id: Int, val type: CreatureType, val controller: ControllerType, var cooldown: Long) : Component

/** Indicates what team this creature belongs to. */
data class AllegianceComponent(val allegiance: Allegiance) : Component

enum class ControllerType
{
  SIMPLE_AI,
  AGENT,
  PLAYER
}
