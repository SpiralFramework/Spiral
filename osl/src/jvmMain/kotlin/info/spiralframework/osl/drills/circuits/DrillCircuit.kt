package info.spiralframework.osl.drills.circuits

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import kotlin.reflect.KClass

interface DrillCircuit: DrillHead<Unit> {
    override val klass: KClass<Unit>
        get() = Unit::class

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {}
}
