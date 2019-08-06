package info.spiralframework.base.util

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

/** ***Do not use for things like passwords*** */
fun ByteArray.hash(algorithm: String): String {
    val md = MessageDigest.getInstance(algorithm)
    val hashBytes = md.digest(this)
    return String.format("%032x", BigInteger(1, hashBytes))
}
/** ***Do not use for things like passwords*** */
fun ByteArray.md2Hash(): String = hash("MD2")
/** ***Do not use for things like passwords*** */
fun ByteArray.md5Hash(): String = hash("MD5")
/** ***Do not use for things like passwords*** */
fun ByteArray.sha1Hash(): String = hash("SHA-1")
/** ***Do not use for things like passwords*** */
fun ByteArray.sha224Hash(): String = hash("SHA-224")
/** ***Do not use for things like passwords*** */
fun ByteArray.sha256Hash(): String = hash("SHA-256")
/** ***Do not use for things like passwords*** */
fun ByteArray.sha384Hash(): String = hash("SHA-384")
/** ***Do not use for things like passwords*** */
fun ByteArray.sha512Hash(): String = hash("SHA-512")

/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
fun String.md2Hash(): String = toByteArray(Charsets.UTF_8).md2Hash()
/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
fun String.md5Hash(): String = toByteArray(Charsets.UTF_8).md5Hash()
/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
fun String.sha1Hash(): String = toByteArray(Charsets.UTF_8).sha1Hash()
/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
fun String.sha224Hash(): String = toByteArray(Charsets.UTF_8).sha224Hash()
/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
fun String.sha256Hash(): String = toByteArray(Charsets.UTF_8).sha256Hash()
/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
fun String.sha384Hash(): String = toByteArray(Charsets.UTF_8).sha384Hash()
/** **Do not use for things like passwords, or situations where the data needs to be blanked out** */
fun String.sha512Hash(): String = toByteArray(Charsets.UTF_8).sha512Hash()

/** ***Do not use for things like passwords*** */
fun InputStream.hash(algorithm: String): String {
    val md = MessageDigest.getInstance(algorithm)
    readChunked { md.update(it) }
    val hashBytes = md.digest()
    return String.format("%032x", BigInteger(1, hashBytes))
}
/** ***Do not use for things like passwords*** */
fun InputStream.md2Hash(): String = hash("MD2")
/** ***Do not use for things like passwords*** */
fun InputStream.md5Hash(): String = hash("MD5")
/** ***Do not use for things like passwords*** */
fun InputStream.sha1Hash(): String = hash("SHA-1")
/** ***Do not use for things like passwords*** */
fun InputStream.sha224Hash(): String = hash("SHA-224")
/** ***Do not use for things like passwords*** */
fun InputStream.sha256Hash(): String = hash("SHA-256")
/** ***Do not use for things like passwords*** */
fun InputStream.sha384Hash(): String = hash("SHA-384")
/** ***Do not use for things like passwords*** */
fun InputStream.sha512Hash(): String = hash("SHA-512")

/** ***Do not use for things like passwords*** */
fun ReadableByteChannel.hash(algorithm: String): String {
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
    return String.format("%032x", BigInteger(1, hashBytes))
}
/** ***Do not use for things like passwords*** */
fun ReadableByteChannel.md2Hash(): String = hash("MD2")
/** ***Do not use for things like passwords*** */
fun ReadableByteChannel.md5Hash(): String = hash("MD5")
/** ***Do not use for things like passwords*** */
fun ReadableByteChannel.sha1Hash(): String = hash("SHA-1")
/** ***Do not use for things like passwords*** */
fun ReadableByteChannel.sha224Hash(): String = hash("SHA-224")
/** ***Do not use for things like passwords*** */
fun ReadableByteChannel.sha256Hash(): String = hash("SHA-256")
/** ***Do not use for things like passwords*** */
fun ReadableByteChannel.sha384Hash(): String = hash("SHA-384")
/** ***Do not use for things like passwords*** */
fun ReadableByteChannel.sha512Hash(): String = hash("SHA-512")

fun CharArray.toByteArray(): ByteArray {
    val byteBuffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(this))
    val byteArray = ByteArray(byteBuffer.remaining())
    byteBuffer.get(byteArray)
    return byteArray
}

fun ByteArray.encryptAES(iv: ByteArray, secret: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secret, "AES"), IvParameterSpec(iv))
    return cipher.doFinal(this)
}

fun ByteArray.decryptAES(iv: ByteArray, secret: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(secret, "AES"), IvParameterSpec(iv))
    return cipher.doFinal(this)
}

fun ByteArray.sign(privateKey: PrivateKey): ByteArray {
    val signature = Signature.getInstance("SHA512withRSA")
    signature.initSign(privateKey)
    signature.update(this)

    return signature.sign()
}

fun ByteArray.sign(privateKey: File): ByteArray {
    val keyFactory = KeyFactory.getInstance("RSA")
    val private = keyFactory.generatePrivate(RSAPrivateKeySpec(privateKey.readText()))
    return sign(private)
}

fun ByteArray.sign(privateKey: String): ByteArray {
    val keyFactory = KeyFactory.getInstance("RSA")
    val private = keyFactory.generatePrivate(RSAPrivateKeySpec(privateKey))
    return sign(private)
}

fun InputStream.sign(privateKey: PrivateKey): ByteArray {
    val signature = Signature.getInstance("SHA512withRSA")
    signature.initSign(privateKey)
    this.readChunked { signature.update(it) }

    return signature.sign()
}

fun InputStream.sign(privateKey: File): ByteArray {
    val keyFactory = KeyFactory.getInstance("RSA")
    val private = keyFactory.generatePrivate(RSAPrivateKeySpec(privateKey.readText()))
    return sign(private)
}

fun InputStream.sign(privateKey: String): ByteArray {
    val keyFactory = KeyFactory.getInstance("RSA")
    val private = keyFactory.generatePrivate(RSAPrivateKeySpec(privateKey))
    return sign(private)
}

fun ByteArray.verify(signatureData: ByteArray, publicKey: PublicKey): Boolean {
    val signature = Signature.getInstance("SHA512withRSA")
    signature.initVerify(publicKey)
    signature.update(this)

    return signature.verify(signatureData)
}

fun ByteArray.verify(signatureData: ByteArray, publicKey: File): Boolean {
    val keyFactory = KeyFactory.getInstance("RSA")
    val public = keyFactory.generatePublic(RSAPublicKeySpec(publicKey.readText()))
    return verify(signatureData, public)
}

fun ByteArray.verify(signatureData: ByteArray, publicKey: String): Boolean {
    val keyFactory = KeyFactory.getInstance("RSA")
    val public = keyFactory.generatePublic(RSAPublicKeySpec(publicKey))
    return verify(signatureData, public)
}

fun InputStream.verify(signatureData: ByteArray, publicKey: PublicKey): Boolean {
    val signature = Signature.getInstance("SHA512withRSA")
    signature.initVerify(publicKey)
    this.readChunked { signature.update(it) }

    return signature.verify(signatureData)
}

fun InputStream.verify(signatureData: ByteArray, publicKey: File): Boolean {
    val keyFactory = KeyFactory.getInstance("RSA")
    val public = keyFactory.generatePublic(RSAPublicKeySpec(publicKey.readText()))
    return verify(signatureData, public)
}

fun InputStream.verify(signatureData: ByteArray, publicKey: String): Boolean {
    val keyFactory = KeyFactory.getInstance("RSA")
    val public = keyFactory.generatePublic(RSAPublicKeySpec(publicKey))
    return verify(signatureData, public)
}

fun ReadableByteChannel.verify(signatureData: ByteArray, publicKey: PublicKey): Boolean {
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

fun ByteArray.encryptRSA(publicKey: PublicKey): ByteArray {
    val cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    return cipher.doFinal(this)
}

fun ByteArray.decryptRSA(privateKey: PrivateKey): ByteArray {
    val cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    return cipher.doFinal(this)
}

fun RSAPrivateKey(str: String): PrivateKey
        = KeyFactory.getInstance("RSA").generatePrivate(RSAPrivateKeySpec(str))

fun RSAPublicKey(str: String): PublicKey
        = KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(str))

fun RSAPrivateKeySpec(str: String): KeySpec
        = RSAPrivateKeySpec(
        Base64.getDecoder().decode(str
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s+".toRegex(), "")))

fun RSAPublicKeySpec(str: String): KeySpec
        = RSAPublicKeySpec(
        Base64.getDecoder().decode(str
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s+".toRegex(), "")))

fun RSAPrivateKey(data: ByteArray): PrivateKey
        = KeyFactory.getInstance("RSA").generatePrivate(RSAPrivateKeySpec(data))

fun RSAPublicKey(data: ByteArray): PublicKey
        = KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(data))

fun RSAPrivateKeySpec(data: ByteArray): KeySpec
        = PKCS8EncodedKeySpec(data)

fun RSAPublicKeySpec(data: ByteArray): KeySpec
        = X509EncodedKeySpec(data)