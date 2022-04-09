package info.spiralframework.formats.common.archives

public data class WadFileEntry(val name: String, val size: Long, val offset: Long)
public data class WadDirectoryEntry(val name: String, val subEntries: Array<WadSubEntry>)
public data class WadSubEntry(val name: String, val isDirectory: Boolean)