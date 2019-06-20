package info.spiralframework.osl.data

import info.spiralframework.osl.GameContext

sealed class OSLEnvironment {
    data class Context(val context: GameContext): OSLEnvironment()
    object NONE: OSLEnvironment()
}