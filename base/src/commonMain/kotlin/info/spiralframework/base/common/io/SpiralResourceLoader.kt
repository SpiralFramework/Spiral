package info.spiralframework.base.common.io

import info.spiralframework.base.common.SpiralModuleBase
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.korneaNotFound
import dev.brella.kornea.io.common.DataSource
import kotlin.reflect.KClass

interface SpiralResourceLoader {
    object NoOp: SpiralResourceLoader {
//        override suspend fun hasResource(name: String): Boolean? = false
        override suspend fun loadResource(name: String, from: KClass<*>): KorneaResult<DataSource<*>> = korneaNotFound("Could not find $name (NoOp Resource Loader)")
    }

//    suspend fun hasResource(name: String): Boolean?
    suspend fun loadResource(name: String, from: KClass<*> = SpiralModuleBase::class): KorneaResult<DataSource<*>>
//    suspend fun findResources(name: String, from: KClass<*> = SpiralModuleBase::class):
}