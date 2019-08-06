package info.spiralframework.base.binding

expect object SpiralConfig {
    fun getConfigFile(module: String): String
    fun getLocalDataDir(group: String): String
}