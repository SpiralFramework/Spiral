package info.spiralframework.core.formats.audio

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.base.common.empty
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.getOrElse
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.useInputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.ensureFormatSuccess
import java.io.File

object AudioFormats {
    const val WAV_MAGIC_NUMBER = 0x46464952
    const val OGG_MAGIC_NUMBER = 0x5367674F
    const val ID3_MAGIC_NUMBER = 0x4334449

    val DEFAULT_WAV = object: SpiralAudioFormat("wav", "wav") {
        override suspend fun identify(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<Optional<File>> {
            if (source.useInputFlow { flow -> flow.readInt32LE() == WAV_MAGIC_NUMBER }.getOrElse(false)) {
                return ensureFormatSuccess(Optional.empty(), 1.0)
            }

            return KorneaResult.empty()
        }
    }
    val DEFAULT_OGG = object: SpiralAudioFormat("ogg", "ogg") {
        override suspend fun identify(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<Optional<File>> {
            if (source.useInputFlow { flow -> flow.readInt32LE() == OGG_MAGIC_NUMBER }.getOrElse(false)) {
                return ensureFormatSuccess(Optional.empty(), 1.0)
            }

            return KorneaResult.empty()
        }
    }
    val DEFAULT_MP3 = object: SpiralAudioFormat("mp3", "mp3") {
        override suspend fun identify(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<Optional<File>> {
            if (source.useInputFlow { flow -> flow.readInt32LE() == ID3_MAGIC_NUMBER }.getOrElse(false)) {
                return ensureFormatSuccess(Optional.empty(), 0.8)
            }

            return KorneaResult.empty()
        }
    }

    var wav = DEFAULT_WAV
    var ogg = DEFAULT_OGG
    var mp3 = DEFAULT_MP3
}