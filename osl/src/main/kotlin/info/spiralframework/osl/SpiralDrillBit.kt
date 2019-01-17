package info.spiralframework.osl

import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.osl.drills.StaticDrill

data class SpiralDrillBit(val head: DrillHead<out Any>) {
    companion object {
        inline operator fun <reified T: Any> invoke(value: T): SpiralDrillBit = SpiralDrillBit(StaticDrill(value, T::class), "")
    }

    var script: String = ""

    constructor(head: DrillHead<out Any>, script: String): this(head) {
        this.script = script
    }
}
