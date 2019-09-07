package info.spiralframework.core.security

import java.security.PublicKey

interface SpiralSignatures {
    val publicKey: PublicKey?

    fun signatureForModule(module: String, version: String, file: String): ByteArray?
    fun signatureForPlugin(plugin: String, version: String, file: String): ByteArray?
}