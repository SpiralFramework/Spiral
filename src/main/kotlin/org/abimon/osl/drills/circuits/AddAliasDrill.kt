package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Rule

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

    fun addAnimationID(stack: Array<Any>, parser: OpenSpiralLanguageParser) {
        val major = stack[1].toString().toIntOrNull() ?: 0
        val minor = stack[2].toString().toIntOrNull() ?: 0

        val id = (major shl 8) or minor

        parser.customAnimationNames[stack[0].toString()] = id
    }

    fun addFlagID(stack: Array<Any>, parser: OpenSpiralLanguageParser) {
        val group = stack[1].toString().toIntOrNull() ?: 0
        val flagID = stack[2].toString().toIntOrNull() ?: 0

        val id = (group shl 8) or flagID

        parser.customFlagNames[stack[0].toString()] = id
    }

    fun addItemID(stack: Array<Any>, parser: OpenSpiralLanguageParser) {
        parser.customItemNames[stack[0].toString()] = stack[1].toString().toIntOrNull() ?: 0
    }
    
    fun addLabelID(stack: Array<Any>, parser: OpenSpiralLanguageParser) {
        val first = stack[1].toString().toIntOrNull() ?: 0
        val second = stack[2].toString().toIntOrNull() ?: 0

        val id = (first shl 8) or second

        parser.customLabelNames[stack[0].toString()] = id
    }

    fun addNameID(stack: Array<Any>, parser: OpenSpiralLanguageParser) {
        parser.customIdentifiers[stack[0].toString()] = stack[1].toString().toIntOrNull() ?: 0
    }

    fun addEvidenceID(stack: Array<Any>, parser: OpenSpiralLanguageParser) {
        parser.customEvidenceNames[stack[0].toString()] = stack[1].toString().toIntOrNull() ?: 0
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
        }

        ALIAS_BINDINGS = bindings
    }
}