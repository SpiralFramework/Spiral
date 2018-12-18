package org.abimon.spiral.core.objects.archives

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.util.*

class PakTest {

    //@Test
    fun validPakOne() = assert(getPak("https://dr.abimon.org/unit_tests/pak/valid/fla_700.pak")?.files?.size == 7)

    //@Test
    fun validPakTwo() = assert(getPak("https://dr.abimon.org/unit_tests/pak/valid/fla_701.pak")?.files?.size == 3)

    //@Test
    fun validPakThree() = assert(getPak("https://dr.abimon.org/unit_tests/pak/valid/fla_702.pak")?.files?.size == 3)

    //@Test
    fun validPakFour() = assert(getPak("https://dr.abimon.org/unit_tests/pak/valid/fla_703.pak")?.files?.size == 3)

    //@Test
    fun validPakContentsOne() {
        val pak = getPak("https://dr.abimon.org/unit_tests/pak/valid/fla_700.pak")!!

        pak.files.forEach { pakEntry ->
            val wadTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")
            val remoteTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")

            wadTmp.deleteOnExit()
            remoteTmp.deleteOnExit()

            FileOutputStream(wadTmp).use { fos -> pakEntry.inputStream.use { fis -> fis.copyTo(fos) } }
            FileOutputStream(remoteTmp).use { fos ->
                URLInputStream("https://dr.abimon.org/unit_tests/pak/valid/contents/fla_700/${pakEntry.index}").use { uis ->
                    uis.copyTo(fos)
                }
            }

            val wadStream = FileInputStream(wadTmp)
            val remoteStream = FileInputStream(remoteTmp)

            val wadBuffer = ByteArray(8192)
            val remoteBuffer = ByteArray(8192)

            while (true) {
                val wadRead = wadStream.read(wadBuffer)
                val remoteRead = remoteStream.read(remoteBuffer)

                assert(wadRead == remoteRead)
                if (wadRead == -1)
                    break

                for (i in 0 until wadRead)
                    assert(wadBuffer[i] == remoteBuffer[i])
            }

            wadTmp.delete()
            remoteTmp.delete()
        }
    }

    //@Test
    fun validPakContentsTwo() {
        val pak = getPak("https://dr.abimon.org/unit_tests/pak/valid/fla_701.pak")!!

        pak.files.forEach { pakEntry ->
            val wadTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")
            val remoteTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")

            wadTmp.deleteOnExit()
            remoteTmp.deleteOnExit()

            FileOutputStream(wadTmp).use { fos -> pakEntry.inputStream.use { fis -> fis.copyTo(fos) } }
            FileOutputStream(remoteTmp).use { fos ->
                URLInputStream("https://dr.abimon.org/unit_tests/pak/valid/contents/fla_701/${pakEntry.index}").use { uis ->
                    uis.copyTo(fos)
                }
            }

            val wadStream = FileInputStream(wadTmp)
            val remoteStream = FileInputStream(remoteTmp)

            val wadBuffer = ByteArray(8192)
            val remoteBuffer = ByteArray(8192)

            while (true) {
                val wadRead = wadStream.read(wadBuffer)
                val remoteRead = remoteStream.read(remoteBuffer)

                assert(wadRead == remoteRead)
                if (wadRead == -1)
                    break

                for (i in 0 until wadRead)
                    assert(wadBuffer[i] == remoteBuffer[i])
            }

            wadTmp.delete()
            remoteTmp.delete()
        }
    }

    //@Test
    fun validPakContentsThree() {
        val pak = getPak("https://dr.abimon.org/unit_tests/pak/valid/fla_702.pak")!!

        pak.files.forEach { pakEntry ->
            val wadTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")
            val remoteTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")

            wadTmp.deleteOnExit()
            remoteTmp.deleteOnExit()

            FileOutputStream(wadTmp).use { fos -> pakEntry.inputStream.use { fis -> fis.copyTo(fos) } }
            FileOutputStream(remoteTmp).use { fos ->
                URLInputStream("https://dr.abimon.org/unit_tests/pak/valid/contents/fla_702/${pakEntry.index}").use { uis ->
                    uis.copyTo(fos)
                }
            }

            val wadStream = FileInputStream(wadTmp)
            val remoteStream = FileInputStream(remoteTmp)

            val wadBuffer = ByteArray(8192)
            val remoteBuffer = ByteArray(8192)

            while (true) {
                val wadRead = wadStream.read(wadBuffer)
                val remoteRead = remoteStream.read(remoteBuffer)

                assert(wadRead == remoteRead)
                if (wadRead == -1)
                    break

                for (i in 0 until wadRead)
                    assert(wadBuffer[i] == remoteBuffer[i])
            }

            wadTmp.delete()
            remoteTmp.delete()
        }
    }

    //@Test
    fun validPakContentsFour() {
        val pak = getPak("https://dr.abimon.org/unit_tests/pak/valid/fla_703.pak")!!

        pak.files.forEach { pakEntry ->
            val wadTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")
            val remoteTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")

            wadTmp.deleteOnExit()
            remoteTmp.deleteOnExit()

            FileOutputStream(wadTmp).use { fos -> pakEntry.inputStream.use { fis -> fis.copyTo(fos) } }
            FileOutputStream(remoteTmp).use { fos ->
                URLInputStream("https://dr.abimon.org/unit_tests/pak/valid/contents/fla_703/${pakEntry.index}").use { uis ->
                    uis.copyTo(fos)
                }
            }

            val wadStream = FileInputStream(wadTmp)
            val remoteStream = FileInputStream(remoteTmp)

            val wadBuffer = ByteArray(8192)
            val remoteBuffer = ByteArray(8192)

            while (true) {
                val wadRead = wadStream.read(wadBuffer)
                val remoteRead = remoteStream.read(remoteBuffer)

                assert(wadRead == remoteRead)
                if (wadRead == -1)
                    break

                for (i in 0 until wadRead)
                    assert(wadBuffer[i] == remoteBuffer[i])
            }

            wadTmp.delete()
            remoteTmp.delete()
        }
    }

    //@Test
    fun invalidPakOne() = assert(getPak("https://dr.abimon.org/unit_tests/pak/invalid/bgd_000.tga") == null)

    //@Test
    fun invalidPakTwo() = assert(getPak("https://dr.abimon.org/unit_tests/pak/invalid/debug_menu.ttf") == null)

    //@Test
    fun invalidPakThree() = assert(getPak("https://dr.abimon.org/unit_tests/pak/invalid/DR2_16.png") == null)

    //@Test
    fun invalidPakFour() = assert(getPak("https://dr.abimon.org/unit_tests/pak/invalid/movie_01.ogg") == null)

    fun getPak(str: String): Pak? = Pak {
        val url = URL(str)

        val connection = url.openConnection()
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:58.0) Gecko/20100101 Firefox/58.0")
        connection.getInputStream()
    }

    fun URLInputStream(str: String): InputStream {
        val url = URL(str)

        val connection = url.openConnection()
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:58.0) Gecko/20100101 Firefox/58.0")
        return connection.getInputStream()
    }
}