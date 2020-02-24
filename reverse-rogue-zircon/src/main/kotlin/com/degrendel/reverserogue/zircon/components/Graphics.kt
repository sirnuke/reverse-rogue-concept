package com.degrendel.reverserogue.zircon.components

import com.badlogic.ashley.core.Component
import com.degrendel.reverserogue.common.Position

/** Stores where this entity is currently drawn. */
data class DrawnAtComponent(val position: Position) : Component