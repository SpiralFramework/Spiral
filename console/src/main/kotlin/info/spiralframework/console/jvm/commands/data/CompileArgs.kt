package info.spiralframework.console.jvm.commands.data

import info.spiralframework.core.common.formats.WritableSpiralFormat
import java.io.File

public class CompileArgs {
    public companion object {
        public val EMPTY: File = File("<empty>")
    }

    public data class Immutable(
        val compilingDir: File?,
        val compileDestination: File?,
        val formatOverride: WritableSpiralFormat<*>?,
        val filter: Regex?,
        val attemptCompression: Boolean?
    )

    public var compilingDir: File? = null
    public var compileDestination: File? = null
    public var formatOverride: WritableSpiralFormat<*>? = null
    public var filter: Regex? = null
    public var attemptCompression: Boolean? = false

    public var builder: Boolean = false

    public fun makeImmutable(
        defaultCompilingDir: File? = null,
        defaultCompileDestination: File? = null,
        defaultFormatOverride: WritableSpiralFormat<*>? = null,
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