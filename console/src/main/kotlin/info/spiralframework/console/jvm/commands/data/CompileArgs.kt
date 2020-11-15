package info.spiralframework.console.jvm.commands.data

import info.spiralframework.core.common.formats.WritableSpiralFormat
import java.io.File

class CompileArgs {
    companion object {
        val EMPTY = File("<empty>")
    }

    data class Immutable(val compilingDir: File?, val compileDestination: File?, val formatOverride: WritableSpiralFormat?, val filter: Regex?, val attemptCompression: Boolean?)

    var compilingDir: File? = null
    var compileDestination: File? = null
    var formatOverride: WritableSpiralFormat? = null
    var filter: Regex? = null
    var attemptCompression: Boolean? = false

    var builder: Boolean = false

    fun makeImmutable(
        defaultCompilingDir: File? = null,
        defaultCompileDestination: File? = null,
        defaultFormatOverride: WritableSpiralFormat? = null,
        defaultFilter: Regex? = null,
        defaultAttemptCompression: Boolean? = null
    ): Immutable =
            Immutable(
                    compilingDir ?: defaultCompilingDir,
                    compileDestination ?: defaultCompileDestination,
                    formatOverride ?: defaultFormatOverride,
                    filter ?: defaultFilter,
                    attemptCompression ?: defaultAttemptCompression
            )
}