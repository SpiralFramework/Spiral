package info.spiralframework.core.security

import info.spiralframework.core.SpiralHttp
import java.security.PublicKey

public interface SpiralSignatures {
    public val publicKey: PublicKey?

    public suspend fun SpiralHttp.signatureForModule(module: String, version: String, file: String): ByteArray?
    public suspend fun SpiralHttp.signatureForPlugin(plugin: String, version: String, file: String): ByteArray?
}

public suspend fun <C> C.signatureForModule(module: String, version: String, file: String): ByteArray? where C: SpiralSignatures, C: SpiralHttp =
    signatureForModule(module, version, file)

public suspend fun <C> C.signatureForPlugin(plugin: String, version: String, file: String): ByteArray? where C: SpiralSignatures, C: SpiralHttp =
    signatureForPlugin(plugin, version, file)

public suspend fun SpiralSignatures.signatureForModule(spiralHttp: SpiralHttp, module: String, version: String, file: String): ByteArray? =
    spiralHttp.signatureForModule(module, version, file)

public suspend fun SpiralSignatures.signatureForPlugin(spiralHttp: SpiralHttp, plugin: String, version: String, file: String): ByteArray? =
    spiralHttp.signatureForModule(plugin, version, file)