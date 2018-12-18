package info.spiralframework.core.formats.archives

import info.spiralframework.core.DataContext
import info.spiralframework.core.DataSource
import info.spiralframework.core.FormatChance
import info.spiralframework.core.formats.EnumFormatWriteResponse
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
import org.abimon.spiral.core.objects.archives.CPK
import org.abimon.spiral.core.objects.archives.CustomWAD
import org.abimon.spiral.core.objects.archives.WAD
import org.abimon.spiral.core.objects.game.DRGame
import java.io.OutputStream

object WadFormat: ReadableSpiralFormat<WAD>, WritableSpiralFormat {
    override fun supportsWriting(data: Any): Boolean = data is WAD || data is CPK

    override fun isFormat(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatChance {
        val wad = WAD(source) ?: return FormatChance(false, 1.0)

        if (wad.files.size == 1)
            return FormatChance(true, 0.75)

        return FormatChance(wad.files.isNotEmpty(), 1.0)
    }

    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): WAD? = WAD(source)

    override fun write(name: String?, game: DRGame?, context: DataContext, data: Any, stream: OutputStream): EnumFormatWriteResponse {
        val customWad = CustomWAD()

        when (data) {
            is WAD -> customWad.add(data)
            is CPK -> data.files.forEach { entry -> customWad.add(entry.name, entry.extractSize, entry::inputStream) }
            else -> return EnumFormatWriteResponse.WRONG_FORMAT
        }

        customWad.compile(stream)
        return EnumFormatWriteResponse.SUCCESS
    }
}