package info.spiralframework.formats.scripting

enum class EnumWordScriptCommand {
    LABEL,
    PARAMETER,
    STRING,
    RAW;

    companion object {
        public inline fun arrayOf(vararg elements: Int): Array<EnumWordScriptCommand> = Array(elements.size) { index ->
            when (elements[index]) {
                0 -> PARAMETER
                1 -> RAW
                2 -> STRING
                3 -> LABEL
                else -> PARAMETER
            }
        }
    }
}