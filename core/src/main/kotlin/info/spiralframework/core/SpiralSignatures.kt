package info.spiralframework.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.isSuccessful
import info.spiralframework.base.util.RSAPublicKey
import java.security.PublicKey

object SpiralSignatures {
    const val MODULE_SIGNATURE_PATH = "https://github.com/UnderMybrella/SpiralSignatures/raw/master/Spiral-%s/%s/%s.sig"
    const val PLUGIN_SIGNATURE_PATH = "https://github.com/UnderMybrella/SpiralSignatures/raw/master/Plugins/%s/%s/%s.sig"
    val PUBLIC_KEY: PublicKey? by lazy { SPIRALFRAMEWORK_PUBLIC_KEY ?: GITHUB_PUBLIC_KEY }

    val SPIRALFRAMEWORK_PUBLIC_KEY: PublicKey? by lazy {
        Fuel.get("https://spiralframework.info/public.key?time=${System.nanoTime()}")
                .userAgent()
                .timeout(2 * 1000)
                .timeoutRead(2 * 1000)
                .response().takeIfSuccessful()?.let { data ->
            try {
                RSAPublicKey(data)
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }

    val GITHUB_PUBLIC_KEY: PublicKey? by lazy {
        Fuel.get("https://github.com/UnderMybrella/SpiralSignatures/raw/master/public.key?time=${System.nanoTime()}")
                .userAgent()
                .timeout(2 * 1000)
                .timeoutRead(2 * 1000)
                .response().takeIfSuccessful()?.let { data ->
                    try {
                        RSAPublicKey(data)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        throw e
                    }
                }
    }

    val spiralFrameworkOnline: Boolean by lazy { Fuel.head("https://spiralframework.info").userAgent().timeout(10 * 1000).timeoutRead(5 * 1000).response().second.isSuccessful }
    val githubOnline: Boolean by lazy { Fuel.head("https://github.com").userAgent().timeout(10 * 1000).timeoutRead(5 * 1000).response().second.isSuccessful }

    fun signatureForModule(module: String, version: String, file: String): ByteArray? =
            Fuel.get(String.format(MODULE_SIGNATURE_PATH, module, version, file))
                    .userAgent()
                    .timeout(2 * 1000)
                    .response().second.takeIf(Response::isSuccessful)?.data

    fun signatureForPlugin(plugin: String, version: String, file: String): ByteArray? =
            Fuel.get(String.format(PLUGIN_SIGNATURE_PATH, plugin, version, file))
                    .userAgent()
                    .timeout(2 * 1000)
                    .response().second.takeIf(Response::isSuccessful)?.data

    init {}
}