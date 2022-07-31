package info.spiralframework.gui.javafx.controls.content

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.extensions.readUInt32LE
import dev.brella.kornea.io.common.flow.fauxSeekFromStart
import dev.brella.kornea.io.common.useInputFlowForResult
import info.spiralframework.core.common.formats.SpiralFormat
import info.spiralframework.core.formats.images.PNGFormat
import info.spiralframework.gui.javafx.config
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane

class ContentPane: BorderPane() {
    var imageView = config(::ImageView)
    var blank = config(::LogoPane)

    public suspend fun load(source: DataSource<*>, hint: SpiralFormat): KorneaResult<SpiralFormat> =
        source.useInputFlowForResult { flow ->
            when (flow.readUInt32LE()) {
                // PNG
                0x89504E47u -> {
                    flow.fauxSeekFromStart(0uL, source, ::loadRawImageView)
                    KorneaResult.success(PNGFormat)
                }
            }

            KorneaResult.empty()
        }

    public suspend fun loadRawImageView(flow: InputFlow) {

    }

    init {
        center = blank { setStartup() }
    }
}