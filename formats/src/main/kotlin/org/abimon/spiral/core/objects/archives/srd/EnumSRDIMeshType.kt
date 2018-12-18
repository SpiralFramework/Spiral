package org.abimon.spiral.core.objects.archives.srd

enum class EnumSRDIMeshType(val id: Int) {
    UNKNOWN(-1),
    MAP_UNK(515),
    NORMAL(516),
    MAP(517),
    BONES(518),
    MAP_UNK_2(773);

    companion object {
        fun meshTypeForID(id: Int): EnumSRDIMeshType = values().firstOrNull { mesh -> mesh.id == id } ?: UNKNOWN
    }
}