package org.abimon.spiral.mvc

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

    fun Command(commandName: String, scope: String? = null, command: (Pair<Array<String>, String>) -> Unit): InstanceSoldier<InstanceOrder<*>> {
        return InstanceSoldier<InstanceOrder<*>>(InstanceOrder::class.java, commandName, arrayListOf(InstanceWatchtower<InstanceOrder<*>> {
            return@InstanceWatchtower (scope == null || SpiralModel.scope.second == scope) &&
                    it is InstanceOrder<*> &&
                    it.data is String &&
                    ((it.data as String).splitOutsideGroup().firstOrNull() ?: "") == commandName
        })) { command((it.data as String).splitOutsideGroup() to it.data as String) }
    }
}