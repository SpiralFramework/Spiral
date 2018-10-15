package org.abimon.osl.results

import org.abimon.osl.SpiralDrillBit
import org.abimon.osl.data.nonstopDebate.NonstopDebateMinigame
import org.abimon.osl.data.nonstopDebate.NonstopDebateNewObject
import org.abimon.osl.data.nonstopDebate.NonstopDebateVariable
import org.abimon.osl.data.nonstopDebate.OSLVariable
import org.abimon.spiral.core.objects.game.hpa.HopesPeakKillingGame
import org.abimon.spiral.core.objects.scripting.CustomLin
import org.abimon.spiral.core.objects.scripting.CustomNonstopDebate
import org.abimon.spiral.core.objects.scripting.NonstopDebateSection
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.LinTextScript
import org.abimon.spiral.core.utils.castToTypedArray
import kotlin.reflect.KClass

open class CustomNonstopMinigameOSL(val game: HopesPeakKillingGame) : OSLCompilation<Triple<CustomLin, CustomLin, CustomNonstopDebate>> {
    var operatingBlock: Int = 0
    val minigame = NonstopDebateMinigame(game)
    var stage: Int = 0

    override fun <T : Any> handle(drill: SpiralDrillBit, product: T, klass: KClass<out T>) {
        when (product) {
            is LinScript -> {
                if (product is LinTextScript && stage <= OSLVariable.VALUES.NONSTOP_STAGE_TEXT && operatingBlock == OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_NONE) {
                    if (stage < OSLVariable.VALUES.NONSTOP_STAGE_TEXT)
                        stage = OSLVariable.VALUES.NONSTOP_STAGE_TEXT
                    minigame.addSection(nonstopDebateSection(game.nonstopDebateSectionSize / 2, this::defaultInit))
                }

                when (stage) {
                    OSLVariable.VALUES.NONSTOP_STAGE_PRE_SCRIPT -> minigame.addPreScriptEntry(product)
                    OSLVariable.VALUES.NONSTOP_STAGE_PRE_TEXT -> minigame.addPreTextEntry(product)
                    OSLVariable.VALUES.NONSTOP_STAGE_TEXT -> {
                        when (operatingBlock) {
                            OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_NONE -> minigame.addWorkingTextEntry(product)
                            OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_SUCCESS -> minigame.addCorrectEntry(product)
                            OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_FAIL -> minigame.addIncorrectEntry(product)
                            OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_TEXT -> minigame.addTextEntry(product)
                        }
                    }
                    OSLVariable.VALUES.NONSTOP_STAGE_POST_TEXT -> minigame.addPostTextEntry(product)
                    OSLVariable.VALUES.NONSTOP_STAGE_POST_SCRIPT -> minigame.addPostScriptEntry(product)
                }
            }
            is Array<*> -> {
                if (klass == CustomLinOSL.ARRAY_LIN_SCRIPT) {
                    val scripts = product.castToTypedArray<LinScript>() ?: emptyArray()

                    if (scripts.any { script -> script is LinTextScript } && stage <= OSLVariable.VALUES.NONSTOP_STAGE_TEXT && operatingBlock == OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_NONE) {
                        if (stage < OSLVariable.VALUES.NONSTOP_STAGE_TEXT)
                            stage = OSLVariable.VALUES.NONSTOP_STAGE_TEXT
                        minigame.addSection(nonstopDebateSection(game.nonstopDebateSectionSize / 2, this::defaultInit))
                    }
                    when (stage) {
                        OSLVariable.VALUES.NONSTOP_STAGE_PRE_SCRIPT -> minigame.addPreScriptEntries(scripts)
                        OSLVariable.VALUES.NONSTOP_STAGE_PRE_TEXT -> minigame.addPreTextEntries(scripts)
                        OSLVariable.VALUES.NONSTOP_STAGE_TEXT -> {
                            when (operatingBlock) {
                                OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_NONE -> minigame.addWorkingTextEntries(scripts)
                                OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_SUCCESS -> minigame.addCorrectEntries(scripts)
                                OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_FAIL -> minigame.addIncorrectEntries(scripts)
                                OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_TEXT -> minigame.addTextEntries(scripts)
                            }
                        }
                        OSLVariable.VALUES.NONSTOP_STAGE_POST_TEXT -> minigame.addPostTextEntries(scripts)
                        OSLVariable.VALUES.NONSTOP_STAGE_POST_SCRIPT -> minigame.addPostScriptEntries(scripts)
                    }
                }
            }
            is NonstopDebateVariable -> minigame[product.index] = product.data
            is NonstopDebateNewObject -> {
                if (stage < OSLVariable.VALUES.NONSTOP_STAGE_TEXT)
                    stage = OSLVariable.VALUES.NONSTOP_STAGE_TEXT

                minigame.addSection(nonstopDebateSection(product.size, this::defaultInit))
            }
            is OSLVariable<*> -> {
                when (product.key) {
                    OSLVariable.KEYS.NONSTOP_TIMELIMIT -> minigame.customNonstopDebate.timeLimit = product.value as? Int ?: 300
                    OSLVariable.KEYS.NONSTOP_CHANGE_STAGE -> stage = product.value as Int
                    OSLVariable.KEYS.NONSTOP_CHANGE_OPERATING_BLOCK -> operatingBlock = product.value as Int
                    OSLVariable.KEYS.NONSTOP_CORRECT_EVIDENCE -> minigame.sectionStack?.shootWithEvidence = product.value as Int
                    OSLVariable.KEYS.NONSTOP_DEBATE_NUMBER -> minigame.debateNumber = product.value as Int
                    OSLVariable.KEYS.NONSTOP_DEBATE_COUPLED_SCRIPT -> {
                        if (product.value === OSLVariable.VALUES.NONSTOP_COUPLED_SCRIPT_NULL) {
                            minigame.coupledScript = null
                        } else {
                            minigame.coupledScript = (product.value as Triple<*, *, *>).let { (a, b, c) -> if (a !is Int || b !is Int || c !is Int) null else Triple(a, b, c) }
                        }
                    }
                }
            }
        }
    }

    override fun produce(): Triple<CustomLin, CustomLin, CustomNonstopDebate> = minigame.makeObjects()
    fun produce(chapter: Int?, scene: Int?, room: Int?): Triple<CustomLin, CustomLin, CustomNonstopDebate> = minigame.makeObjects(chapter, scene, room)

    fun defaultInit(debate: NonstopDebateSection) {
        debate.advance = 60 * 2
        debate.fadeout = (60 * 1.75).toInt()
        debate.horizontal = 210
        debate.vertical = 170
        debate.scale = 100
        debate.finalScale = 100
        debate.shouldShootWithEvidence = false
    }

    fun nonstopDebateSection(size: Int, init: NonstopDebateSection.() -> Unit): NonstopDebateSection {
        val section = NonstopDebateSection(size)
        section.init()
        return section
    }
}