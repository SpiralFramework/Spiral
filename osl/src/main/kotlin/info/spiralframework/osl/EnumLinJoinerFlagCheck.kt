package info.spiralframework.osl

enum class EnumLinJoinerFlagCheck(val names: Array<String>, val flag: Int) {
    AND(arrayOf("&&", "and"), 7),
    OR(arrayOf("||", "or"), 6);

    companion object {
        val NAMES: Array<String> by lazy { values().flatMap{ enum -> enum.names.toList() }.toTypedArray().sortedArray().reversedArray() }
    }
}
