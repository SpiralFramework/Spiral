package info.spiralframework.console.data

import info.spiralframework.base.ANSI

data class GurrenArgs(
        val disableUpdateCheck: Boolean = false,
        val isTool: Boolean = false,
        val timeCommands: Boolean = false,
        val silenceOutput: Boolean = false,
        val ansiEnabled: Boolean = false,
        val rawArgs: Array<String>
) {
    companion object Keys {
        const val DISABLE_UPDATE_CHECK = "disable update check"
        const val DISABLE_UPDATE_CHECK_SHORT = 'u'
        const val USE_AS_TOOL = "tool"
        const val USE_AS_TOOL_SHORT = 'T'
        const val TIME_COMMANDS = "time commands"
        const val TIME_COMMANDS_SHORT = 't'
        const val SILENCE_OUTPUT = "suppress"
        const val SILENCE_OUTPUT_SHORT = 'S'
        const val ENABLE_ANSI = "ansi"
        const val ENABLE_ANSI_SHORT = 'A'
        const val DISABLE_ANSI = "disable ansi"
        const val DISABLE_ANSI_SHORT = 'p'

        val SPACE_REGEX = "\\s".toRegex()
        infix fun Array<String>.hasArg(arg: String): Boolean {
            val hyphens = "--${arg.replace(SPACE_REGEX, "-")}"
            val underscore = "--${arg.replace(SPACE_REGEX, "_")}"
            val spaces = "--$arg"
            return any { str -> str.equals(hyphens, true) || str.equals(underscore, true) || str.equals(spaces, true) }
        }

        infix fun Array<String>.hasShortArg(arg: Char): Boolean {
            val shortArgs = firstOrNull { str -> str.startsWith("-") && !str.startsWith("--") } ?: return false
            return (arg in shortArgs)
        }

        infix fun String.isArg(arg: String): Boolean {
            val hyphens = "--${arg.replace(SPACE_REGEX, "-")}"
            val underscore = "--${arg.replace(SPACE_REGEX, "_")}"
            val spaces = "--$arg"

            return equals(hyphens, true) || equals(underscore, true) || equals(spaces, true)
        }

        val String.isShortArg: Boolean
            get() = startsWith("-") && !startsWith("--") && (DISABLE_UPDATE_CHECK_SHORT in this || USE_AS_TOOL_SHORT in this || SILENCE_OUTPUT_SHORT in this)
    }

    constructor(args: Array<String>): this(
            args hasArg DISABLE_UPDATE_CHECK || args hasShortArg DISABLE_UPDATE_CHECK_SHORT,
            args hasArg USE_AS_TOOL || args hasShortArg USE_AS_TOOL_SHORT,
            args hasArg TIME_COMMANDS || args hasShortArg TIME_COMMANDS_SHORT,
            args hasArg SILENCE_OUTPUT || args hasShortArg SILENCE_OUTPUT_SHORT,
            (ANSI.supported || args hasArg ENABLE_ANSI || args hasShortArg ENABLE_ANSI_SHORT) && !(args hasArg DISABLE_ANSI || args hasShortArg DISABLE_ANSI_SHORT),
            args
    )

    val filteredArgs = rawArgs.filterNot { arg -> arg isArg DISABLE_UPDATE_CHECK || arg isArg USE_AS_TOOL || arg.isShortArg }

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