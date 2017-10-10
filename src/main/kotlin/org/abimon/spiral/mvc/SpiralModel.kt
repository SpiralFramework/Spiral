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
import org.abimon.spiral.util.LoggerLevel
import org.abimon.visi.lang.splitOutsideGroup
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty

object SpiralModel {
    val imperator: Imperator = BasicImperator()

    val archives: MutableSet<File> = ConcurrentSkipListSet()
    var operating: File? by saveDelegate(null)
    var scope: Pair<String, String> by saveDelegate("> " to "default")
    var loggerLevel: LoggerLevel by saveDelegate(LoggerLevel.NONE)
    var cacheEnabled: Boolean by saveDelegate(true)
    var concurrentOperations: Int by saveDelegate(4)
    var autoConfirm: Boolean by saveDelegate(false)
    var purgeCache: Boolean by saveDelegate(true)

    var patchOperation: PatchOperation? by saveDelegate(null)
    var patchFile: File? by saveDelegate(null)

    private val pluginData: MutableMap<String, Any?> = HashMap()

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
        val config: ModelConfig

        if(JSON_CONFIG.exists())
            config = SpiralData.MAPPER.readValue(JSON_CONFIG, ModelConfig::class.java)
        else if(YAML_CONFIG.exists())
            config = SpiralData.YAML_MAPPER.readValue(YAML_CONFIG, ModelConfig::class.java)
        else
            return

        archives.clear()
        archives.addAll(config.archives.map { File(it) })
        loggerLevel = config.loggerLevel
        concurrentOperations = config.concurrentOperations
        scope = config.scope
        operating = if(config.operating == null) null else File(config.operating)
        autoConfirm = config.autoConfirm
        purgeCache = config.purgeCache
        patchOperation = config.patchOperation
        patchFile = config.patchFile

        pluginData.clear()
        pluginData.putAll(config.pluginData)

        if(config.debug != null)
            loggerLevel = LoggerLevel.DEBUG
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
                pluginData
        )

    init { load() }

    fun <T> saveDelegate(initial: T): ReadWriteProperty<Any?, T> = Delegates.observable(initial) { _, _, _ -> save() }

    fun confirm(question: () -> Boolean): Boolean = autoConfirm || question()

    fun getPluginData(uid: String): Any? = pluginData[uid]
    fun putPluginData(uid: String, data: Any?) {
        pluginData[uid] = data
        save()
    }
}