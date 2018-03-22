package org.abimon.spiral.core.objects.archives

import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.util.*

class WADTest {
    //@Test
    fun sourceWadHeader() {
        val wad = wadFor("https://dr.abimon.org/unit_tests/wad/valid/src.wad")!!
        assert(wad.major == 1)
        assert(wad.minor == 1)
        assert(wad.header.isEmpty())
        assert(wad.files.size == 100)
        assert(wad.directories.size == 32)
    }

    //@Test
    fun cinnamonWadHeader() {
        val wad = wadFor("https://dr.abimon.org/unit_tests/wad/valid/cinnamon.wad")!!
        assert(wad.major == 1)
        assert(wad.minor == 1)
        assert(wad.header.isEmpty())
        assert(wad.files.size == 16)
        assert(wad.directories.size == 1)
    }

    //@Test
    fun usWadHeader() {
        val wad = wadFor("https://dr.abimon.org/unit_tests/wad/valid/us.wad")!!
        assert(wad.major == 1)
        assert(wad.minor == 1)
        assert(wad.header.isEmpty())
        assert(wad.files.size == 4)
        assert(wad.directories.size == 1)
    }

    //@Test
    fun sourceWadContents() {
        val wad = wadFor("https://dr.abimon.org/unit_tests/wad/valid/src.wad")!!

        wad.files.forEach { fileEntry ->
            val wadTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")
            val remoteTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")

            wadTmp.deleteOnExit()
            remoteTmp.deleteOnExit()

            FileOutputStream(wadTmp).use { fos -> fileEntry.inputStream.use { fis -> fis.copyTo(fos) } }
            FileOutputStream(remoteTmp).use { fos ->
                URLInputStream("https://dr.abimon.org/unit_tests/wad/valid/contents/src/${fileEntry.name}").use { uis ->
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
    fun cinnamonWadContents() {
        val wad = wadFor("https://dr.abimon.org/unit_tests/wad/valid/cinnamon.wad")!!

        wad.files.forEach { fileEntry ->
            val wadTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")
            val remoteTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")

            wadTmp.deleteOnExit()
            remoteTmp.deleteOnExit()

            FileOutputStream(wadTmp).use { fos -> fileEntry.inputStream.use { fis -> fis.copyTo(fos) } }
            FileOutputStream(remoteTmp).use { fos ->
                URLInputStream("https://dr.abimon.org/unit_tests/wad/valid/contents/cinnamon/${fileEntry.name}").use { uis ->
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
    fun usWadContents() {
        val wad = wadFor("https://dr.abimon.org/unit_tests/wad/valid/us.wad")!!

        wad.files.forEach { fileEntry ->
            val wadTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")
            val remoteTmp = File.createTempFile(UUID.randomUUID().toString(), ".tmp")

            wadTmp.deleteOnExit()
            remoteTmp.deleteOnExit()

            FileOutputStream(wadTmp).use { fos -> fileEntry.inputStream.use { fis -> fis.copyTo(fos) } }
            FileOutputStream(remoteTmp).use { fos ->
                URLInputStream("https://dr.abimon.org/unit_tests/wad/valid/contents/us/${fileEntry.name}").use { uis ->
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
    fun invalidMagicNumber() = assert(wadFor("https://dr.abimon.org/unit_tests/wad/invalid/src_bad_magic.wad") == null)

    //@Test
    fun invalidBelowFileCount() = assert(wadFor("https://dr.abimon.org/unit_tests/wad/invalid/src_below_file_count.wad") == null)

    //@Test
    fun invalidAboveFileCount() = assert(wadFor("https://dr.abimon.org/unit_tests/wad/invalid/src_above_file_count.wad") == null)

    //@Test
    fun invalidBelowMinNameLength() = assert(wadFor("https://dr.abimon.org/unit_tests/wad/invalid/src_below_filename_length.wad") == null)

    //@Test
    fun invalidAboveMinNameLength() = assert(wadFor("https://dr.abimon.org/unit_tests/wad/invalid/src_above_filename_length.wad") == null)

    infix fun wadFor(str: String): WAD? {
        val url = URL(str)

        val connection = url.openConnection()
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:58.0) Gecko/20100101 Firefox/58.0")
        val urlStream = connection.getInputStream()
        val tmp = File.createTempFile(UUID.randomUUID().toString(), ".wad")
        tmp.deleteOnExit()
        FileOutputStream(tmp).use { out -> urlStream.use { inStream -> inStream.copyTo(out) } }

        return WAD { FileInputStream(tmp) }
    }

    fun URLInputStream(str: String): InputStream {
        val url = URL(str)

        val connection = url.openConnection()
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:58.0) Gecko/20100101 Firefox/58.0")
        return connection.getInputStream()
    }
}