package info.spiralframework.base.jvm.crypto

import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.jvm.flipSafe
import dev.brella.kornea.io.jvm.rewindSafe
import java.io.File
import java.io.InputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.charset.Charset
import java.security.*
import java.security.spec.KeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.streams.toList


/** ***Do not use for things like passwords*** */
public fun ByteArray.hash(algorithm: String): String {
    val md = MessageDigest.getInstance(algorithm)
    val hashBytes = md.digest(this)
    return String.format("%0${hashBytes.size shl 1}x", BigInteger(1, hashBytes))
}

/** ***Do not use for things like passwords*** */
public fun ByteArray.md2Hash(): String = hash("MD2")

/** ***Do not use for things like passwords*** */
public fun ByteArray.md5Hash(): String = hash("MD5")

/** ***Do not use for things like passwords*** */
public fun ByteArray.sha1Hash(): String = hash("SHA-1")

/** ***Do not use for things like passwords*** */
public fun ByteArray.sha224Hash(): String = hash("SHA-224")

/** ***Do not use for things like passwords*** */
public fun ByteArray.sha256Hash(): String = hash("SHA-256")

/** ***Do not use for things like passwords*** */
public fun ByteArray.sha384Hash(): String = hash("SHA-384")

/** ***Do not use for things like passwords*** */
public fun ByteArray.sha512Hash(): String = hash("SHA-512")

/** ***Do not use for things like passwords*** */
public fun ByteArray.hashBytes(algorithm: String): ByteArray {
    val md = MessageDigest.getInstance(algorithm)
    return md.digest(this)
}

/** ***Do not use for things like passwords*** */
public fun ByteArray.md2HashBytes(): ByteArray = hashBytes("MD2")

/** ***Do not use for things like passwords*** */
public fun ByteArray.md5HashBytes(): ByteArray = hashBytes("MD5")

/** ***Do not use for things like passwords*** */
public fun ByteArray.sha1HashBytes(): ByteArray = hashBytes("SHA-1")

/** ***Do not use for things like passwords*** */
public fun ByteArray.sha224HashBytes(): ByteArray = hashBytes("SHA-224")

/** ***Do not use for things like passwords*** */
public fun ByteArray.sha256HashBytes(): ByteArray = hashBytes("SHA-256")

/** ***Do not use for things like passwords*** */
public fun ByteArray.sha384HashBytes(): ByteArray = hashBytes("SHA-384")

/** ***Do not use for things like passwords*** */
public fun ByteArray.sha512HashBytes(): ByteArray = hashBytes("SHA-512")

/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
public fun String.md2Hash(): String = toByteArray(Charsets.UTF_8).md2Hash()

/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
public fun String.md5Hash(): String = toByteArray(Charsets.UTF_8).md5Hash()

/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
public fun String.sha1Hash(): String = toByteArray(Charsets.UTF_8).sha1Hash()

/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
public fun String.sha224Hash(): String = toByteArray(Charsets.UTF_8).sha224Hash()

/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
public fun String.sha256Hash(): String = toByteArray(Charsets.UTF_8).sha256Hash()

/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
public fun String.sha384Hash(): String = toByteArray(Charsets.UTF_8).sha384Hash()

/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
public fun String.sha512Hash(): String = toByteArray(Charsets.UTF_8).sha512Hash()

private fun InputStream.readChunked(
    bufferSize: Int = 8192,
    closeAfter: Boolean = true,
    processChunk: (ByteArray) -> Unit
): Int {
    val buffer = ByteArray(bufferSize)
    var total = 0
    var count = 0

    var read: Int

    while (true) {
        read = read(buffer)
        if (read < 0)
            break

        if (read == 0 && ++count > 3)
            break

        processChunk(buffer.copyOfRange(0, read))
        total += read
    }

    if (closeAfter)
        close()

    return total
}

/** ***Do not use for things like passwords*** */
public fun InputStream.hash(algorithm: String): String {
    val md = MessageDigest.getInstance(algorithm)
    readChunked { md.update(it) }
    val hashBytes = md.digest()
    return String.format("%0${hashBytes.size shl 1}x", BigInteger(1, hashBytes))
}

/** ***Do not use for things like passwords*** */
public fun InputStream.md2Hash(): String = hash("MD2")

/** ***Do not use for things like passwords*** */
public fun InputStream.md5Hash(): String = hash("MD5")

/** ***Do not use for things like passwords*** */
public fun InputStream.sha1Hash(): String = hash("SHA-1")

/** ***Do not use for things like passwords*** */
public fun InputStream.sha224Hash(): String = hash("SHA-224")

/** ***Do not use for things like passwords*** */
public fun InputStream.sha256Hash(): String = hash("SHA-256")

/** ***Do not use for things like passwords*** */
public fun InputStream.sha384Hash(): String = hash("SHA-384")

/** ***Do not use for things like passwords*** */
public fun InputStream.sha512Hash(): String = hash("SHA-512")

/** ***Do not use for things like passwords*** */
public fun ReadableByteChannel.hash(algorithm: String): String {
    val md = MessageDigest.getInstance(algorithm)
    val buffer = ByteBuffer.allocate(8192)

    while (isOpen) {
        val read = read(buffer)
        if (read <= 0)
            break


        buffer.flipSafe()
        md.update(buffer)
        buffer.rewindSafe()
    }

    val hashBytes = md.digest()
    return String.format("%0${hashBytes.size shl 1}x", BigInteger(1, hashBytes))
}

/** ***Do not use for things like passwords*** */
public fun ReadableByteChannel.md2Hash(): String = hash("MD2")

/** ***Do not use for things like passwords*** */
public fun ReadableByteChannel.md5Hash(): String = hash("MD5")

/** ***Do not use for things like passwords*** */
public fun ReadableByteChannel.sha1Hash(): String = hash("SHA-1")

/** ***Do not use for things like passwords*** */
public fun ReadableByteChannel.sha224Hash(): String = hash("SHA-224")

/** ***Do not use for things like passwords*** */
public fun ReadableByteChannel.sha256Hash(): String = hash("SHA-256")

/** ***Do not use for things like passwords*** */
public fun ReadableByteChannel.sha384Hash(): String = hash("SHA-384")

/** ***Do not use for things like passwords*** */
public fun ReadableByteChannel.sha512Hash(): String = hash("SHA-512")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.hash(algorithm: String): String {
    val md = MessageDigest.getInstance(algorithm)
    val buffer = ByteArray(8192)

    while (true) {
        val read = read(buffer) ?: break
        md.update(buffer, 0, read)
    }

    val hashBytes = md.digest()
    return String.format("%0${hashBytes.size shl 1}x", BigInteger(1, hashBytes))
}

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.md2Hash(): String = hash("MD2")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.md5Hash(): String = hash("MD5")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.sha1Hash(): String = hash("SHA-1")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.sha224Hash(): String = hash("SHA-224")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.sha256Hash(): String = hash("SHA-256")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.sha384Hash(): String = hash("SHA-384")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.sha512Hash(): String = hash("SHA-512")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.hashBytes(algorithm: String): ByteArray {
    val md = MessageDigest.getInstance(algorithm)
    val buffer = ByteArray(8192)

    while (true) {
        val read = read(buffer) ?: break
        md.update(buffer, 0, read)
    }

    return md.digest()
}

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.md2HashBytes(): ByteArray = hashBytes("MD2")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.md5HashBytes(): ByteArray = hashBytes("MD5")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.sha1HashBytes(): ByteArray = hashBytes("SHA-1")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.sha224HashBytes(): ByteArray = hashBytes("SHA-224")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.sha256HashBytes(): ByteArray = hashBytes("SHA-256")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.sha384HashBytes(): ByteArray = hashBytes("SHA-384")

/** ***Do not use for things like passwords*** */
public suspend fun InputFlow.sha512HashBytes(): ByteArray = hashBytes("SHA-512")

public fun CharArray.toByteArray(): ByteArray {
    val byteBuffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(this))
    val byteArray = ByteArray(byteBuffer.remaining())
    byteBuffer.get(byteArray)
    return byteArray
}

public fun ByteArray.encryptAES(iv: ByteArray, secret: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secret, "AES"), IvParameterSpec(iv))
    return cipher.doFinal(this)
}

public fun ByteArray.decryptAES(iv: ByteArray, secret: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(secret, "AES"), IvParameterSpec(iv))
    return cipher.doFinal(this)
}

public fun ByteArray.sign(privateKey: PrivateKey): ByteArray {
    val signature = Signature.getInstance("SHA512withRSA")
    signature.initSign(privateKey)
    signature.update(this)

    return signature.sign()
}

public fun ByteArray.sign(privateKey: File): ByteArray {
    val keyFactory = KeyFactory.getInstance("RSA")
    val private = keyFactory.generatePrivate(RSAPrivateKeySpec(privateKey.readText()))
    return sign(private)
}

public fun ByteArray.sign(privateKey: String): ByteArray {
    val keyFactory = KeyFactory.getInstance("RSA")
    val private = keyFactory.generatePrivate(RSAPrivateKeySpec(privateKey))
    return sign(private)
}

public fun InputStream.sign(privateKey: PrivateKey): ByteArray {
    val signature = Signature.getInstance("SHA512withRSA")
    signature.initSign(privateKey)
    this.readChunked { signature.update(it) }

    return signature.sign()
}

public fun InputStream.sign(privateKey: File): ByteArray {
    val keyFactory = KeyFactory.getInstance("RSA")
    val private = keyFactory.generatePrivate(RSAPrivateKeySpec(privateKey.readText()))
    return sign(private)
}

public fun InputStream.sign(privateKey: String): ByteArray {
    val keyFactory = KeyFactory.getInstance("RSA")
    val private = keyFactory.generatePrivate(RSAPrivateKeySpec(privateKey))
    return sign(private)
}

public fun ByteArray.verify(signatureData: ByteArray, publicKey: PublicKey): Boolean {
    val signature = Signature.getInstance("SHA512withRSA")
    signature.initVerify(publicKey)
    signature.update(this)

    return signature.verify(signatureData)
}

public fun ByteArray.verify(signatureData: ByteArray, publicKey: File): Boolean {
    val keyFactory = KeyFactory.getInstance("RSA")
    val public = keyFactory.generatePublic(RSAPublicKeySpec(publicKey.readText()))
    return verify(signatureData, public)
}

public fun ByteArray.verify(signatureData: ByteArray, publicKey: String): Boolean {
    val keyFactory = KeyFactory.getInstance("RSA")
    val public = keyFactory.generatePublic(RSAPublicKeySpec(publicKey))
    return verify(signatureData, public)
}

public fun InputStream.verify(signatureData: ByteArray, publicKey: PublicKey): Boolean {
    val signature = Signature.getInstance("SHA512withRSA")
    signature.initVerify(publicKey)
    this.readChunked { signature.update(it) }

    return signature.verify(signatureData)
}

public fun InputStream.verify(signatureData: ByteArray, publicKey: File): Boolean {
    val keyFactory = KeyFactory.getInstance("RSA")
    val public = keyFactory.generatePublic(RSAPublicKeySpec(publicKey.readText()))
    return verify(signatureData, public)
}

public fun InputStream.verify(signatureData: ByteArray, publicKey: String): Boolean {
    val keyFactory = KeyFactory.getInstance("RSA")
    val public = keyFactory.generatePublic(RSAPublicKeySpec(publicKey))
    return verify(signatureData, public)
}

public fun ReadableByteChannel.verify(signatureData: ByteArray, publicKey: PublicKey): Boolean {
    val signature = Signature.getInstance("SHA512withRSA")
    signature.initVerify(publicKey)
    val buffer = ByteBuffer.allocate(8192)

    while (isOpen) {
        val read = read(buffer)
        if (read <= 0)
            break


        buffer.flipSafe()
        signature.update(buffer)
        buffer.rewindSafe()
    }

    return signature.verify(signatureData)
}

public fun ByteArray.encryptRSA(publicKey: PublicKey): ByteArray {
    val cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    return cipher.doFinal(this)
}

public fun ByteArray.decryptRSA(privateKey: PrivateKey): ByteArray {
    val cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    return cipher.doFinal(this)
}

@Suppress("FunctionName")
public fun RSAPrivateKey(str: String): PrivateKey = KeyFactory.getInstance("RSA").generatePrivate(RSAPrivateKeySpec(str))

@Suppress("FunctionName")
public fun RSAPublicKey(str: String): PublicKey = KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(str))

@Suppress("FunctionName")
public fun RSAPrivateKeySpec(str: String): KeySpec = RSAPrivateKeySpec(
    Base64.getDecoder().decode(
        str
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")
    )
)

@Suppress("FunctionName")
public fun RSAPublicKeySpec(str: String): KeySpec = RSAPublicKeySpec(
    Base64.getDecoder().decode(
        str
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s+".toRegex(), "")
    )
)

@Suppress("FunctionName")
public fun RSAPrivateKey(data: ByteArray): PrivateKey = KeyFactory.getInstance("RSA").generatePrivate(RSAPrivateKeySpec(data))

@Suppress("FunctionName")
public fun RSAPublicKey(data: ByteArray): PublicKey = KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(data))

@Suppress("FunctionName")
public fun RSAPrivateKeySpec(data: ByteArray): KeySpec = PKCS8EncodedKeySpec(data)

@Suppress("FunctionName")
public fun RSAPublicKeySpec(data: ByteArray): KeySpec = X509EncodedKeySpec(data)

public fun String.matchesSha256(data: ByteArray): Boolean = data.sha256Hash().equals(this, true)

private val LAZY_HEX_DECODER: Map<Int, Byte> by lazy {
    val alphabet = "0123456789abcdef".chars().toList()
    val decoder: HashMap<Int, Byte> = HashMap()

    var value: Byte = 0
    alphabet.forEach { x ->
        alphabet.forEach { y ->
            decoder[x or y.shl(8)] = value++
        }
    }

    decoder
}

public fun ByteArray.asHexEncodedString(): String = String.format("%0${size shl 1}x", BigInteger(1, this))
public fun String.decodeHex(): ByteArray =
    ByteArray(length / 2) { i ->
        LAZY_HEX_DECODER.getValue(this[i * 2].code or this[i * 2 + 1].code.shl(8))
    }