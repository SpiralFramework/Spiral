package info.spiralframework.core.formats.audio

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.base.common.empty
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.getOrDefault
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.useInputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.SpiralFormatOptionalResult
import info.spiralframework.core.common.formats.buildFormatSuccess
import java.io.File

public object AudioFormats {
    public const val WAV_MAGIC_NUMBER: Int = 0x46464952
    public const val OGG_MAGIC_NUMBER: Int = 0x5367674F
    public const val ID3_MAGIC_NUMBER: Int = 0x4334449

    public val DEFAULT_WAV: SpiralAudioFormat = object : SpiralAudioFormat("wav", "wav") {
        override suspend fun identify(
            context: SpiralContext,
            readContext: SpiralProperties?,
            source: DataSource<*>
        ): SpiralFormatOptionalResult<File> {
            if (source.useInputFlow { flow -> flow.readInt32LE() == WAV_MAGIC_NUMBER }.getOrDefault(false)) {
                return buildFormatSuccess(Optional.empty(), 1.0)
            }

            return KorneaResult.empty()
        }
    }
    public val DEFAULT_OGG: SpiralAudioFormat = object : SpiralAudioFormat("ogg", "ogg") {
        override suspend fun identify(
            context: SpiralContext,
            readContext: SpiralProperties?,
            source: DataSource<*>
        ): SpiralFormatOptionalResult<File> {
            if (source.useInputFlow { flow -> flow.readInt32LE() == OGG_MAGIC_NUMBER }.getOrDefault(false)) {
                return buildFormatSuccess(Optional.empty(), 1.0)
            }

            return KorneaResult.empty()
        }
    }
    public val DEFAULT_MP3: SpiralAudioFormat = object : SpiralAudioFormat("mp3", "mp3") {
        override suspend fun identify(
            context: SpiralContext,
            readContext: SpiralProperties?,
            source: DataSource<*>
        ): SpiralFormatOptionalResult<File> {
            if (source.useInputFlow { flow -> flow.readInt32LE() == ID3_MAGIC_NUMBER }.getOrDefault(false)) {
                return buildFormatSuccess(Optional.empty(), 0.8)
            }

            return KorneaResult.empty()
        }
    }

    public var wav: SpiralAudioFormat = DEFAULT_WAV
    public var ogg: SpiralAudioFormat = DEFAULT_OGG
    public var mp3: SpiralAudioFormat = DEFAULT_MP3
}