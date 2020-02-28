package com.degrendel.reverserogue.zircon.events

import com.degrendel.reverserogue.common.Action
import org.hexworks.cobalt.events.api.Event

data class PlayerActionInput(val action: Action, override val emitter: Any): Event