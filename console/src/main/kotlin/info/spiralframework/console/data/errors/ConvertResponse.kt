package info.spiralframework.console.data.errors

import java.io.File

data class ConvertResponse(val file: File, val from: String?, val to: String?, val error: String? = null)