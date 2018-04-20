package org.abimon.osl

import org.abimon.osl.drills.DrillHead

data class SpiralDrillBit(val head: DrillHead<out Any>) {
    lateinit var script: String

    constructor(head: DrillHead<out Any>, script: String): this(head) {
        this.script = script
    }
}