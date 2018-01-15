package org.abimon.spiral.core.objects.archives

data class CPKFileEntry(val fileName: String, val directoryName: String, val fileSize: Long, val extractSize: Long, val offset: Long, val isCompressed: Boolean)