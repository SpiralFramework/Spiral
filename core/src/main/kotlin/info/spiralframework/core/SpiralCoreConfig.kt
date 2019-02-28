package info.spiralframework.core

import com.fasterxml.jackson.annotation.JsonProperty

open class SpiralCoreConfig(
        @JsonProperty("update_connect_timeout") val updateConnectTimeout: Int?,
        @JsonProperty("update_read_timeout") val updateReadTimeout: Int?,
        @JsonProperty("network_connect_timeout") val networkConnectTimeout: Int?,
        @JsonProperty("network_read_Timeout") val networkReadTimeout: Int?,
        @JsonProperty("api_base") val apiBase: String?,
        @JsonProperty("jenkins_base") val jenkinsBase: String?
)