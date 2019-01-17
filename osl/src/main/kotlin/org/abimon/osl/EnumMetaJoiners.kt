package org.abimon.osl

enum class EnumMetaJoiners(private val compare: (Boolean, Boolean) -> Boolean, vararg val names: String) {
    AND(ComparisonOperations::and, "&&", "and"),
    OR(ComparisonOperations::or, "||", "or"),
    NAND(ComparisonOperations::nand, "nand"),
    NOR(ComparisonOperations::nor, "nor"),
    XOR(ComparisonOperations::xor, "xor", "^");

    companion object {
        val NAMES: Array<String> by lazy { values().flatMap{ enum -> enum.names.toList() }.toTypedArray() }
    }

    operator fun invoke(first: Boolean, second: Boolean): Boolean = compare(first, second)
}