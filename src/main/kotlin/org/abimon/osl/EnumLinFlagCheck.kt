package org.abimon.osl

enum class EnumLinFlagCheck(val names: Array<String>, val flag: Int) {
    NOT_EQUALS(arrayOf("!=", "!==", "!is", "does not equal", "not equal to"), 0),
    EQUALS(arrayOf("==", "===", "is", "equals", "equal to"), 1);

    companion object {
        val NAMES: Array<String> by lazy { values().flatMap{ enum -> enum.names.toList() }.toTypedArray() }
    }
}