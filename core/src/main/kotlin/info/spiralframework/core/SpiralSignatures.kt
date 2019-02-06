package info.spiralframework.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.isSuccessful
import info.spiralframework.base.util.RSAPublicKey
import java.security.PublicKey

object SpiralSignatures {
    const val SIGNATURE_PATH = "https://github.com/UnderMybrella/SpiralSignatures/raw/master/Spiral-%s/%s/%s.sig"
    val PUBLIC_KEY: PublicKey? by lazy {
        Fuel.get("https://spiralframework.info/public.key?time=${System.nanoTime()}")
                .userAgent()
                .timeout(2 * 1000)
                .timeoutRead(2 * 1000)
                .response().second.takeIf(Response::isSuccessful)?.data?.let { data ->
            try {
                RSAPublicKey(data)
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }

    fun signatureForModule(module: String, version: String, file: String): ByteArray? =
            Fuel.get(String.format(SIGNATURE_PATH, module, version, file))
                    .userAgent()
                    .timeout(2 * 1000)
                    .response().second.takeIf(Response::isSuccessful)?.data
}