package com.degrendel.reverserogue.zircon

import com.degrendel.reverserogue.common.Position
import org.hexworks.zircon.api.data.Position3D

fun Position.toPosition3D() = Position3D.create(this.x, this.y, this.floor)
fun Position.toPosition() = org.hexworks.zircon.api.data.Position.create(this.x, this.y)

enum class MouseButtons(val id: Int)
{
  LEFT(java.awt.event.MouseEvent.BUTTON1),
  MIDDLE(java.awt.event.MouseEvent.BUTTON2),
  RIGHT(java.awt.event.MouseEvent.BUTTON3),
  ;
}
