package info.spiralframework.osl.results

import info.spiralframework.formats.customNonstopDebate
import info.spiralframework.formats.game.hpa.HopesPeakKillingGame
import info.spiralframework.formats.scripting.CustomNonstopDebate
import info.spiralframework.formats.scripting.NonstopDebateSection
import info.spiralframework.osl.SpiralDrillBit
import info.spiralframework.osl.data.nonstopDebate.NonstopDebateNewObject
import info.spiralframework.osl.data.nonstopDebate.NonstopDebateVariable
import info.spiralframework.osl.data.nonstopDebate.OSLVariable
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
