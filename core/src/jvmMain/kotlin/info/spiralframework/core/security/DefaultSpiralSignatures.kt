package info.spiralframework.core.security

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.isSuccessful
import info.spiralframework.base.util.RSAPublicKey
import info.spiralframework.base.util.matchesSha256
import info.spiralframework.core.takeIfSuccessful
import info.spiralframework.core.userAgent
import java.io.InputStream
import java.security.PublicKey

@ExperimentalStdlibApi
class DefaultSpiralSignatures: SpiralSignatures {
    companion object {
        const val LAST_PUBLIC_KEY_SHA256 = "FD3B9DEAF32420F25BCA27B7B0B1F87CC505AFE4556BEFD88DD1869383D02C57"
        const val MODULE_SIGNATURE_PATH = "https://github.com/UnderMybrella/SpiralSignatures/raw/master/Spiral-%s/%s/%s.sig"
        const val PLUGIN_SIGNATURE_PATH = "https://github.com/UnderMybrella/SpiralSignatures/raw/master/Plugins/%s/%s/%s.sig"

        private val CDN_LAST_PUBLIC_KEY_SHA256: String? by lazy {
            Fuel.get("https://storage.googleapis.com/signatures.spiralframework.info/public.key.sha256")
                    .userAgent()
                    .timeout(2 * 1000)
                    .timeoutRead(2 * 1000)
                    .response().takeIfSuccessful()?.let(ByteArray::decodeToString)
        }

        private val lastPublicKeySha256: String = CDN_LAST_PUBLIC_KEY_SHA256 ?: LAST_PUBLIC_KEY_SHA256

        private val SPIRAL_FRAMEWORK_KEY: PublicKey? by lazy {
            Fuel.get("https://spiralframework.info/public.key?time=${System.nanoTime()}")
                    .userAgent()
                    .timeout(2 * 1000)
                    .timeoutRead(2 * 1000)
                    .response().takeIfSuccessful()
                    ?.takeIf(lastPublicKeySha256::matchesSha256)
                    ?.let { data ->
                        try {
                            RSAPublicKey(data)
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            throw e
                        }
                    }
        }

        private val BUILT_IN_PUBLIC_KEY: PublicKey? by lazy {
            DefaultSpiralSignatures::class.java
                    .classLoader
                    ?.getResourceAsStream("public.key")
                    ?.use(InputStream::readBytes)
                    ?.takeIf(lastPublicKeySha256::matchesSha256)
                    ?.let { data ->
                        try {
                            RSAPublicKey(data)
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            throw e
                        }
                    }
        }
    }

    override val publicKey: PublicKey? = SPIRAL_FRAMEWORK_KEY ?: BUILT_IN_PUBLIC_KEY

    override fun signatureForModule(module: String, version: String, file: String): ByteArray? =
            Fuel.get(String.format(MODULE_SIGNATURE_PATH, module, version, file))
                    .userAgent()
                    .timeout(2 * 1000)
                    .response().second.takeIf(Response::isSuccessful)?.data

    override fun signatureForPlugin(plugin: String, version: String, file: String): ByteArray? =
            Fuel.get(String.format(PLUGIN_SIGNATURE_PATH, plugin, version, file))
                    .userAgent()
                    .timeout(2 * 1000)
                    .response().second.takeIf(Response::isSuccessful)?.data
}