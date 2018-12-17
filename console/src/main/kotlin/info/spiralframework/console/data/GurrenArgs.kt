package info.spiralframework.console.data

data class GurrenArgs(
        val disableUpdateCheck: Boolean = false,
        val isTool: Boolean = false,
        val rawArgs: Array<String>
) {
    companion object Keys {
        const val DISABLE_UPDATE_CHECK = "disable update check"
        const val USE_AS_TOOL = "tool"

        val SPACE_REGEX = "\\s".toRegex()
        infix fun Array<String>.hasArg(arg: String): Boolean {
            val hyphens = "--${arg.replace(SPACE_REGEX, "-")}"
            val underscore = "--${arg.replace(SPACE_REGEX, "_")}"
            val spaces = "--$arg"
            return any { str -> str.equals(hyphens, true) || str.equals(underscore, true) || str.equals(spaces, true) }
        }
    }

    constructor(args: Array<String>): this(
            args hasArg DISABLE_UPDATE_CHECK,
            args hasArg USE_AS_TOOL,
            args
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GurrenArgs) return false

        if (!rawArgs.contentEquals(other.rawArgs)) return false

        return true
    }

    override fun hashCode(): Int {
        return rawArgs.contentHashCode()
    }
}