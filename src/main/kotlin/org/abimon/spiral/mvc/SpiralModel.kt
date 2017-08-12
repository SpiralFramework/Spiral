package org.abimon.spiral.mvc

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.imperator.impl.InstanceSoldier
import org.abimon.imperator.impl.InstanceWatchtower
import org.abimon.visi.lang.splitOutsideGroup
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

object SpiralModel {
    val wads: MutableSet<File> = ConcurrentSkipListSet()
    var operating: File? = null
    var scope: Pair<String, String> = "> " to "default"

    val MAPPER: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModules(Jdk8Module(), JavaTimeModule(), ParameterNamesModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

    fun Command(commandName: String, scope: String? = null, command: (Pair<Array<String>, String>) -> Unit): InstanceSoldier<InstanceOrder<*>> {
        return InstanceSoldier<InstanceOrder<*>>(InstanceOrder::class.java, commandName, arrayListOf(InstanceWatchtower<InstanceOrder<*>> {
            return@InstanceWatchtower (scope == null || SpiralModel.scope.second == scope) &&
                    it is InstanceOrder<*> &&
                    it.data is String &&
                    ((it.data as String).splitOutsideGroup().firstOrNull() ?: "") == commandName
        })) { command((it.data as String).splitOutsideGroup() to it.data as String) }
    }
}