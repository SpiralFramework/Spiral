package info.spiralframework.core.security

import dev.brella.kornea.toolkit.common.oneTimeMutable
import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.base.jvm.crypto.RSAPublicKey
import info.spiralframework.base.jvm.crypto.matchesSha256
import info.spiralframework.core.SpiralCoreContext
import info.spiralframework.core.SpiralHttp
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.io.InputStream
import java.security.PublicKey
import kotlin.reflect.KClass

@ExperimentalStdlibApi
class DefaultSpiralSignatures : SpiralSignatures, SpiralCatalyst<SpiralHttp> {
    companion object {
        const val LAST_PUBLIC_KEY_SHA256 = "FD3B9DEAF32420F25BCA27B7B0B1F87CC505AFE4556BEFD88DD1869383D02C57"
        const val MODULE_SIGNATURE_PATH = "https://github.com/UnderMybrella/SpiralSignatures/raw/master/Spiral-%s/%s/%s.sig"
        const val PLUGIN_SIGNATURE_PATH = "https://github.com/UnderMybrella/SpiralSignatures/raw/master/Plugins/%s/%s/%s.sig"

        private val BUILT_IN_PUBLIC_KEY: PublicKey? by lazy {
            DefaultSpiralSignatures::class.java
                .classLoader
                ?.getResourceAsStream("public.key")
                ?.use(InputStream::readBytes)
                ?.takeIf(LAST_PUBLIC_KEY_SHA256::matchesSha256)
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

    private var cdnLastPublicKeySha256: String? by oneTimeMutable()
    private var lastPublicKeySha256: String by oneTimeMutable()
    private var spiralFrameworkKey: PublicKey? by oneTimeMutable()
    override var publicKey: PublicKey? by oneTimeMutable()

    override val klass: KClass<SpiralHttp> = SpiralHttp::class
    private var primed: Boolean = false

    override suspend fun SpiralHttp.signatureForModule(module: String, version: String, file: String): ByteArray? =
        httpClient.get<HttpResponse>(String.format(MODULE_SIGNATURE_PATH, module, version, file))
            .takeIf { response -> response.status.value < 400 }
            ?.readBytes()

    override suspend fun SpiralHttp.signatureForPlugin(plugin: String, version: String, file: String): ByteArray? =
        httpClient.get<HttpResponse>(String.format(PLUGIN_SIGNATURE_PATH, plugin, version, file))
            .takeIf { response -> response.status.value < 400 }
            ?.readBytes()

    override suspend fun prime(catalyst: SpiralHttp) {
        if (!primed) {
            //TODO: Upgrade to Backblaze
            cdnLastPublicKeySha256 = catalyst.httpClient.get<HttpResponse>("https://storage.googleapis.com/signatures.spiralframework.info/public.key.sha256")
                .takeIf { response -> response.status.value < 400 }
                ?.readText()

            lastPublicKeySha256 = cdnLastPublicKeySha256 ?: LAST_PUBLIC_KEY_SHA256

            spiralFrameworkKey = catalyst.httpClient.get<HttpResponse>("https://spiralframework.info/public.key?time=${System.nanoTime()}")
                .takeIf { response -> response.status.value < 400 }
                ?.readBytes()
                ?.takeIf(lastPublicKeySha256::matchesSha256)
                ?.let { data ->
                    try {
                        RSAPublicKey(data)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        throw e
                    }
                }

            publicKey = spiralFrameworkKey ?: BUILT_IN_PUBLIC_KEY

            primed = true
        }
    }
}