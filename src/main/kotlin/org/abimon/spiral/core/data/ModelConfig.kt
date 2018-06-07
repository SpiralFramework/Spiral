package org.abimon.spiral.core.data

import ch.qos.logback.classic.Level

data class ModelConfig(
        val archives: Set<String> = emptySet(), //The registered archives
        val loggerLevel: Level = Level.OFF, //What logger level we operate on
        val concurrentOperations: Int = 16, //How many concurrent operations we run, used for extraction
        val scope: Pair<String, String> = "> " to "default", //The last scope we used
        val operating: String? = null, //The operating file path
        val autoConfirm: Boolean = false, //Do we really need to prompt you to confirm each operation again?
        val purgeCache: Boolean = true, //Purge the cache on startup/exit
        val patchOperation: PatchOperation? = null, //What the patch operation is
        val patchFile: String? = null, //Patch file path
        val fileOperation: String? = null, //File Operation patch

        val attemptFingerprinting: Boolean = true, //Do we attempt fingerprinting at various stages, eg: Format detection, info

        val printExtractPercentage: Boolean = true, //Do we print the extraction percentage
        val printCompilePercentage: Boolean = true, //Do we print the compile percentage

        val noFluffIO: Boolean = false, //Disable hooks, disable percentages, just extract and compile at maximum speeds
        val multithreadedSimple: Boolean = false, //Do we use multiple threads to extract for regular operations (no conversions)

        val defaultParams: Map<String, Any?> = emptyMap(), //Default parameters supplied to formats for conversion
        val pluginData: Map<String, Any?> = emptyMap() //Data stored by plugins
)