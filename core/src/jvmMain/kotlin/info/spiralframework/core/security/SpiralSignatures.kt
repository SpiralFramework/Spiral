package info.spiralframework.core.security

import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.core.SpiralCoreContext
import info.spiralframework.core.SpiralHttp
import java.security.PublicKey

interface SpiralSignatures {
    val publicKey: PublicKey?

    suspend fun SpiralHttp.signatureForModule(module: String, version: String, file: String): ByteArray?
    suspend fun SpiralHttp.signatureForPlugin(plugin: String, version: String, file: String): ByteArray?
}

suspend fun <C> C.signatureForModule(module: String, version: String, file: String): ByteArray? where C: SpiralSignatures, C: SpiralHttp =
    signatureForModule(module, version, file)

suspend fun <C> C.signatureForPlugin(plugin: String, version: String, file: String): ByteArray? where C: SpiralSignatures, C: SpiralHttp =
    signatureForPlugin(plugin, version, file)

suspend fun SpiralSignatures.signatureForModule(spiralHttp: SpiralHttp, module: String, version: String, file: String) =
    spiralHttp.signatureForModule(module, version, file)

suspend fun SpiralSignatures.signatureForPlugin(spiralHttp: SpiralHttp, plugin: String, version: String, file: String) =
    spiralHttp.signatureForModule(plugin, version, file)