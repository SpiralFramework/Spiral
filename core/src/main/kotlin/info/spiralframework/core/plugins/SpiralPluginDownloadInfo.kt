package info.spiralframework.core.plugins

data class SpiralPluginDownloadInfo(
        val uid: String,
        val filename: String,
        val version: String,
        val target: String?
)