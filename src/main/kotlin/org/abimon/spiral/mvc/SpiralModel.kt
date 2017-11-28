package org.abimon.spiral.mvc

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.abimon.imperator.handle.Imperator
import org.abimon.imperator.impl.BasicImperator
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.imperator.impl.InstanceSoldier
import org.abimon.imperator.impl.InstanceWatchtower
import org.abimon.spiral.core.data.ModelConfig
import org.abimon.spiral.core.data.PatchOperation
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.modding.HookManager
import org.abimon.spiral.util.LoggerLevel
import org.abimon.visi.lang.splitOutsideGroup
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object SpiralModel {
    val imperator: Imperator = BasicImperator()

    val archives: MutableSet<File> = ConcurrentSkipListSet()
    var operating: File? by hookable(null, HookManager::beforeOperatingChange, HookManager::afterOperatingChange)
    var scope: Pair<String, String> by hookable("> " to "default", HookManager::beforeScopeChange, HookManager::afterScopeChange)
    var loggerLevel: LoggerLevel by hookable(LoggerLevel.NONE, HookManager::beforeLoggerLevelChange, HookManager::afterLoggerLevelChange)
    var cacheEnabled: Boolean by hookable(true, HookManager::beforeCacheEnabledChange, HookManager::afterCacheEnabledChange)
    var concurrentOperations: Int by hookable(4, HookManager::beforeConcurrentOperationsChange, HookManager::afterConcurrentOperationsChange)
    var autoConfirm: Boolean by hookable(false, HookManager::beforeAutoConfirmChange, HookManager::afterAutoConfirmChange)
    var purgeCache: Boolean by hookable(true, HookManager::beforePurgeCacheChange, HookManager::afterPurgeCacheChange)

    var patchOperation: PatchOperation? by hookable(null, HookManager::beforePatchOperationChange, HookManager::afterPatchOperationChange)
    var patchFile: File? by hookable(null, HookManager::beforePatchFileChange, HookManager::afterPatchFileChange)

    var attemptFingerprinting: Boolean by hookable(true, HookManager::beforeAttemptFingerprintChange, HookManager::afterAttemptFingerprintChange)

    val defaultParams: MutableMap<String, Any?> = HashMap()

    private val pluginData: MutableMap<String, Any?> = HashMap()
    private val unsafe: AtomicBoolean = AtomicBoolean(false)

    fun Command(commandName: String, scope: String? = null, command: (Pair<Array<String>, String>) -> Unit): InstanceSoldier<InstanceOrder<*>> {
        return InstanceSoldier<InstanceOrder<*>>(InstanceOrder::class.java, commandName, arrayListOf(InstanceWatchtower<InstanceOrder<*>> {
            return@InstanceWatchtower (scope == null || SpiralModel.scope.second == scope) &&
                    it.data is String &&
                    ((it.data as String).splitOutsideGroup().firstOrNull() ?: "") == commandName
        })) { command((it.data as String).splitOutsideGroup() to it.data as String) }
    }

    private val JSON_CONFIG = File("config.json")
    private val YAML_CONFIG = File("config.yaml")

    fun save() {
        if(JSON_CONFIG.exists())
            SpiralData.MAPPER.writeValue(JSON_CONFIG, config)
        else
            SpiralData.YAML_MAPPER.writeValue(YAML_CONFIG, config)
    }

    fun load() {
        unsafeChange {
            val config: ModelConfig

            if (JSON_CONFIG.exists())
                config = SpiralData.MAPPER.readValue(JSON_CONFIG, ModelConfig::class.java)
            else if (YAML_CONFIG.exists())
                config = SpiralData.YAML_MAPPER.readValue(YAML_CONFIG, ModelConfig::class.java)
            else
                return@unsafeChange

            archives.clear()
            archives.addAll(config.archives.map { File(it) })
            loggerLevel = config.loggerLevel
            concurrentOperations = config.concurrentOperations
            scope = config.scope
            operating = if (config.operating == null) null else File(config.operating)
            autoConfirm = config.autoConfirm
            purgeCache = config.purgeCache
            patchOperation = config.patchOperation
            patchFile = config.patchFile

            attemptFingerprinting = config.attemptFingerprinting

            defaultParams.clear()
            defaultParams.putAll(config.defaultParams)

            pluginData.clear()
            pluginData.putAll(config.pluginData)

            if (config.debug != null)
                loggerLevel = LoggerLevel.DEBUG
        }
    }

    suspend fun <T> distribute(list: List<T>, operation: (T) -> Unit) {
        val coroutines: MutableList<Job> = ArrayList()
        coroutines.addAll(list.map { t ->
            val job = launch(CommonPool, CoroutineStart.LAZY) { operation(t) }
            job.invokeOnCompletion comp@ { coroutines.forEach { if (it.start()) return@comp } }

            return@map job
        })

        for(i in 0 until minOf(SpiralModel.concurrentOperations, coroutines.size))
            coroutines[i].start()

        coroutines.chunked(SpiralModel.concurrentOperations).forEach { sublist -> sublist.forEach { it.join() } }
    }

    val config: ModelConfig
        get() = ModelConfig(
                archives.map { it.absolutePath }.toSet(), loggerLevel, null, concurrentOperations, scope, operating?.absolutePath, autoConfirm, purgeCache,
                patchOperation, patchFile,
                attemptFingerprinting, defaultParams, pluginData
        )

    init { load() }

    fun <T> saveDelegate(initial: T): ReadWriteProperty<Any?, T> = Delegates.observable(initial) { _, _, _ -> if(!unsafe.get()) save() }
    fun <T> hookable(initialValue: T, beforeChange: (T, T) -> Boolean, afterChange: (T, T) -> Unit):
            ReadWriteProperty<Any?, T> = object : ObservableProperty<T>(initialValue) {
        override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean = beforeChange(oldValue, newValue)
        override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
            afterChange(oldValue, newValue)

            if(!SpiralModel.unsafe.get())
                SpiralModel.save()
        }
    }

    fun confirm(question: () -> Boolean): Boolean = autoConfirm || question()

    fun unsafeChange(op: () -> Unit) {
        try {
            unsafe.set(true)
            op()
        } finally {
            unsafe.set(false)
        }
    }

    fun getPluginData(uid: String): Any? = pluginData[uid]
    fun putPluginData(uid: String, data: Any?) {
        pluginData[uid] = data
        save()
    }
}