package org.abimon.osl

enum class EnumLinFlagCheck(val names: Array<String>, val flag: Int) {
    NOT_EQUALS(arrayOf("!=", "!==", "!is", "does not equal", "not equal to"), 0),
    EQUALS(arrayOf("==", "===", "is", "equals", "equal to"), 1),
    LESS_OR_EQUAL(arrayOf("<=", "≤", "less than or equal to", "less or equal"), 2),
    GREATER_OR_EQUAL(arrayOf(">=", "≥", "greater than or equal to", "greater or equal"), 3),
    LESS_THAN(arrayOf("<", "less than", "less"), 4),
    GREATER_THAN(arrayOf(">", "greater than", "greater"), 5);

    companion object {
        val NAMES: Array<String> by lazy { values().flatMap{ enum -> enum.names.toList() }.toTypedArray().sortedArray().reversedArray() }
    }
}