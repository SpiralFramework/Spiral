package info.spiralframework.antlr.osl

import dev.brella.kornea.toolkit.common.SemanticVersion
import info.spiralframework.osb.common.OSLUnion
import info.spiralframework.osb.common.OpenSpiralBitcodeBuilder
import info.spiralframework.osb.common.OpenSpiralBitcodeBuilderBranch
import info.spiralframework.osb.common.OpenSpiralBitcodeFlagCondition
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public sealed class OSLVisitorUnion {
    public data class Value<T : OSLUnion>(val union: T) : OSLVisitorUnion()
    public data class ValueList<T : OSLUnion>(val unions: List<T>) : OSLVisitorUnion()

    public data class Header(val version: SemanticVersion? = null) : OSLVisitorUnion()
    public data class AddFunctionCall(
        val functionName: String,
        val functionParameters: Array<OSLUnion.FunctionParameterType>
    ) : OSLVisitorUnion()

    public data class AddOpcode(val opcode: Int, val parameters: Array<OSLUnion>) : OSLVisitorUnion()
    public data class AddNamedOpcode(val opcodeName: String, val parameters: Array<OSLUnion>) : OSLVisitorUnion()
    public data class SetVariable(val name: String, val value: OSLUnion) : OSLVisitorUnion()
    public data class AddDialogue(val speakerName: String, val dialogue: OSLUnion) : OSLVisitorUnion()
    public data class AddDialogueVariable(val speakerVariable: String, val dialogue: OSLUnion) : OSLVisitorUnion()

    public data class Script(val header: Header, val scope: Scope) : OSLVisitorUnion() {
        public suspend fun writeToBuilder(builder: OpenSpiralBitcodeBuilder) {
            header.version?.let { v -> builder.setVersion(v) }
            scope.writeToBuilder(builder)
        }
    }

    public data class Scope(val lines: List<OSLVisitorUnion>) : OSLVisitorUnion() {
        public suspend fun writeToBuilder(builder: OpenSpiralBitcodeBuilder) {
            lines.forEach { union ->
                when (union) {
                    is Header -> union.version?.let { v -> builder.setVersion(v) }
                    is AddFunctionCall -> builder.addFunctionCall(union.functionName, union.functionParameters)
                    is AddOpcode -> builder.addOpcode(union.opcode, union.parameters)
                    is AddNamedOpcode -> builder.addOpcode(union.opcodeName, union.parameters)
                    is SetVariable -> builder.setVariable(union.name, union.value)
                    is AddDialogue -> builder.addDialogue(union.speakerName, union.dialogue)
                    is AddDialogueVariable -> builder.addDialogueVariable(union.speakerVariable, union.dialogue)
                    is Script -> union.writeToBuilder(builder)
                    is Scope -> union.writeToBuilder(builder)
                    is IfCheck -> {
                        builder.addIfCheck(
                            union.check.toCheck(),
                            union.elseIf.map(CheckBranch::toCheck),
                            union.elseScope?.let { scope -> scope::writeToBuilder }
                        )
                    }
                    is CheckFlag -> {
                        builder.addCheckFlag(
                            union.check.toCheck(),
                            union.elseIf.map(CheckBranch::toCheck),
                            union.elseScope?.let { scope -> scope::writeToBuilder }
                        )
                    }
                    is CheckCharacter -> {
                        builder.addOpcode("Check Character", arrayOf(union.characterID.union.parameterValue))
                        union.scope.writeToBuilder(builder)
                    }
                    is CheckObject -> {
                        builder.addOpcode("Check Object", arrayOf(union.objectID.union.parameterValue))
                        union.scope.writeToBuilder(builder)
                    }
                    is LoadMap -> {
                        builder.addLoadMap(
                            arrayOf(
                                union.mapID.union,
                                union.state?.union,
                                union.arg3?.union
                            ).filterNotNull(), union.scope::writeToBuilder
                        )
                    }
                    is SelectPresent -> {
                        builder.addPresentSelection(union.scope::writeToBuilder)
                    }
                    is TreeBranch -> {
                        builder.addOpcode("Branch", arrayOf(union.branchNum?.union ?: OSLUnion.NullType))
                        union.scope.writeToBuilder(builder)
                    }
                    else -> {}
                }
            }
        }
    }

    public data class Equality(val op: Int) : OSLVisitorUnion()
    public data class Logical(val op: Int) : OSLVisitorUnion()

    public data class Condition(val checking: Value<*>?, val equality: Equality, val against: Value<*>?) : OSLVisitorUnion() {
        public fun toFlagCondition(): OpenSpiralBitcodeFlagCondition =
            OpenSpiralBitcodeFlagCondition(
                checking?.union ?: OSLUnion.NullType,
                equality.op,
                against?.union ?: OSLUnion.NullType
            )
    }

    public data class CheckBranch(
        val condition: Condition,
        val otherConditions: Array<Pair<Logical, Condition>>,
        val scope: Scope
    ) : OSLVisitorUnion() {
        public fun toCheck(): OpenSpiralBitcodeBuilderBranch =
            OpenSpiralBitcodeBuilderBranch(
                condition.toFlagCondition(),
                Array(otherConditions.size) { i ->
                    Pair(
                        otherConditions[i].first.op,
                        otherConditions[i].second.toFlagCondition()
                    )
                },
                scope::writeToBuilder
            )
    }

    public data class IfCheck(val check: CheckBranch, val elseIf: List<CheckBranch>, val elseScope: Scope?) : OSLVisitorUnion()
    public data class CheckFlag(val check: CheckBranch, val elseIf: List<CheckBranch>, val elseScope: Scope?) :
        OSLVisitorUnion()

    public data class CheckCharacter(val characterID: Value<OSLUnion.FunctionParameterType>, val scope: Scope) :
        OSLVisitorUnion()

    public data class CheckObject(val objectID: Value<OSLUnion.FunctionParameterType>, val scope: Scope) : OSLVisitorUnion()
    public data class LoadMap(
        val mapID: Value<OSLUnion.FunctionParameterType>,
        val state: Value<OSLUnion.FunctionParameterType>?,
        val arg3: Value<OSLUnion.FunctionParameterType>?,
        val scope: Scope
    ) : OSLVisitorUnion()

    public data class SelectPresent(val scope: Scope) : OSLVisitorUnion()
    public data class TreeBranch(val branchNum: Value<*>?, val scope: Scope) : OSLVisitorUnion()
}

public inline fun <T : OSLUnion> wrapUnionValue(block: () -> T): OSLVisitorUnion.Value<T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return OSLVisitorUnion.Value(block())
}