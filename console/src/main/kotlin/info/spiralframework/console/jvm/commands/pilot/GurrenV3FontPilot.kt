package info.spiralframework.console.jvm.commands.pilot

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.modules.functionregistry.registerFunctionWithContextWithoutReturn
import dev.brella.knolus.stringTypeParameter
import dev.brella.kornea.errors.common.getOrBreak
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.pipeline.spiralContext
import java.io.File

object GurrenV3FontPilot : CommandRegistrar {
    suspend fun SpiralContext.createV3Font(knolusContext: KnolusContext, fontName: String, destinationPath: String) {
        val destination = File(destinationPath)

        println("Creating v3_font_$fontName.spc at $destinationPath")
    }


    override suspend fun register(spiralContext: SpiralContext, knolusContext: KnolusContext) {
        with(knolusContext) {
            registerFunctionWithContextWithoutReturn("create_v3_font", stringTypeParameter("font_name"), stringTypeParameter("dest_path")) { context, fontName, destPath ->
                val spiralContext = context.spiralContext().getOrBreak { return@registerFunctionWithContextWithoutReturn }

                spiralContext.createV3Font(knolusContext, fontName, destPath)
            }

            GurrenPilot.help("create_v3_font")
        }
    }
}