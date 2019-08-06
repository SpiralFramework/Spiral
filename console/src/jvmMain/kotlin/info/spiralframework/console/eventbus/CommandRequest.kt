package info.spiralframework.console.eventbus

import info.spiralframework.console.data.SpiralScope

data class CommandRequest(val command: String, val scope: SpiralScope) {
    private val mutableFoundCommands: MutableList<ParboiledCommand> = ArrayList()
    val foundCommands: List<ParboiledCommand>
        get() = mutableFoundCommands

    fun register(command: ParboiledCommand) {
        mutableFoundCommands.add(command)
    }
}