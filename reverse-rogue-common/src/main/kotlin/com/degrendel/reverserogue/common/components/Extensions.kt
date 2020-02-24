package com.degrendel.reverserogue.common.components

import com.badlogic.ashley.core.Entity
import com.degrendel.reverserogue.common.Position

// Mapper helper functions
fun Entity.getPosition() = ComponentMaps.position.get(this).position
fun Entity.getSquare(): SquareTypeComponent = ComponentMaps.squareType.get(this)
fun Entity.getWallOrientation() = ComponentMaps.wallOrientation.get(this).wallOrientation
fun Entity.getRoomData() = ComponentMaps.roomData.get(this)!!
fun Entity.getCreatureType() = ComponentMaps.creatureType.get(this).type
fun Entity.getAllegiance() = ComponentMaps.allegiance.get(this).allegiance

fun Entity.isWithinRoom(position: Position): Boolean
{
  val roomPos = this.getPosition()
  val roomData = this.getRoomData()
  return (position.x >= roomPos.x
      && position.x < roomPos.x + roomData.width
      && position.y >= roomPos.y
      && position.y < roomPos.y + roomData.height)
}
