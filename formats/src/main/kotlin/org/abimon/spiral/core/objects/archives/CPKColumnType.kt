package org.abimon.spiral.core.objects.archives

enum class CPKColumnType(val mask: Int) {
    TYPE_DATA(0x0B),
    TYPE_STRING(0x0A),
    TYPE_FLOAT(0x08),
    TYPE_8BYTE(0x06),
    TYPE_4BYTE2(0x05),
    TYPE_4BYTE(0x04),
    TYPE_2BYTE2(0x03),
    TYPE_2BYTE(0x02),
    TYPE_1BYTE2(0x01),
    TYPE_1BYTE(0x00),
    TYPE_UNKNOWN(-1);

    companion object {
        fun getForMask(type: Int): CPKColumnType = values().firstOrNull { column -> type and CPK.COLUMN_TYPE_MASK == column.mask } ?: TYPE_UNKNOWN
    }
}