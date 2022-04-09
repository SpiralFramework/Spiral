package info.spiralframework.formats.common.data

public enum class EnumWordScriptCommand {
    LABEL,
    PARAMETER,
    TEXT,
    RAW;

    public companion object {
        public operator fun invoke(num: Int): EnumWordScriptCommand {
            return when (num and 3) { //num % 4
                0 -> PARAMETER
                1 -> RAW
                2 -> TEXT
                3 -> LABEL
                else -> RAW
            }
        }
    }
}