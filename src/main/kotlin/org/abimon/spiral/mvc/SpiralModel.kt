package org.abimon.spiral.mvc

import org.abimon.imperator.impl.InstanceOrder
import org.abimon.imperator.impl.InstanceSoldier
import org.abimon.imperator.impl.InstanceWatchtower
import org.abimon.spiral.core.data.ModelConfig
import org.abimon.spiral.core.data.SpiralData
import org.abimon.visi.lang.splitOutsideGroup
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

object SpiralModel {
    val archives: MutableSet<File> = ConcurrentSkipListSet()
    var operating: File? = null
    var scope: Pair<String, String> = "> " to "default"
    var isDebug: Boolean = false

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
        isDebug = config.debug
    }

    val config: ModelConfig
        get() = ModelConfig(archives.map { it.absolutePath }.toSet(), isDebug)

    init { load() }
}