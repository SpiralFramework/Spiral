package info.spiralframework.console.data

import com.fasterxml.jackson.annotation.JsonProperty
import info.spiralframework.base.binding.isAnsiSupported

data class GurrenArgs(
        val disableUpdateCheck: Boolean = DEFAULTS.DISABLE_UPDATE_CHECK,
        val isTool: Boolean = DEFAULTS.IS_TOOL,
        val timeCommands: Boolean = DEFAULTS.TIME_COMMANDS,
        val silenceOutput: Boolean = DEFAULTS.SILENCE_OUTPUT,
        val ansiEnabled: Boolean = DEFAULTS.ANSI_ENABLED,
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

        const val DISABLE_CONFIG_LOAD = "disable config load"
        const val DISABLE_CONFIG_LOAD_SHORT = 'c'

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

        fun disableConfigLoad(args: Array<String>): Boolean = args hasArg DISABLE_CONFIG_LOAD || args hasShortArg DISABLE_CONFIG_LOAD_SHORT
    }
    object DEFAULTS {
        const val DISABLE_UPDATE_CHECK = false
        const val IS_TOOL = false
        const val TIME_COMMANDS = false
        const val SILENCE_OUTPUT = false
        const val ANSI_ENABLED = false
    }

    class Pojo(
            @JsonProperty("disable_update_check")   val disableUpdateCheck: Boolean?,
            @JsonProperty("is_tool")                val isTool: Boolean?,
            @JsonProperty("time_commands")          val timeCommands: Boolean?,
            @JsonProperty("silence_output")         val silenceOutput: Boolean?,
            @JsonProperty("ansi_enabled")           val ansiEnabled: Boolean?
    )

    constructor(args: Array<String>): this(
            args hasArg DISABLE_UPDATE_CHECK || args hasShortArg DISABLE_UPDATE_CHECK_SHORT,
            args hasArg USE_AS_TOOL || args hasShortArg USE_AS_TOOL_SHORT,
            args hasArg TIME_COMMANDS || args hasShortArg TIME_COMMANDS_SHORT,
            args hasArg SILENCE_OUTPUT || args hasShortArg SILENCE_OUTPUT_SHORT,
            (isAnsiSupported() || args hasArg ENABLE_ANSI || args hasShortArg ENABLE_ANSI_SHORT) && !(args hasArg DISABLE_ANSI || args hasShortArg DISABLE_ANSI_SHORT),
            args
    )

    constructor(args: Array<String>, pojo: GurrenArgs.Pojo): this(
            args hasArg DISABLE_UPDATE_CHECK || args hasShortArg DISABLE_UPDATE_CHECK_SHORT || (pojo.disableUpdateCheck ?: DEFAULTS.DISABLE_UPDATE_CHECK),
            args hasArg USE_AS_TOOL || args hasShortArg USE_AS_TOOL_SHORT || (pojo.isTool ?: DEFAULTS.IS_TOOL),
            args hasArg TIME_COMMANDS || args hasShortArg TIME_COMMANDS_SHORT || (pojo.timeCommands ?: DEFAULTS.TIME_COMMANDS),
            args hasArg SILENCE_OUTPUT || args hasShortArg SILENCE_OUTPUT_SHORT || (pojo.silenceOutput ?: DEFAULTS.SILENCE_OUTPUT),
            (isAnsiSupported() || args hasArg ENABLE_ANSI || args hasShortArg ENABLE_ANSI_SHORT || pojo.ansiEnabled == true) && !(args hasArg DISABLE_ANSI || args hasShortArg DISABLE_ANSI_SHORT),
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