package com.degrendel.reverserogue.common

interface Frontend
{
  suspend fun getPlayerInput(): Action
  suspend fun refreshMap()
}