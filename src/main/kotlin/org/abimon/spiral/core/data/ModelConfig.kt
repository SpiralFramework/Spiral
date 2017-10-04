package org.abimon.spiral.core.data

import org.abimon.spiral.util.LoggerLevel

data class ModelConfig(
    val archives: Set<String> = emptySet(),
    val loggerLevel: LoggerLevel = LoggerLevel.NONE,
    val debug: Boolean? = null,
    val concurrentOperations: Int = 16
)