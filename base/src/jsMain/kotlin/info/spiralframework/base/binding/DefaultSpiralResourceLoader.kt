package info.spiralframework.base.binding

import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.js.io.AjaxDataSource
import info.spiralframework.base.js.urlExists
import kotlin.reflect.KClass

actual class DefaultSpiralResourceLoader(val root: String) : SpiralResourceLoader {
    actual constructor(): this("")

    override suspend fun loadResource(name: String, from: KClass<*>): DataSource<*>? {
        //TODO: Fix a possible scenario where HEAD requests return a 404
        if (urlExists("$root/$name")) {
            return AjaxDataSource("$root/$name")
        } else {
            return null
        }
    }
}