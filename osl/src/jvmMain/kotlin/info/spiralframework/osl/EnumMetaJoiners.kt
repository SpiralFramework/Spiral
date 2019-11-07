package info.spiralframework.osl

enum class EnumMetaJoiners(private val compare: (Boolean, Boolean) -> Boolean, vararg val names: String) {
    AND(info.spiralframework.osl.ComparisonOperations::and, "&&", "and"),
    OR(info.spiralframework.osl.ComparisonOperations::or, "||", "or"),
    NAND(info.spiralframework.osl.ComparisonOperations::nand, "nand"),
    NOR(info.spiralframework.osl.ComparisonOperations::nor, "nor"),
    XOR(info.spiralframework.osl.ComparisonOperations::xor, "xor", "^");

    companion object {
        val NAMES: Array<String> by lazy { values().flatMap{ enum -> enum.names.toList() }.toTypedArray() }
    }

    operator fun invoke(first: Boolean, second: Boolean): Boolean = compare(first, second)
}
