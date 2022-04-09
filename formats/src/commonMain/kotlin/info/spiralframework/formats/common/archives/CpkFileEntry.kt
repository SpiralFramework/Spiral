package info.spiralframework.formats.common.archives

public data class CpkFileEntry(val fileName: String, val directoryName: String, val fileSize: Int, val extractSize: Int, val fileOffset: Long) {
    val name: String = "$directoryName/$fileName"
    val isCompressed: Boolean = extractSize != fileSize //Since technically you can have a *larger* crilayla file...
}