package info.spiralframework.formats.archives

data class WADSubdirectoryEntry(
        val name: String,
        /** Name to isDirectory */
        val subEntries: Array<Pair<String, Boolean>>
)