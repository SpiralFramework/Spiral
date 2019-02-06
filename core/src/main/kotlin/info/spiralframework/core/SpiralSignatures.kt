package info.spiralframework.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.isSuccessful
import info.spiralframework.base.util.RSAPublicKey
import java.security.PublicKey

object SpiralSignatures {
    val PUBLIC_KEY: PublicKey? by lazy {
        Fuel.get("https://spiralframework.info/public.key?time=${System.nanoTime()}").response().second.takeIf(Response::isSuccessful)?.data?.let { data ->
            try {
                RSAPublicKey(data)
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }
}