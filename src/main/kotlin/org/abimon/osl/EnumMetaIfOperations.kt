package org.abimon.osl

enum class EnumMetaIfOperations(private val compare: (OpenSpiralLanguageParser, String, String) -> Boolean, vararg val names: String) {
    EQUALS(ComparisonOperations::equal, "==", "===", "is", "equals", "equal to"),
    NOT_EQUALS(ComparisonOperations::notEqual, "!=", "!==", "!is", "does not equal", "not equal to");

    companion object {
        val NAMES: Array<String> by lazy { values().flatMap{ enum -> enum.names.toList() }.toTypedArray() }
    }

    operator fun invoke(parser: OpenSpiralLanguageParser, first: String, second: String): Boolean = compare(parser, first, second)
}