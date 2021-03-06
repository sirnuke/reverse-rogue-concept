package com.degrendel.reverserogue.common.components

import com.badlogic.ashley.core.ComponentMapper

object ComponentMaps
{
  val position: ComponentMapper<PositionComponent> = ComponentMapper.getFor(PositionComponent::class.java)
  val squareType: ComponentMapper<SquareTypeComponent> = ComponentMapper.getFor(SquareTypeComponent::class.java)
  val wallOrientation: ComponentMapper<WallOrientationComponent> = ComponentMapper.getFor(WallOrientationComponent::class.java)
  val roomData: ComponentMapper<RoomComponent> = ComponentMapper.getFor(RoomComponent::class.java)
  val creature: ComponentMapper<CreatureComponent> = ComponentMapper.getFor(CreatureComponent::class.java)
  val allegiance: ComponentMapper<AllegianceComponent> = ComponentMapper.getFor(AllegianceComponent::class.java)
}
