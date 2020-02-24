package com.degrendel.reverserogue.common.components

import com.badlogic.ashley.core.Component
import com.degrendel.reverserogue.common.Position
import com.degrendel.reverserogue.common.SquareType
import com.degrendel.reverserogue.common.WallOrientation

/** Indicates that this entity is currently visible to the Rogue. */
class VisibleComponent : Component

/** Indicates that this entity has been seen by the Rogue, even if it is not currently visible. */
class KnownComponent : Component

/** Tracks the position of this entity in the map. */
data class PositionComponent(val position: Position) : Component

/** Indicates this entity is a square in the level. */
data class SquareTypeComponent(val type: SquareType) : Component

/** Records the orientation of this wall square. */
data class WallOrientationComponent(val wallOrientation: WallOrientation) : Component

/** Tracks the id and dimensions of the room. */
data class RoomComponent(val id: Int, val width: Int, val height: Int) : Component
