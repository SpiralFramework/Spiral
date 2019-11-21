package info.spiralframework.osl.drills.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.game.hpa.UDG
import info.spiralframework.formats.game.hpa.UnknownHopesPeakGame
import info.spiralframework.formats.scripting.lin.*
import info.spiralframework.formats.scripting.lin.dr1.DR1TrialCameraEntry
import info.spiralframework.formats.scripting.lin.dr2.DR2TrialCameraEntry
import info.spiralframework.formats.scripting.lin.udg.UDGTextEntry
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var
import kotlin.reflect.KClass

object LinDialogueDrill : DrillHead<Array<LinEntry>> {
    val NAME = info.spiralframework.osl.AllButMatcher(charArrayOf(':', '\n', '('))
    val cmd = "LIN-DIALOGUE"

    override val klass: KClass<Array<LinEntry>> = Array<LinEntry>::class
    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val character = Var<Int>(0)

        return Sequence(
                clearTmpStack(cmd),

                Sequence(
                        OneOrMore(NAME),
                        Action<Any> {
                            val speakerName = match().trim()

                            val game = hopesPeakGame ?: UnknownHopesPeakGame
                            val id = customIdentifiers[speakerName]
                                    ?: game.characterIdentifiers[speakerName]
                                    ?: return@Action false

                            character.set(id)
                            push(id)
                        },
                        pushDrillHead(cmd, this@LinDialogueDrill),
                        pushTmpFromStack(cmd),
                        OptionalInlineWhitespace(),
                        FirstOf(
                                Sequence(
                                        "(",
                                        Optional(
                                                FirstOf(
                                                        "Sprite:",
                                                        "Emotion:",
                                                        "Sprite",
                                                        "Emotion"
                                                )
                                        ),
                                        OptionalInlineWhitespace(),
                                        SpriteEmotion(character),
                                        pushTmpFromStack(cmd),
                                        FirstOf(
                                                Sequence(
                                                        CommaSeparator(),
                                                        Optional(
                                                                FirstOf(
                                                                        "Camera:",
                                                                        "Camera",
                                                                        "Trial Camera:",
                                                                        "Trial Camera"
                                                                )
                                                        ),
                                                        TrialCameraID(),
                                                        pushTmpFromStack(cmd),
                                                        pushTmpFromStack(cmd)
                                                ),
                                                Sequence(
                                                        pushTmpAction(cmd, ""),
                                                        pushTmpAction(cmd, "")
                                                )
                                        ),
                                        OptionalInlineWhitespace(),
                                        ")"
                                ),
                                Sequence(
                                        pushTmpAction(cmd, ""),
                                        pushTmpAction(cmd, ""),
                                        pushTmpAction(cmd, "")
                                )
                        ),
                        ':',
                        OptionalInlineWhitespace(),
                        LinText(cmd)
                ),

                pushStackWithHead(cmd)
        )
    }

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinEntry> {
        val game = parser.hopesPeakGame ?: UnknownHopesPeakGame
        val speakerID = rawParams[0].toString().toIntOrNull() ?: 0
        val spriteID = rawParams[1].toString().toIntOrNull()
        val trialCameraMajor = rawParams[2].toString().toIntOrNull()
        val trialCameraMinor = rawParams[3].toString().toIntOrNull()
        val trialCamera = if (trialCameraMajor != null && trialCameraMinor != null) (trialCameraMajor shl 8) or trialCameraMinor else null
        val text = rawParams[4].toString()

        val entries: MutableList<LinEntry> = ArrayList(6)

        if (spriteID != null)
            entries.add(SpriteEntry(if (parser.gameContext is info.spiralframework.osl.GameContext.HopesPeakTrialContext) speakerID else 0, speakerID, spriteID, 0, 0))

        if (trialCamera != null) {
            entries.add(when (game) {
                DR1 -> DR1TrialCameraEntry(speakerID, trialCamera)
                DR2 -> DR2TrialCameraEntry(speakerID, trialCamera, 0, 0, 0)
                else -> TODO("Trial cameras is not documented for $game")
            })
        }

        entries.addAll(when (game) {
            DR1 -> arrayOf(
                    SpeakerEntry(speakerID),
                    TextEntry(text, -1),
                    WaitFrameEntry.DR1,
                    WaitForInputEntry.DR1
            )
            DR2 -> arrayOf(
                    SpeakerEntry(speakerID),
                    TextEntry(text, -1),
                    WaitFrameEntry.DR2,
                    WaitForInputEntry.DR2
            )
            UDG -> arrayOf(
                    UnknownEntry(0x15, intArrayOf(speakerID)),
                    UDGTextEntry(text, -1),
                    WaitForInputEntry.UDG
            )
            else -> TODO("Dialogue is not documented for $game")
        })

        return entries.toTypedArray()
    }
}
