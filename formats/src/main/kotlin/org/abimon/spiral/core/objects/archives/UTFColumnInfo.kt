package org.abimon.spiral.core.objects.archives

data class UTFColumnInfo(
        var type: Int = -1,
        var columnName: String = "",
        var constantOffset: Long = -1
)