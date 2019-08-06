package info.spiralframework.osl

enum class EnumMetaIfOperations(private val compare: (info.spiralframework.osl.OpenSpiralLanguageParser, String, String) -> Boolean, vararg val names: String) {
    EQUALS(info.spiralframework.osl.ComparisonOperations::equal, "==", "===", "is", "equals", "equal to"),
    NOT_EQUALS(info.spiralframework.osl.ComparisonOperations::notEqual, "!=", "!==", "!is", "does not equal", "not equal to");

    companion object {
        val NAMES: Array<String> by lazy { values().flatMap{ enum -> enum.names.toList() }.toTypedArray() }
    }

    operator fun invoke(parser: info.spiralframework.osl.OpenSpiralLanguageParser, first: String, second: String): Boolean = compare(parser, first, second)
}
