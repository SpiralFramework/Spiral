package info.spiralframework.formats.common.models

sealed class SrdiMeshType {
    companion object {
        const val MAP_UNK_ID = 515
        const val NORMAL_ID = 516
        const val MAP_ID = 517
        const val BONES_ID = 518
        const val MAP_UNK_2_ID = 773

        operator fun invoke(id: Int): SrdiMeshType {
            when (id) {
                MAP_UNK_ID -> return MapUnknown
                NORMAL_ID -> return Normal
                MAP_ID -> return Map
                BONES_ID -> return Bones
                MAP_UNK_2_ID -> return MapUnk2
                else -> return Unknown(id)
            }
        }
    }

    data class Unknown(val id: Int): SrdiMeshType()
    object MapUnknown: SrdiMeshType()
    object Normal: SrdiMeshType()
    object Map: SrdiMeshType()
    object Bones: SrdiMeshType()
    object MapUnk2: SrdiMeshType()
}