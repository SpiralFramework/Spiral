package org.abimon.osl.results

import org.abimon.osl.SpiralDrillBit
import org.abimon.osl.WordScriptCommand
import org.abimon.spiral.core.objects.scripting.CustomWordScript
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import kotlin.reflect.KClass

open class CustomWordScriptOSL : OSLCompilation<CustomWordScript> {
    companion object {
        val WRD_SCRIPT = WrdScript::class.java
        val ARRAY_WRD_SCRIPT = Array<WrdScript>::class
        const val compileText = false //temporary
    }

    val wrd = CustomWordScript()

    override fun <T : Any> handle(drill: SpiralDrillBit, product: T, klass: KClass<out T>) {
        when (product) {
            is WrdScript -> wrd.add(product)
            is Array<*> -> if (klass == ARRAY_WRD_SCRIPT) wrd.addAll(product.filterIsInstance(WRD_SCRIPT))
            is WordScriptCommand -> {
                when (product.type) {
                    EnumWordScriptCommand.LABEL -> if (product.command !in wrd.labels) wrd.label(product.command)
                    EnumWordScriptCommand.PARAMETER -> if (product.command !in wrd.parameters) wrd.parameter(product.command)
                    EnumWordScriptCommand.STRING -> if (product.command !in wrd.strings && compileText) wrd.strings.add(product.command)
                    EnumWordScriptCommand.RAW -> {
                    }
                }
            }
        }
    }

    override fun produce(): CustomWordScript = wrd
}