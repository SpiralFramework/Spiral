package org.abimon.spiral.core.data

data class ModelConfig(
    val archives: Set<String> = emptySet(),
    val debug: Boolean = false
)