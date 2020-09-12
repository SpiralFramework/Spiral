package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.environment.DynamicEnvironmentFunction
import info.spiralframework.base.common.environment.SpiralEnvironment

actual class DefaultSpiralEnvironment actual constructor() : SpiralEnvironment {
    override suspend fun SpiralContext.retrieveEnvironment(): Map<String, String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun storeStaticValue(key: String, value: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun storeDynamicValue(key: String, value: DynamicEnvironmentFunction) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun storeEnvironmentalValue(key: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun retrieveStaticValue(key: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun retrieveEnvironmentalValue(key: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun SpiralContext.retrieveDynamicValue(key: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun addModuleProvider(moduleProvider: SpiralModuleProvider) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ExperimentalUnsignedTypes
    override suspend fun SpiralContext.registerAllModules() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}