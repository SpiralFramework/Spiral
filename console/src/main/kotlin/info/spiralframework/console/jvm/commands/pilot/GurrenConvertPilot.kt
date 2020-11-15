package info.spiralframework.console.jvm.commands.pilot

import info.spiralframework.console.jvm.commands.CommandRegistrar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class GurrenConvertPilot: CoroutineScope {
//    companion object: CommandRegistrar {
//
//    }
    override val coroutineContext: CoroutineContext = SupervisorJob()
}