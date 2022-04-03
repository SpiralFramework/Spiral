package info.spiralframework.base.binding

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.korneaNotFound
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.js.AjaxDataSource
import dev.brella.kornea.io.js.urlExists
import info.spiralframework.base.common.io.SpiralResourceLoader
import kotlin.reflect.KClass

public actual class DefaultSpiralResourceLoader(public val root: String) : SpiralResourceLoader {
    public actual constructor(): this("")

    override suspend fun loadResource(name: String, from: KClass<*>): KorneaResult<DataSource<*>> {
        //TODO: Fix a possible scenario where HEAD requests return a 404
        return if (urlExists("$root/$name")) {
            KorneaResult.success(AjaxDataSource("$root/$name"))
        } else {
            korneaNotFound("Url '$root/$name' does not exist")
        }
    }
}