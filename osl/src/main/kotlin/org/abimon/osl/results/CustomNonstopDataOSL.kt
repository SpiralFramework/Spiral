package org.abimon.osl.results

import org.abimon.osl.SpiralDrillBit
import org.abimon.osl.data.nonstopDebate.NonstopDebateNewObject
import org.abimon.osl.data.nonstopDebate.NonstopDebateVariable
import org.abimon.osl.data.nonstopDebate.OSLVariable
import org.abimon.spiral.core.objects.customNonstopDebate
import org.abimon.spiral.core.objects.game.hpa.HopesPeakKillingGame
import org.abimon.spiral.core.objects.scripting.CustomNonstopDebate
import org.abimon.spiral.core.objects.scripting.NonstopDebateSection
import kotlin.reflect.KClass

open class CustomNonstopDataOSL(game: HopesPeakKillingGame) : OSLCompilation<CustomNonstopDebate> {
    val nonstop = customNonstopDebate { this.game = game }

    override fun <T : Any> handle(drill: SpiralDrillBit, product: T, klass: KClass<out T>) {
        when (product) {
            is NonstopDebateVariable -> {
                nonstop.currentSection?.let { nonstopSection ->
                    if (product.index < nonstopSection.data.size)
                        nonstopSection[product.index] = product.data
                }
            }
            is NonstopDebateNewObject -> {
                nonstop.currentSection = NonstopDebateSection(product.size)
            }

            is OSLVariable<*> -> {
                when (product.key) {
                    OSLVariable.KEYS.NONSTOP_TIMELIMIT -> nonstop.timeLimit = product.value as? Int ?: 300
                    OSLVariable.KEYS.COMPILE_AS -> nonstop.currentSection?.let(nonstop::section)
                }
            }
        }
    }

    override fun produce(): CustomNonstopDebate = nonstop
}