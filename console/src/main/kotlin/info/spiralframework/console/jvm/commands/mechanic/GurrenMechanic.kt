package info.spiralframework.console.jvm.commands.mechanic

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.modules.functionregistry.registerFunctionWithContextWithoutReturn
import dev.brella.kornea.errors.common.doOnSuccessAsync
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.formats.archives.*
import java.text.DecimalFormat

@Suppress("unused")
@ExperimentalUnsignedTypes
object GurrenMechanic : CommandRegistrar {
    val COMPILABLE_ARCHIVES = arrayOf<WritableSpiralFormat>(
            CpkArchiveFormat, PakArchiveFormat, SpcArchiveFormat,
            WadArchiveFormat, ZipFormat
    )

    val PERCENT_FORMAT = DecimalFormat("00.00")

    override suspend fun register(spiralContext: SpiralContext, knolusContext: KnolusContext) {
        knolusContext.registerFunctionWithContextWithoutReturn("show_environment") { context ->
            context.spiralContext().doOnSuccessAsync(GurrenShared::showEnvironment)
        }
    }
}