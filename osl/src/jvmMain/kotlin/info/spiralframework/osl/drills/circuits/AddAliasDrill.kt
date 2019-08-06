package info.spiralframework.osl.drills.circuits

import info.spiralframework.osl.OpenSpiralLanguageParser
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var

class AddAliasDrill(parser: OpenSpiralLanguageParser) : DrillCircuit {
    val cmd = "ADD-ALIAS-ENTRIES"

    val ALIAS_BINDINGS: Map<String, Pair<Rule, (Array<Any>, OpenSpiralLanguageParser) -> Unit>>

    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val aliases = ALIAS_BINDINGS.map { (aliasName, rulePair) ->
            Sequence(
                    aliasName,
                    InlineWhitespace(),
                    "alias",
                    InlineWhitespace(),
                    pushDrillHead(cmd, this@AddAliasDrill),
                    pushTmpAction(cmd, aliasName),
                    Parameter(cmd),
                    InlineWhitespace(),
                    FirstOf("to", "as", "under"),
                    InlineWhitespace(),
                    rulePair.first
            )
        }.toTypedArray()

        return Sequence(
                clearTmpStack(cmd),
                Sequence(
                        "Add",
                        InlineWhitespace(),
                        FirstOf(aliases),
                        operateOnTmpActions(cmd) { params -> operate(this, params.drop(1).toTypedArray()) }
                ),

                pushStackWithHead(cmd)
        )
    }

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        if (parser.silence)
            return

        ALIAS_BINDINGS[rawParams[0].toString()]?.second?.invoke(rawParams.drop(1).toTypedArray(), parser)
    }

    fun addByteID(stack: Array<Any>, map: MutableMap<String, Int>) {
        map[stack[0].toString()] = stack[1].toString().toIntOrNull() ?: 0
    }

    fun addShortID(stack: Array<Any>, map: MutableMap<String, Int>) {
        val major = stack[1].toString().toIntOrNull() ?: 0
        val minor = stack[2].toString().toIntOrNull() ?: 0

        val id = (major shl 8) or minor

        map[stack[0].toString()] = id
    }

    fun addAnimationID(stack: Array<Any>, parser: OpenSpiralLanguageParser) = addShortID(stack, parser.customAnimationNames)
    fun addFlagID(stack: Array<Any>, parser: OpenSpiralLanguageParser) = addShortID(stack, parser.customFlagNames)
    fun addItemID(stack: Array<Any>, parser: OpenSpiralLanguageParser) = addByteID(stack, parser.customItemNames)
    fun addLabelID(stack: Array<Any>, parser: OpenSpiralLanguageParser) = addShortID(stack, parser.customLabelNames)
    fun addNameID(stack: Array<Any>, parser: OpenSpiralLanguageParser) = addByteID(stack, parser.customIdentifiers)
    fun addEvidenceID(stack: Array<Any>, parser: OpenSpiralLanguageParser) = addByteID(stack, parser.customEvidenceNames)
    fun addTrialCameraID(stack: Array<Any>, parser: OpenSpiralLanguageParser) = addShortID(stack, parser.customTrialCameraNames)
    fun addCutinID(stack: Array<Any>, parser: OpenSpiralLanguageParser) = addByteID(stack, parser.customCutinNames)
    fun addOperatorID(stack: Array<Any>, parser: OpenSpiralLanguageParser) = addByteID(stack, parser.customOperatorNames)
    fun addJoinerOperatorID(stack: Array<Any>, parser: OpenSpiralLanguageParser) = addByteID(stack, parser.customJoinerOperatorNames)

    fun addEmotionID(stack: Array<Any>, parser: OpenSpiralLanguageParser) {
        val charID = stack[1].toString().toIntOrNull() ?: 0
        val emotionsMap = parser.customEmotionNames[charID] ?: HashMap()

        emotionsMap[stack[0].toString()] = stack[2].toString().toIntOrNull() ?: 0

        parser.customEmotionNames[charID] = emotionsMap
    }

    init {
        val bindings: MutableMap<String, Pair<Rule, (Array<Any>, OpenSpiralLanguageParser) -> Unit>> = HashMap()

        parser.apply {
            bindings["animation"] = Sequence(
                    AnimationID(),
                    pushTmpFromStack(cmd),
                    pushTmpFromStack(cmd)
            ) to this@AddAliasDrill::addAnimationID

            bindings["flag"] = Sequence(
                    Flag(),
                    pushTmpFromStack(cmd),
                    pushTmpFromStack(cmd)
            ) to this@AddAliasDrill::addFlagID

            bindings["item"] = Sequence(
                    ItemID(),
                    pushTmpFromStack(cmd)
            ) to this@AddAliasDrill::addItemID

            bindings["item name"] = Sequence(
                    ItemID(),
                    pushTmpFromStack(cmd)
            ) to this@AddAliasDrill::addItemID

            bindings["label"] = Sequence(
                    Label(),
                    pushTmpFromStack(cmd),
                    pushTmpFromStack(cmd)
            ) to this@AddAliasDrill::addLabelID

            bindings["name"] = Sequence(
                    SpeakerName(),
                    pushTmpFromStack(cmd)
            ) to this@AddAliasDrill::addNameID

            bindings["evidence"] = Sequence(
                    EvidenceID(),
                    pushTmpFromStack(cmd)
            ) to this@AddAliasDrill::addEvidenceID

            val speakerNameVar = Var<Int>()
            val emotionRule = Sequence(
                    Action<Any> { speakerNameVar.set(0) },
                    SpeakerName(),
                    Action<Any> {
                        val speakerName = pop().toString().toIntOrNull() ?: 0

                        pushTmp(cmd, speakerName)

                        return@Action speakerNameVar.set(speakerName)
                    },
                    InlineWhitespace(),
                    Optional("for", InlineWhitespace()),
                    Optional("character", InlineWhitespace()),

                    SpriteEmotion(speakerNameVar),
                    pushTmpAction(cmd)
            )

            bindings["emotion"] = emotionRule to this@AddAliasDrill::addEmotionID
            bindings["sprite"] = emotionRule to this@AddAliasDrill::addEmotionID

            bindings["trial camera"] = Sequence(
                    TrialCameraID(),
                    pushTmpFromStack(cmd),
                    pushTmpFromStack(cmd)
            ) to this@AddAliasDrill::addTrialCameraID

            bindings["cutin"] = Sequence(
                    CutinID(),
                    pushTmpFromStack(cmd)
            ) to this@AddAliasDrill::addCutinID

            bindings["cut in"] = Sequence(
                    CutinID(),
                    pushTmpFromStack(cmd)
            ) to this@AddAliasDrill::addCutinID

            bindings["operator"] = Sequence(
                    LinIfOperator(),
                    pushTmpFromStack(cmd)
            ) to this@AddAliasDrill::addOperatorID

            bindings["joiner operator"] = Sequence(
                    JoinerOperator(),
                    pushTmpFromStack(cmd)
            ) to this@AddAliasDrill::addJoinerOperatorID
        }

        ALIAS_BINDINGS = bindings
    }
}
