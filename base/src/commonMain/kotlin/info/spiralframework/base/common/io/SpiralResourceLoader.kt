package info.spiralframework.base.common.io

import info.spiralframework.base.common.SpiralModuleBase
import org.abimon.kornea.io.common.DataSource
import kotlin.reflect.KClass

interface SpiralResourceLoader {
    object NoOp: SpiralResourceLoader {
//        override suspend fun hasResource(name: String): Boolean? = false
        override suspend fun loadResource(name: String, from: KClass<*>): DataSource<*>? = null
    }

//    suspend fun hasResource(name: String): Boolean?
    suspend fun loadResource(name: String, from: KClass<*> = SpiralModuleBase::class): DataSource<*>?
}