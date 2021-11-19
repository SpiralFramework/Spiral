package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.filterToInstance
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.EnumSeekMode
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.SeekableInputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.readNullTerminatedUTF8String
import dev.brella.kornea.io.common.flow.extensions.readNumBytes
import dev.brella.kornea.io.common.flow.extensions.writeInt16LE
import dev.brella.kornea.io.common.flow.extensions.writeInt32LE
import dev.brella.kornea.io.common.flow.globalOffset
import dev.brella.kornea.toolkit.common.oneTimeMutableInline
import dev.brella.kornea.toolkit.common.toHexString
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignedTo
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.logging.SpiralLogger.NoOp.trace

@ExperimentalUnsignedTypes
/** ResourceInfoEntry? */
data class RSISrdEntry(
    override val classifier: Int,
    override val mainDataLength: ULong,
    override val subDataLength: ULong,
    override val unknown: Int
) : SrdEntryWithData(classifier, mainDataLength, subDataLength, unknown) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x24525349

        const val NO_RESOURCE_TYPE = 0xA000

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<RSISrdEntry> = BaseSrdEntry(context, dataSource)
            .also { context.trace("'RSI': {0}", it) }
            .filterToInstance()
    }

    sealed class ResourceIndex {
        data class GlobalModelResource(val start: Int, val length: Int, val unk1: Int, val unk2: Int) : ResourceIndex()
        data class GlobalTextureResource(val start: Int, val length: Int, val unk1: Int, val unk2: Int) : ResourceIndex()
        data class LocalLabelledResource(val labelOffset: Int, val start: Int, val length: Int, val unk2: Int) : ResourceIndex()
    }

    data class LocalResource(val label: String, val data: ByteArray)

    var locationKey: String by oneTimeMutableInline()
    var setupFlow: InputFlow by oneTimeMutableInline()

    var unk1: Int by oneTimeMutableInline()
    var unk2: Int by oneTimeMutableInline()
    var unk3: Int by oneTimeMutableInline()
    var resourceCount: Int by oneTimeMutableInline()
    var unk4: Int by oneTimeMutableInline()
    var unk5: Int by oneTimeMutableInline()
    var unk6: Int by oneTimeMutableInline()
    var unk7: Int by oneTimeMutableInline()

    var resources: Array<ResourceIndex> by oneTimeMutableInline()
    var localResources: Map<ResourceIndex, LocalResource> by oneTimeMutableInline()
    var name: String by oneTimeMutableInline()

    override suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<RSISrdEntry> {
        locationKey = flow.location.toString()
        setupFlow = flow
        debug("Setting up RSI entry @ {0}", locationKey)

        flow.seek(0, EnumSeekMode.FROM_BEGINNING)

        debug("[{0}] Reading RSI entry @ {1}", locationKey, flow.globalOffset())

//        if (locationKey.endsWith("+5560h[A0h,120h][10h,65h]")) {
//            println()
//        }

        unk1 = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk2 = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk3 = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        resourceCount = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk4 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk5 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk6 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk7 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        val nameOffset = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        resources = Array(resourceCount) {
            val resourceTypeIndicator = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
            when (val type = resourceTypeIndicator shr 28) {
                0 -> ResourceIndex.LocalLabelledResource(
                    resourceTypeIndicator and 0x0FFFFFFF,
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY),
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY),
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                )
                2 -> ResourceIndex.GlobalModelResource(
                    resourceTypeIndicator and 0x0FFFFFFF,
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY),
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY),
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                )
                4 -> ResourceIndex.GlobalTextureResource(
                    resourceTypeIndicator and 0x0FFFFFFF,
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY),
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY),
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                )
                else -> {
                    trace("formats.rsi_entry.new_resource_type", type.toHexString())
                    return KorneaResult.errorAsIllegalState(NO_RESOURCE_TYPE, localise("formats.rsi_entry.no_resource_type", type.toHexString()))
                }
            }
        }

        flow.seek(nameOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
        name = flow.readNullTerminatedUTF8String()

        localResources = resources.mapNotNull { resource ->
            if (resource !is ResourceIndex.LocalLabelledResource) return@mapNotNull null

            flow.seek(resource.labelOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
            val label = flow.readNullTerminatedUTF8String()

            flow.seek(resource.start.toLong(), EnumSeekMode.FROM_BEGINNING)
            val data = flow.readNumBytes(resource.length)

            Pair(resource, LocalResource(label, data))
        }.toMap()

        return KorneaResult.success(this@RSISrdEntry)
    }

    override suspend fun SpiralContext.writeSubData(out: OutputFlow) {}
    override suspend fun SpiralContext.writeMainData(out: OutputFlow) {
        out.write(unk1)
        out.write(unk2)
        out.write(unk3)
        out.write(resources.size)
        out.writeInt16LE(unk4)
        out.writeInt16LE(unk5)
        out.writeInt16LE(unk6)
        out.writeInt16LE(unk7)

        //Name Offset


        val baseStart = 16 + (resources.size * 16)
        val strings: MutableMap<String, ByteArray> = mutableMapOf(name to name.encodeToByteArray())

        var stringTableOffset = baseStart + resources.sumBy {
            if (it is ResourceIndex.LocalLabelledResource) localResources[it]?.data?.size?.alignedTo(16) ?: 0 else 0
        }

        out.writeInt32LE(stringTableOffset)

        stringTableOffset += strings.getValue(name).size + 1

        resources.forEachIndexed { index, resource ->
            when (resource) {
                is ResourceIndex.LocalLabelledResource -> {
                    localResources[resource]?.let { localResource ->
                        strings[localResource.label] = localResource.label.encodeToByteArray()
                    }

                    var dataOffset = baseStart
                    var labelOffset = stringTableOffset

                    resources.take(index).forEach { prevResource ->
                        if (prevResource is ResourceIndex.LocalLabelledResource) {
                            localResources[prevResource]?.label?.let(strings::get)?.let { labelOffset += it.size + 1 }
                            dataOffset += prevResource.length.alignedTo(16)
                        }
                    }

                    out.writeInt32LE(labelOffset and 0x0FFFFFFF or (0 shl 28))
                    out.writeInt32LE(dataOffset)
                    out.writeInt32LE(resource.length)
                    out.writeInt32LE(resource.unk2)
                }
                is ResourceIndex.GlobalModelResource -> {
                    out.writeInt32LE(resource.start and 0x0FFFFFFF or (2 shl 28))
                    out.writeInt32LE(resource.length)
                    out.writeInt32LE(resource.unk1)
                    out.writeInt32LE(resource.unk2)
                }
                is ResourceIndex.GlobalTextureResource -> {
                    out.writeInt32LE(resource.start and 0x0FFFFFFF or (4 shl 28))
                    out.writeInt32LE(resource.length)
                    out.writeInt32LE(resource.unk1)
                    out.writeInt32LE(resource.unk2)
                }
            }
        }

        resources.forEach { resource ->
            if (resource !is ResourceIndex.LocalLabelledResource) return@forEach
            val localResource = localResources[resource] ?: return@forEach

            out.write(localResource.data)
            out.write(ByteArray(localResource.data.size.alignmentNeededFor(16)))
        }

        out.write(name.encodeToByteArray())
        out.write(0x00)

        resources.forEach { resource ->
            if (resource !is ResourceIndex.LocalLabelledResource) return@forEach
            val localResource = localResources[resource] ?: return@forEach
            val encoded = strings[localResource.label] ?: localResource.label.encodeToByteArray()

            out.write(encoded)
            out.write(0x00)
        }
    }
}

class RsiSrdEntryBuilder {
    val resources: MutableList<RSISrdEntry.ResourceIndex> = ArrayList()
    val localResources: MutableMap<RSISrdEntry.ResourceIndex, RSISrdEntry.LocalResource> = HashMap()

    fun globalTextureResource(start: Int, length: Int, unk1: Int, unk2: Int) =
        resources.add(RSISrdEntry.ResourceIndex.GlobalTextureResource(start, length, unk1, unk2))

    fun localResource(label: String, data: ByteArray, unk2: Int) {
        val index = RSISrdEntry.ResourceIndex.LocalLabelledResource(resources.size, 0, 0, unk2)
        resources.add(index)
        localResources[index] = RSISrdEntry.LocalResource(label, data)
    }
}

@SrdBuilder
inline fun buildRsiEntry(unk1: Int, unk2: Int, unk3: Int, unk4: Int, unk5: Int, unk6: Int, unk7: Int, name: String, block: RsiSrdEntryBuilder.() -> Unit): RSISrdEntry {
    val entry = RSISrdEntry(RSISrdEntry.MAGIC_NUMBER_BE, ULong.MAX_VALUE, ULong.MAX_VALUE, 0)
    entry.unk1 = unk1
    entry.unk2 = unk2
    entry.unk3 = unk3
    entry.unk4 = unk4
    entry.unk5 = unk5
    entry.unk6 = unk6
    entry.unk7 = unk7

    entry.name = name

    val builder = RsiSrdEntryBuilder()
    builder.block()

    entry.resources = builder.resources.toTypedArray()
    entry.localResources = builder.localResources

    return entry
}