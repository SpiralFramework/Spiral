package org.abimon.spiral.modding

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.abimon.spiral.core.archives.IArchive
import org.abimon.spiral.core.data.PatchOperation
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.util.LoggerLevel
import java.io.File
import java.io.InputStream

object HookManager {
    val BEFORE_OPERATING_CHANGE: MutableList<Pair<IPlugin, (File?, File?, Boolean) -> Boolean>> = ArrayList()
    val ON_OPERATING_CHANGE: MutableList<Pair<IPlugin, (File?, File?) -> Unit>> = ArrayList()

    val BEFORE_FILE_OPERATING_CHANGE: MutableList<Pair<IPlugin, (File?, File?, Boolean) -> Boolean>> = ArrayList()
    val ON_FILE_OPERATING_CHANGE: MutableList<Pair<IPlugin, (File?, File?) -> Unit>> = ArrayList()

    val BEFORE_SCOPE_CHANGE: MutableList<Pair<IPlugin, (Pair<String, String>, Pair<String, String>, Boolean) -> Boolean>> = ArrayList()
    val ON_SCOPE_CHANGE: MutableList<Pair<IPlugin, (Pair<String, String>, Pair<String, String>) -> Unit>> = ArrayList()

    val BEFORE_LOGGER_LEVEL_CHANGE: MutableList<Pair<IPlugin, (LoggerLevel, LoggerLevel, Boolean) -> Boolean>> = ArrayList()
    val ON_LOGGER_LEVEL_CHANGE: MutableList<Pair<IPlugin, (LoggerLevel, LoggerLevel) -> Unit>> = ArrayList()

    val BEFORE_CACHE_ENABLED_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean, Boolean) -> Boolean>> = ArrayList()
    val ON_CACHE_ENABLED_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean) -> Unit>> = ArrayList()

    val BEFORE_CONCURRENT_OPERATIONS_CHANGE: MutableList<Pair<IPlugin, (Int, Int, Boolean) -> Boolean>> = ArrayList()
    val ON_CONCURRENT_OPERATIONS_CHANGE: MutableList<Pair<IPlugin, (Int, Int) -> Unit>> = ArrayList()

    val BEFORE_AUTO_CONFIRM_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean, Boolean) -> Boolean>> = ArrayList()
    val ON_AUTO_CONFIRM_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean) -> Unit>> = ArrayList()

    val BEFORE_PURGE_CACHE_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean, Boolean) -> Boolean>> = ArrayList()
    val ON_PURGE_CACHE_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean) -> Unit>> = ArrayList()

    val BEFORE_PATCH_OPERATION_CHANGE: MutableList<Pair<IPlugin, (PatchOperation?, PatchOperation?, Boolean) -> Boolean>> = ArrayList()
    val ON_PATCH_OPERATION_CHANGE: MutableList<Pair<IPlugin, (PatchOperation?, PatchOperation?) -> Unit>> = ArrayList()

    val BEFORE_PATCH_FILE_CHANGE: MutableList<Pair<IPlugin, (File?, File?, Boolean) -> Boolean>> = ArrayList()
    val ON_PATCH_FILE_CHANGE: MutableList<Pair<IPlugin, (File?, File?) -> Unit>> = ArrayList()

    val BEFORE_ATTEMPT_FINGERPRINT_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean, Boolean) -> Boolean>> = ArrayList()
    val ON_ATTEMPT_FINGERPRINT_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean) -> Unit>> = ArrayList()

    val BEFORE_PRINT_EXTRACT_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean, Boolean) -> Boolean>> = ArrayList()
    val ON_PRINT_EXTRACT_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean) -> Unit>> = ArrayList()

    val BEFORE_PRINT_COMPILE_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean, Boolean) -> Boolean>> = ArrayList()
    val ON_PRINT_COMPILE_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean) -> Unit>> = ArrayList()

    val BEFORE_NO_FLUFF_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean, Boolean) -> Boolean>> = ArrayList()
    val ON_NO_FLUFF_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean) -> Unit>> = ArrayList()

    val BEFORE_MULTITHREADED_SIMPLE_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean, Boolean) -> Boolean>> = ArrayList()
    val ON_MULTITHREADED_SIMPLE_CHANGE: MutableList<Pair<IPlugin, (Boolean, Boolean) -> Unit>> = ArrayList()

    val BEFORE_EXTRACT: MutableList<Pair<IPlugin, (IArchive, File, List<Pair<String, () -> InputStream>>, Boolean) -> Boolean>> = ArrayList()
    val ON_EXTRACT: MutableList<Pair<IPlugin, (IArchive, File, List<Pair<String, () -> InputStream>>) -> Unit>> = ArrayList()
    val DURING_EXTRACT: MutableList<Pair<IPlugin, (IArchive, File, List<Pair<String, () -> InputStream>>, Pair<String, () -> InputStream>) -> Unit>> = ArrayList()
    val AFTER_EXTRACT: MutableList<Pair<IPlugin, (IArchive, File, List<Pair<String, () -> InputStream>>) -> Unit>> = ArrayList()

    fun beforeOperatingChange(old: File?, new: File?): Boolean =
            beforeChange(old, new, BEFORE_OPERATING_CHANGE)

    fun beforeFileOperatingChange(old: File?, new: File?): Boolean =
            beforeChange(old, new, BEFORE_FILE_OPERATING_CHANGE)


    fun beforeScopeChange(old: Pair<String, String>, new: Pair<String, String>): Boolean
            = beforeChange(old, new, BEFORE_SCOPE_CHANGE)

    fun beforeLoggerLevelChange(old: LoggerLevel, new: LoggerLevel): Boolean
            = beforeChange(old, new, BEFORE_LOGGER_LEVEL_CHANGE)

    fun beforeCacheEnabledChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_CACHE_ENABLED_CHANGE)

    fun beforeConcurrentOperationsChange(old: Int, new: Int): Boolean
            = beforeChange(old, new, BEFORE_CONCURRENT_OPERATIONS_CHANGE)

    fun beforeAutoConfirmChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_AUTO_CONFIRM_CHANGE)

    fun beforePurgeCacheChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_PURGE_CACHE_CHANGE)

    fun beforePatchOperationChange(old: PatchOperation?, new: PatchOperation?): Boolean
            = beforeChange(old, new, BEFORE_PATCH_OPERATION_CHANGE)

    fun beforePatchFileChange(old: File?, new: File?): Boolean
            = beforeChange(old, new, BEFORE_PATCH_FILE_CHANGE)

    fun beforeAttemptFingerprintChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_ATTEMPT_FINGERPRINT_CHANGE)

    fun beforePrintExtractChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_PRINT_EXTRACT_CHANGE)

    fun beforePrintCompileChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_PRINT_COMPILE_CHANGE)

    fun beforeNoFluffChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_NO_FLUFF_CHANGE)

    fun beforeMultithreadedSimpleChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_MULTITHREADED_SIMPLE_CHANGE)

    fun afterOperatingChange(old: File?, new: File?): Unit =
            afterChange(old, new, ON_OPERATING_CHANGE)

    fun afterFileOperatingChange(old: File?, new: File?): Unit =
            afterChange(old, new, ON_FILE_OPERATING_CHANGE)

    fun afterScopeChange(old: Pair<String, String>, new: Pair<String, String>): Unit
            = afterChange(old, new, ON_SCOPE_CHANGE)

    fun afterLoggerLevelChange(old: LoggerLevel, new: LoggerLevel): Unit
            = afterChange(old, new, ON_LOGGER_LEVEL_CHANGE)

    fun afterCacheEnabledChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_CACHE_ENABLED_CHANGE)

    fun afterConcurrentOperationsChange(old: Int, new: Int): Unit
            = afterChange(old, new, ON_CONCURRENT_OPERATIONS_CHANGE)

    fun afterAutoConfirmChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_AUTO_CONFIRM_CHANGE)

    fun afterPurgeCacheChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_PURGE_CACHE_CHANGE)

    fun afterPatchOperationChange(old: PatchOperation?, new: PatchOperation?): Unit
            = afterChange(old, new, ON_PATCH_OPERATION_CHANGE)

    fun afterPatchFileChange(old: File?, new: File?): Unit
            = afterChange(old, new, ON_PATCH_FILE_CHANGE)

    fun afterAttemptFingerprintChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_ATTEMPT_FINGERPRINT_CHANGE)

    fun afterPrintExtractChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_PRINT_EXTRACT_CHANGE)

    fun afterPrintCompileChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_PRINT_COMPILE_CHANGE)

    fun afterNoFluffChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_NO_FLUFF_CHANGE)

    fun afterMultithreadedSimpleChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_MULTITHREADED_SIMPLE_CHANGE)

    fun shouldExtract(archive: IArchive, folder: File, files: List<Pair<String, () -> InputStream>>): Boolean
            = BEFORE_EXTRACT
            .filter { (plugin) -> plugin == SpiralData.BASE_PLUGIN || PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
            .fold(true) { state, (_, hook) -> hook(archive, folder, files, state) }

    fun extracting(archive: IArchive, folder: File, files: List<Pair<String, () -> InputStream>>): Unit
            = ON_EXTRACT
            .filter { (plugin) -> plugin == SpiralData.BASE_PLUGIN || PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
            .forEach { (_, hook) -> hook(archive, folder, files) }

    var prevExtractJob: Job? = null
    fun extractingFile(archive: IArchive, folder: File, files: List<Pair<String, () -> InputStream>>, extracting: Pair<String, () -> InputStream>): Unit {
        val prev: Job? = prevExtractJob
        prevExtractJob = launch(CommonPool) {
            prev?.join()

            DURING_EXTRACT
                    .filter { (plugin) -> plugin == SpiralData.BASE_PLUGIN || PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
                    .forEach { (_, hook) -> hook(archive, folder, files, extracting) }
        }
    }

    fun finishedExtraction(archive: IArchive, folder: File, files: List<Pair<String, () -> InputStream>>): Unit
            = AFTER_EXTRACT
            .filter { (plugin) -> plugin == SpiralData.BASE_PLUGIN || PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
            .forEach { (_, hook) -> hook(archive, folder, files) }

    fun <T : Any?> beforeChange(old: T, new: T, beforeChanges: List<Pair<IPlugin, (T, T, Boolean) -> Boolean>>): Boolean
            = beforeChanges
            .filter { (plugin) -> plugin == SpiralData.BASE_PLUGIN || PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
            .fold(true) { state, (_, hook) -> hook(old, new, state) }

    fun <T : Any?> afterChange(old: T, new: T, afterChanges: List<Pair<IPlugin, (T, T) -> Unit>>): Unit
            = afterChanges
            .filter { (plugin) -> plugin == SpiralData.BASE_PLUGIN || PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
            .forEach { (_, hook) -> hook(old, new) }
}