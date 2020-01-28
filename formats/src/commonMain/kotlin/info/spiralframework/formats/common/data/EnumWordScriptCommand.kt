package info.spiralframework.formats.common.data

enum class EnumWordScriptCommand {
    LABEL,
    PARAMETER,
    TEXT,
    RAW;

    companion object {
        operator fun invoke(num: Int): EnumWordScriptCommand {
            return when (num % 4) {
                0 -> PARAMETER
                1 -> RAW
                2 -> TEXT
                3 -> LABEL
                else -> RAW
            }
        }
    }
}