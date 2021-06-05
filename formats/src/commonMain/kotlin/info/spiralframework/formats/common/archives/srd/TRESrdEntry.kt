package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.EnumSeekMode
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.*
import dev.brella.kornea.toolkit.common.oneTimeMutableInline
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.text.lazyString

@ExperimentalUnsignedTypes
data class TRESrdEntry(
    override val classifier: Int,
    override val mainDataLength: ULong,
    override val subDataLength: ULong,
    override val unknown: Int,
    override val dataSource: DataSource<*>
) : SrdEntryWithData(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    sealed class TreeNode {
        data class Branch internal constructor(
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
            companion object {
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

            infix fun descend(depth: Int): Branch {
                var node = this
                repeat(depth - 1) {
                    //TODO: Make sure this is right
                    node = node.children.firstOrNull() as? Branch ?: return node
                }

                return node
            }

            infix fun descend(path: List<String>): Branch {
                var node = this
                path.forEachIndexed { index, seg ->
                    node = node.children.firstOrNull { node -> node.string.split('|').drop(2).getOrNull(index) == seg } as? Branch ?: return node
                }

                return node
            }

            infix fun add(child: TreeNode) {
                child.parent = this
                children.add(child)
            }

            @Suppress("UNCHECKED_CAST")
            fun leaves(): List<Leaf> =
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

        data class Leaf(val stringOffset: UInt, val unk: UInt, override val string: String) : TreeNode()

        var parent: Branch? = null
        abstract val string: String
    }

    companion object {
        const val MAGIC_NUMBER_BE = 0x24545245
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
            .forEach { (depth, branches) ->
                branches.forEach { branch ->
                    trace("Descending into {0} -> {1} ({2} -> {3})", tree.string, lazyString { branch.string.split('|').drop(2).take(branch.nodeDepth - 1) }, branch.string, branch.nodeDepth)
                    (tree descend branch.string.split('|').drop(2).take(branch.nodeDepth - 1)).add(branch)
                }
            }

        return KorneaResult.success(this@TRESrdEntry)
    }
}

fun TRESrdEntry.TreeNode.Branch.traverse(list: MutableList<TRESrdEntry.TreeNode> = ArrayList()): List<TRESrdEntry.TreeNode> {
    list.add(this)

    children.forEach { node ->
        when (node) {
            is TRESrdEntry.TreeNode.Branch -> node.traverse(list)
            is TRESrdEntry.TreeNode.Leaf -> list.add(node)
        }
    }

    return list
}