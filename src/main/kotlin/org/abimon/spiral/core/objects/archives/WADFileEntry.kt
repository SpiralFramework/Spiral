package org.abimon.spiral.core.objects.archives

data class WADFileEntry(
        val name: String,
        val size: Long,
        val offset: Long
)