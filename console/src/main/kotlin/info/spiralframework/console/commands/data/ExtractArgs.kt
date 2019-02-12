package info.spiralframework.console.commands.data

import java.io.File

class ExtractArgs {
    data class Immutable(val extractPath: File?, val filter: Regex?, val destDir: File?, val leaveCompressed: Boolean?)
    var extractPath: File? = null
    var filter: Regex? = null
    var destDir: File? = null
    var leaveCompressed: Boolean? = null
    var builder: Boolean = false

    fun makeImmutable(
            defaultExtractPath: File? = null,
            defaultFilter: Regex? = null,
            defaultDestDir: File? = null,
            defaultLeaveCompressed: Boolean? = null
    ): ExtractArgs.Immutable =
            Immutable(
                    extractPath ?: defaultExtractPath,
                    filter ?: defaultFilter,
                    destDir ?: defaultDestDir,
                    leaveCompressed ?: defaultLeaveCompressed
            )
}