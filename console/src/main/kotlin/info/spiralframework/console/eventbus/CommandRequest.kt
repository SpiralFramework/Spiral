package info.spiralframework.console.eventbus

data class CommandRequest(val command: String) {
    private val mutableFoundCommands: MutableList<ParboiledCommand> = ArrayList()
    val foundCommands: List<ParboiledCommand>
        get() = mutableFoundCommands

    fun register(command: ParboiledCommand) {
        mutableFoundCommands.add(command)
    }
}