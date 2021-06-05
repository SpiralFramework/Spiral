package info.spiralframework.core.common.formats.images

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.base.common.empty
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.img.RgbMatrix
import dev.brella.kornea.img.dr.SHTXImage
import dev.brella.kornea.img.dr.readSHTXImage
import dev.brella.kornea.img.dr.writeSHTXFFImage
import dev.brella.kornea.img.dr.writeSHTXFSImage
import dev.brella.kornea.img.dr.writeSHTXFfImage
import dev.brella.kornea.img.dr.writeSHTXFsImage
import dev.brella.kornea.img.dr.writeSHTXImage
import dev.brella.kornea.img.dr.writeSHTXUnkImage
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.useInputFlowForResult
import dev.brella.kornea.toolkit.common.KorneaTypeChecker
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.defaultEquals
import info.spiralframework.base.common.properties.defaultHashCode
import info.spiralframework.base.common.properties.get
import info.spiralframework.core.common.formats.FormatWriteResponse
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.buildFormatResult

object SHTXFormat : ReadableSpiralFormat<RgbMatrix>, WritableSpiralFormat {
    enum class SHTXType {
        NONE,
        ARGB_PALETTE,
        BGRA_PALETTE,
        ARGB,
        BGRA,

        LITTLE_ENDIAN,
        BIG_ENDIAN;

        companion object: ISpiralProperty.PropertyKey<SHTXType>, KorneaTypeChecker<SHTXType> by KorneaTypeChecker.ClassBased() {
            override val name: String = "SHTX Type"

            override fun hashCode(): Int = defaultHashCode()
            override fun equals(other: Any?): Boolean = defaultEquals(other)
        }
    }

    //TODO: Separate the different "sub formats" into their own objects
    override val name: String = "shtx"
    override val extension: String = "shtx"

    override fun requiredPropertiesForConversionSelection(context: SpiralContext, properties: SpiralProperties?): List<ISpiralProperty.PropertyKey<*>> = listOf(PreferredImageFormat)
    override fun preferredConversionFormat(context: SpiralContext, properties: SpiralProperties?): WritableSpiralFormat? = properties[PreferredImageFormat]

    override suspend fun identify(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<Optional<RgbMatrix>> =
        source.useInputFlowForResult { flow ->
            if (flow.readInt32LE() == SHTXImage.MAGIC_NUMBER) buildFormatResult(Optional.empty(), 0.9)
            else KorneaResult.empty()
        }

    /**
     * Attempts to read the data source as [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<RgbMatrix> =
        source.useInputFlowForResult { flow -> flow.readSHTXImage() }

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean =
        data is RgbMatrix

    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): FormatWriteResponse {
        when (data) {
            is RgbMatrix -> {
                when (writeContext[SHTXType]) {
                    SHTXType.NONE -> flow.writeSHTXUnkImage(data)
                    SHTXType.ARGB_PALETTE -> flow.writeSHTXFsImage(data)
                    SHTXType.BGRA_PALETTE -> flow.writeSHTXFSImage(data)
                    SHTXType.ARGB -> flow.writeSHTXFfImage(data)
                    SHTXType.BGRA -> flow.writeSHTXFFImage(data)
                    SHTXType.LITTLE_ENDIAN -> flow.writeSHTXImage(data, preferBigEndian = false)
                    SHTXType.BIG_ENDIAN -> flow.writeSHTXImage(data, preferBigEndian = true)
                    null -> flow.writeSHTXImage(data)
                }
                return FormatWriteResponse.SUCCESS
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }
    }
}