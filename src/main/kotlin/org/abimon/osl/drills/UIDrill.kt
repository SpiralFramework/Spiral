package org.abimon.osl.drills

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.spiral.core.objects.scripting.lin.ChangeUIEntry
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object UIDrill : DrillHead<LinScript> {
    val cmd: String = "UI-DRILL"
    val uiElements = mapOf(
            "Speaker Name" to 1,
            "HUD Disabler" to 7,
            "HUD" to 3,
            "Class Trial" to 8,
            "Sprites" to 9,
            "Save Prompt" to 12,
            "Map Transition" to 15,
            "Camera Control" to 26,
            "MonoMono Machine" to 41,
            "Minimal UI" to 51
    )

    val NUMERAL_REGEX = "\\d+".toRegex()

    override val klass: KClass<LinScript> = LinScript::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "UI:",
                    ZeroOrMore(Whitespace()),
                    pushTmpAction(cmd, this@UIDrill),
                    FirstOf(
                            Sequence(
                                    FirstOf("Enable", "Disable"),
                                    pushTmpAction(cmd),
                                    ZeroOrMore(Whitespace()),
                                    FirstOf(
                                            FirstOf(uiElements.keys.toTypedArray()),
                                            OneOrMore(Digit())
                                    ),
                                    pushTmpAction(cmd)
                            ),
                            Sequence(
                                    "Set",
                                    Whitespace(),
                                    FirstOf(
                                            FirstOf(uiElements.keys.toTypedArray()),
                                            OneOrMore(Digit())
                                    ),
                                    Action<Any> { push(match()) },
                                    ZeroOrMore(Whitespace()),
                                    "to",
                                    ZeroOrMore(Whitespace()),
                                    OneOrMore(Digit()),
                                    pushTmpAction(cmd),
                                    Action<Any> { context -> pushTmpAction(cmd, pop()).run(context) }
                            )
                    ),
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val uiStateStr = rawParams[0].toString()
        val uiElement = rawParams[1].toString()

        val uiState: Int

        if (uiStateStr == "Enable")
            uiState = 1
        else if (uiStateStr == "Disable")
            uiState = 0
        else
            uiState = uiStateStr.toIntOrNull() ?: 0

        return ChangeUIEntry(uiElements[uiElement] ?: uiElement.toIntOrNull() ?: 0, uiState)
    }
}