package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.EnumSeekMode
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.*
import dev.brella.kornea.toolkit.common.oneTimeMutableInline
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.text.lazyString

public data class TRESrdEntry(
    override val classifier: Int,
    override val mainDataLength: ULong,
    override val subDataLength: ULong,
    override val unknown: Int
) : SrdEntryWithData(classifier, mainDataLength, subDataLength, unknown) {
    public sealed class TreeNode {
        public data class Branch internal constructor(
            val stringOffset: UInt,
            val leafValueOffset: UInt,
            val leafCount: Int,
            val nodeDepth: Int,
            val unknown0A: Int,
            val unknown0B: Int,
            val unknown0C: UInt,
            override val string: String,
            val children: MutableList<TreeNode>
        ) : TreeNode() {
            public companion object {
                internal suspend inline operator fun invoke(
                    flow: SeekableInputFlow,
                    stringOffset: UInt,
                    leafValueOffsets: UInt,
                    leafCount: Int,
                    nodeDepth: Int,
                    unknown0A: Int,
                    unknown0B: Int,
                    unknown0C: UInt
                ): Branch =
                    Branch(
                        stringOffset,
                        leafValueOffsets,
                        leafCount,
                        nodeDepth,
                        unknown0A,
                        unknown0B,
                        unknown0C,
                        bookmark(flow) {
                            flow.seek(stringOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
                            flow.readNullTerminatedUTF8String()
                        },
                        if (leafValueOffsets > 0u) {
                            bookmark(flow) {
                                flow.seek(leafValueOffsets.toLong(), EnumSeekMode.FROM_BEGINNING)
                                ArrayList<TreeNode>(leafCount).apply {
                                    repeat(leafCount) {
                                        val endpointStringOffset = flow.readUInt32LE()!!
                                        val unk = flow.readUInt32LE()!!

                                        val endpointString = bookmark(flow) {
                                            flow.seek(endpointStringOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
                                            flow.readNullTerminatedUTF8String()
                                        }

                                        add(Leaf(endpointStringOffset, unk, endpointString))
                                    }
                                }
                            }
                        } else {
                            mutableListOf()
                        }
                    )
            }

            public infix fun descend(depth: Int): Branch {
                var node = this
                repeat(depth - 1) {
                    //TODO: Make sure this is right
                    node = node.children.firstOrNull() as? Branch ?: return node
                }

                return node
            }

            public infix fun descend(path: List<String>): Branch {
                var node = this
                path.forEachIndexed { index, seg ->
                    node = node.children.firstOrNull { node -> node.string.split('|').drop(2).getOrNull(index) == seg } as? Branch ?: return node
                }

                return node
            }

            public infix fun add(child: TreeNode) {
                child.parent = this
                children.add(child)
            }

            @Suppress("UNCHECKED_CAST")
            public fun leaves(): List<Leaf> =
                if (children.isEmpty()) emptyList()
                else if (children.all { node -> node is Leaf }) children as List<Leaf>
                else children.flatMap { node ->
                    when (node) {
                        is Branch -> node.leaves()
                        is Leaf -> listOf(node)
                    }
                }

            init {
                children.forEach { it.parent = this }
            }
        }

        public data class Leaf(val stringOffset: UInt, val unk: UInt, override val string: String) : TreeNode()

        public var parent: Branch? = null
        public abstract val string: String
    }

    public companion object {
        public const val MAGIC_NUMBER_BE: Int = 0x24545245
    }

    var maxTreeDepth: UInt by oneTimeMutableInline()
    var unk14: Int by oneTimeMutableInline()
    var totalEntryCount: Int by oneTimeMutableInline()
    var unk18: Int by oneTimeMutableInline()
    var totalLeafCount: Int by oneTimeMutableInline()
    var unknownFloatListOffset: UInt by oneTimeMutableInline()

    var tree: TreeNode.Branch by oneTimeMutableInline()

    override suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<TRESrdEntry> {
        flow.seek(0, EnumSeekMode.FROM_BEGINNING)

        maxTreeDepth = flow.readUInt32LE()!!
        unk14 = flow.readUInt16LE()!!
        totalEntryCount = flow.readUInt16LE()!!

        unk18 = flow.readUInt16LE()!!
        totalLeafCount = flow.readUInt16LE()!!
        unknownFloatListOffset = flow.readUInt32LE()!!

        val treeEntries = Array(totalEntryCount) {
            TreeNode.Branch(
                flow = flow,
                stringOffset = flow.readUInt32LE()!!,
                leafValueOffsets = flow.readUInt32LE()!!,
                leafCount = flow.read()!!,
                nodeDepth = flow.read()!!,
                unknown0A = flow.read()!!,
                unknown0B = flow.read()!!,
                unknown0C = flow.readUInt32LE()!!
            )
        }.toMutableList()

        tree = treeEntries.firstOrNull { node -> node.nodeDepth == 0 } ?: return KorneaResult.empty()
        treeEntries.remove(tree)

        treeEntries.groupBy(TreeNode.Branch::nodeDepth)
            .mapValues { (_, values) -> values.sortedBy(TreeNode.Branch::string) }
            .entries
            .sortedBy(Map.Entry<Int, List<TreeNode.Branch>>::key)
            .forEach { (_, branches) ->
                branches.forEach { branch ->
                    trace("Descending into {0} -> {1} ({2} -> {3})", tree.string, lazyString { branch.string.split('|').drop(2).take(branch.nodeDepth - 1) }, branch.string, branch.nodeDepth)
                    (tree descend branch.string.split('|').drop(2).take(branch.nodeDepth - 1)).add(branch)
                }
            }

        return KorneaResult.success(this@TRESrdEntry)
    }

    override suspend fun SpiralContext.writeMainData(out: OutputFlow) {
        TODO("Not yet implemented")
    }

    override suspend fun SpiralContext.writeSubData(out: OutputFlow) {
        TODO("Not yet implemented")
    }
}

public fun TRESrdEntry.TreeNode.Branch.traverse(list: MutableList<TRESrdEntry.TreeNode> = ArrayList()): List<TRESrdEntry.TreeNode> {
    list.add(this)

    children.forEach { node ->
        when (node) {
            is TRESrdEntry.TreeNode.Branch -> node.traverse(list)
            is TRESrdEntry.TreeNode.Leaf -> list.add(node)
        }
    }

    return list
}