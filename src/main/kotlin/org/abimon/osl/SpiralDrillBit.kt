package org.abimon.osl

import org.abimon.osl.drills.DrillHead
import org.abimon.osl.drills.StaticDrill

data class SpiralDrillBit(val head: DrillHead<out Any>) {
    companion object {
        inline operator fun <reified T: Any> invoke(value: T): SpiralDrillBit = SpiralDrillBit(StaticDrill(value, T::class), "")
    }

    var script: String = ""

    constructor(head: DrillHead<out Any>, script: String): this(head) {
        this.script = script
    }
}