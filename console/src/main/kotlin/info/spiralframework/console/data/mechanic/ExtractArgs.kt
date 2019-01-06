package info.spiralframework.console.data.mechanic

import java.io.File

open class ExtractArgs {
    data class Immutable(val extractPath: File?, val filter: String?, val destDir: File?)
    var extractPath: File? = null
    var filter: String? = null
    var destDir: File? = null

    fun makeImmutable(defaultExtractPath: File? = null, defaultFilter: String? = null, defaultDestDir: File? = null): ExtractArgs.Immutable =
            Immutable(extractPath ?: defaultExtractPath, filter ?: defaultFilter, destDir ?: defaultDestDir)
}