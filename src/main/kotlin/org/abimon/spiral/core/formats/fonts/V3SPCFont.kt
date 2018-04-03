package org.abimon.spiral.core.formats.fonts

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.archives.SPCFormat
import org.abimon.spiral.core.formats.archives.ZIPFormat
import org.abimon.spiral.core.objects.archives.SPC
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.core.objects.images.SRD
import org.abimon.spiral.core.objects.images.TXRItem
import org.abimon.spiral.core.utils.and
import org.abimon.spiral.util.InputStreamFuncDataSource
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO

object V3SPCFont {
    fun hook() {
        SpiralFormat[V3 to SPCFormat and ZIPFormat] = this::convertFromArchive
    }

    fun convertFromArchive(game: DRGame?, from: SpiralFormat, to: SpiralFormat, name: String?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (!"${params["font:convert"] ?: true}".toBoolean())
            return false

        if(from != SPCFormat)
            return false //Wot

        val spc = SPC(dataSource)

        //V3 operates on two files for their fonts.

        val fontSpecFile = spc.files.firstOrNull { entry -> entry.name.endsWith("stx") } ?: return false
        val fontSpec = SRD(InputStreamFuncDataSource(fontSpecFile::inputStream))
        val fontTable = spc.files.firstOrNull { entry -> entry.name == fontSpecFile.name.replace("stx", "srdv") } ?: return false

        val zip = ZipOutputStream(output)

        zip.putNextEntry(ZipEntry(fontSpecFile.name.replace("stx", "srd")))
        fontSpecFile.inputStream.use { stream -> stream.copyTo(zip) }


        fontSpec.items.filterIsInstance(TXRItem::class.java).forEach { txr ->
//            val fontName = txr.run {
//                val data = imageItem.data
//                data.skip(12)
//                val nameOffset = data.readUnsignedLittleInt()
//                val fontNameOffset = nameOffset - 64
//
//                data.reset()
//                data.skip(fontNameOffset)
//
//                return@run String(data.read(64), Charsets.UTF_16LE)
//            }

            val texture = txr.readTexture(fontTable.inputStream) ?: return@forEach

            zip.putNextEntry(ZipEntry(txr.name.replaceAfterLast('.', "png")))
            ImageIO.write(texture, "PNG", zip)
        }

        zip.finish()

        return true
    }
}