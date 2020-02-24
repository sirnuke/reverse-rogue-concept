package com.degrendel.reverserogue.common.components

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity

object ComponentMaps
{
  val position: ComponentMapper<PositionComponent> = ComponentMapper.getFor(PositionComponent::class.java)
  val squareType: ComponentMapper<SquareTypeComponent> = ComponentMapper.getFor(SquareTypeComponent::class.java)
  val wallOrientation: ComponentMapper<WallOrientationComponent> = ComponentMapper.getFor(WallOrientationComponent::class.java)
  val roomData: ComponentMapper<RoomComponent> = ComponentMapper.getFor(RoomComponent::class.java)
}

fun Entity.getPosition() = ComponentMaps.position.get(this).position
fun Entity.getSquareType() = ComponentMaps.squareType.get(this).type
fun Entity.getWallOrientation() = ComponentMaps.wallOrientation.get(this).wallOrientation
fun Entity.getRoomData() = ComponentMaps.roomData.get(this)!!
