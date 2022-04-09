package info.spiralframework.formats.common.models

public sealed class SrdiMeshType {
    public companion object {
        public const val MAP_UNK_ID: Int = 515
        public const val NORMAL_ID: Int = 516
        public const val MAP_ID: Int = 517
        public const val BONES_ID: Int = 518
        public const val MAP_UNK_2_ID: Int = 773

        public operator fun invoke(id: Int): SrdiMeshType =
            when (id) {
                MAP_UNK_ID -> MapUnknown
                NORMAL_ID -> Normal
                MAP_ID -> Map
                BONES_ID -> Bones
                MAP_UNK_2_ID -> MapUnk2
                else -> Unknown(id)
            }
    }

    public data class Unknown(val id: Int) : SrdiMeshType()
    public object MapUnknown : SrdiMeshType()
    public object Normal : SrdiMeshType()
    public object Map : SrdiMeshType()
    public object Bones : SrdiMeshType()
    public object MapUnk2 : SrdiMeshType()
}