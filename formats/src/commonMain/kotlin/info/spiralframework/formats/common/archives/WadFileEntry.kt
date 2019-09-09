package info.spiralframework.formats.common.archives

data class WadFileEntry(val name: String, val size: Long, val offset: Long)
data class WadDirectoryEntry(val name: String, val subEntries: Array<WadSubEntry>)
data class WadSubEntry(val name: String, val isDirectory: Boolean)