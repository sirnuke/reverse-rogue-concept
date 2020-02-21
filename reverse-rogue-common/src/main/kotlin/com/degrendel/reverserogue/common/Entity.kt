package com.degrendel.reverserogue.common

interface Entity
{
  val position: Position
  val type: EntityType
}

enum class EntityType
{
  ROGUE
}