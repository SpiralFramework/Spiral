package org.abimon.spiral.modding

object HookManager {
    private val BEFORE_SCOPE_CHANGE: MutableList<Pair<IPlugin, (Pair<String, String>, Pair<String, String>, Boolean) -> Boolean>> = ArrayList()
    private val ON_SCOPE_CHANGE: MutableList<Pair<IPlugin, (Pair<String, String>, Pair<String, String>) -> Unit>> = ArrayList()

    fun registerBeforeScopeChange(plugin: IPlugin, hook: (Pair<String, String>, Pair<String, String>, Boolean) -> Boolean) {
        BEFORE_SCOPE_CHANGE.add(plugin to hook)
    }

    fun deregisterScopeChange(plugin: IPlugin, hook: (Pair<String, String>, Pair<String, String>, Boolean) -> Boolean) {
        BEFORE_SCOPE_CHANGE.remove(plugin to hook)
    }

    fun registerScopeChange(plugin: IPlugin, hook: (Pair<String, String>, Pair<String, String>) -> Unit) {
        ON_SCOPE_CHANGE.add(plugin to hook)
    }

    fun deregisterScopeChange(plugin: IPlugin, hook: (Pair<String, String>, Pair<String, String>) -> Unit) {
        ON_SCOPE_CHANGE.remove(plugin to hook)
    }

    /** Returns true to go through with the change */
    fun beforeScopeChange(old: Pair<String, String>, new: Pair<String, String>): Boolean
            = BEFORE_SCOPE_CHANGE
            .filter { (plugin) -> PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
            .fold(true) { state, (_, hook) -> hook(old, new, state) }

    fun afterScopeChange(old: Pair<String, String>, new: Pair<String, String>): Unit
            = ON_SCOPE_CHANGE
            .filter { (plugin) -> PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
            .forEach { (_, hook) -> hook(old, new) }
}