package info.spiralframework.console.jvm.eventbus

import info.spiralframework.base.common.events.SpiralEvent
import info.spiralframework.console.jvm.Cockpit
import info.spiralframework.console.jvm.CockpitMechanic
import info.spiralframework.console.jvm.CockpitPilot
import info.spiralframework.console.jvm.CockpitUpdate
import kotlin.time.ExperimentalTime

open class CockpitInitialisedEvent<T: Cockpit> private constructor(open val cockpit: T): SpiralEvent {
    companion object {
        @ExperimentalTime
        @Suppress("UNCHECKED_CAST")
        operator fun <T: Cockpit> invoke(cockpit: T): CockpitInitialisedEvent<T> {
            return when (cockpit) {
                is CockpitMechanic -> CockpitMechanicInitialisedEvent(cockpit) as CockpitInitialisedEvent<T>
                is CockpitPilot -> CockpitPilotInitialisedEvent(cockpit) as CockpitInitialisedEvent<T>
                is CockpitUpdate -> CockpitUpdateInitialisedEvent(cockpit) as CockpitInitialisedEvent<T>
                else -> CockpitInitialisedEvent(cockpit)
            }
        }
    }
    
    @ExperimentalTime
    data class CockpitMechanicInitialisedEvent(override val cockpit: CockpitMechanic): CockpitInitialisedEvent<CockpitMechanic>(cockpit)
    data class CockpitPilotInitialisedEvent(override val cockpit: CockpitPilot): CockpitInitialisedEvent<CockpitPilot>(cockpit)
    data class CockpitUpdateInitialisedEvent(override val cockpit: CockpitUpdate): CockpitInitialisedEvent<CockpitUpdate>(cockpit)
}