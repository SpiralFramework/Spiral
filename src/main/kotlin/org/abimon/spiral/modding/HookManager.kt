package org.abimon.spiral.modding

import org.abimon.spiral.mvc.SpiralModel

object HookManager {
    private val BEFORE_SCOPE_CHANGE: MutableList<Pair<IPlugin, (Pair<String, String>, Pair<String, String>, Boolean) -> Boolean>> = ArrayList()
    private val ON_SCOPE_CHANGE: MutableList<Pair<IPlugin, (Pair<String, String>, Pair<String, String>) -> Boolean>> = ArrayList()

    fun registerBeforeScopeChange(plugin: IPlugin, hook: (Pair<String, String>, Pair<String, String>, Boolean) -> Boolean) {
        BEFORE_SCOPE_CHANGE.add(plugin to hook)
    }

    fun deregisterScopeChange(plugin: IPlugin, hook: (Pair<String, String>, Pair<String, String>, Boolean) -> Boolean) {
        BEFORE_SCOPE_CHANGE.remove(plugin to hook)
    }

    fun registerScopeChange(plugin: IPlugin, hook: (Pair<String, String>, Pair<String, String>) -> Boolean) {
        ON_SCOPE_CHANGE.add(plugin to hook)
    }

    fun deregisterScopeChange(plugin: IPlugin, hook: (Pair<String, String>, Pair<String, String>) -> Boolean) {
        ON_SCOPE_CHANGE.remove(plugin to hook)
    }

    fun changeScope(new: Pair<String, String>): Boolean {
        val current = SpiralModel.scope

        val result = BEFORE_SCOPE_CHANGE
                .filter { (plugin) -> PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
                .fold(false) { state, (_, hook) -> hook(current, new, state) }

        if(!result) {
            SpiralModel.scope = new
            ON_SCOPE_CHANGE
                    .filter { (plugin) -> PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
                    .forEach { (_, hook) -> hook(current, new) }
        }

        return result
    }
}