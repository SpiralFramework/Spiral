package info.spiralframework.core.formats.audio

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.errorAsLocalisedIllegalState
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.SpiralFormatOptionalResult
import info.spiralframework.core.common.formats.SpiralFormatReturnResult
import info.spiralframework.core.common.formats.WritableSpiralFormat
import java.io.File

public open class SpiralAudioFormat(override val name: String, override val extension: String) :
    ReadableSpiralFormat<File>, WritableSpiralFormat<Unit> {
    public open val needsMediaPlugin: Boolean = true

    override suspend fun identify(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatOptionalResult<File> =
        context.errorAsLocalisedIllegalState(-1, "core.formats.no_audio_impl.identify", this)

    override suspend fun read(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatReturnResult<File> =
        context.errorAsLocalisedIllegalState(-1, "core.formats.no_audio_impl.read", this)

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean =
        false
//        throw IllegalStateException(context.localise("core.formats.no_audio_impl.support_write", this))

    override suspend fun write(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        data: Any,
        flow: OutputFlow
    ): KorneaResult<Unit> =
        context.errorAsLocalisedIllegalState(-1, "core.formats.no_audio_impl.write", this)
}