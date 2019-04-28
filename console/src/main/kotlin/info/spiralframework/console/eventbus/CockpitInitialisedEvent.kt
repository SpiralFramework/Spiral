package info.spiralframework.console.eventbus

import info.spiralframework.console.Cockpit
import info.spiralframework.console.CockpitMechanic
import info.spiralframework.console.CockpitPilot
import info.spiralframework.console.CockpitUpdate
import info.spiralframework.core.plugins.events.SpiralEvent

open class CockpitInitialisedEvent<T: Cockpit<T>> private constructor(open val cockpit: T): SpiralEvent {
    companion object {
        @Suppress("UNCHECKED_CAST")
        operator fun <T: Cockpit<T>> invoke(cockpit: T): CockpitInitialisedEvent<T> {
            return when (cockpit) {
                is CockpitMechanic -> CockpitMechanicInitialisedEvent(cockpit) as CockpitInitialisedEvent<T>
                is CockpitPilot -> CockpitPilotInitialisedEvent(cockpit) as CockpitInitialisedEvent<T>
                is CockpitUpdate -> CockpitUpdateInitialisedEvent(cockpit) as CockpitInitialisedEvent<T>
                else -> CockpitInitialisedEvent(cockpit)
            }
        }
    }
    
    data class CockpitMechanicInitialisedEvent(override val cockpit: CockpitMechanic): CockpitInitialisedEvent<CockpitMechanic>(cockpit)
    data class CockpitPilotInitialisedEvent(override val cockpit: CockpitPilot): CockpitInitialisedEvent<CockpitPilot>(cockpit)
    data class CockpitUpdateInitialisedEvent(override val cockpit: CockpitUpdate): CockpitInitialisedEvent<CockpitUpdate>(cockpit)
}