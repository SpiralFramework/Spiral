package org.abimon.spiral.core.data

data class UTFColumnInfo(
        var type: Int = -1,
        var columnName: String = "",
        var constantOffset: Long = -1
)