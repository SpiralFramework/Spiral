package org.abimon.spiral.core.objects.archives

data class WADSubdirectoryEntry(
        val name: String,
        /** Name to isDirectory */
        val subEntries: Array<Pair<String, Boolean>>
)