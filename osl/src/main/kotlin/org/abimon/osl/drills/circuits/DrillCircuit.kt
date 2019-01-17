package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import kotlin.reflect.KClass

interface DrillCircuit: DrillHead<Unit> {
    override val klass: KClass<Unit>
        get() = Unit::class

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {}
}