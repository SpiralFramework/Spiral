package info.spiralframework.base.binding

import info.spiralframework.base.common.io.SpiralResourceLoader
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.korneaNotFound
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.js.AjaxDataSource
import org.abimon.kornea.io.js.urlExists
import kotlin.reflect.KClass

actual class DefaultSpiralResourceLoader(val root: String) : SpiralResourceLoader {
    actual constructor(): this("")

    override suspend fun loadResource(name: String, from: KClass<*>): KorneaResult<DataSource<*>> {
        //TODO: Fix a possible scenario where HEAD requests return a 404
        if (urlExists("$root/$name")) {
            return KorneaResult.success(AjaxDataSource("$root/$name"))
        } else {
            return korneaNotFound("Url '$root/$name' does not exist")
        }
    }
}