package org.abimon.spiral.core

import org.abimon.util.CountingInputStream
import org.abimon.util.OffsetInputStream
import org.abimon.visi.io.*
import org.abimon.visi.lang.remove
import org.abimon.visi.lang.toArrayString
import org.abimon.visi.security.sha512Hash
import java.io.*
import java.nio.charset.Charset
import java.util.*
import java.util.zip.ZipInputStream

val STEAM_DANGANRONPA_TRIGGER_HAPPY_HAVOC = "413410"
val STEAM_DANGANRONPA_2_GOODBYE_DESPAIR = "413420"
val spiralHeaderName = "Spiral-Header"

/**
 * A central object to handle the WAD format used by the Steam releases of DR 1 and 2, our primary targets for modding
 *
 * When destructing, component 1 is the major version, 2 is the minor version, 3 is a list of files, 4 is a list of directories, and 5 is the data offset
 * Why would you want that? Who knows
 */
class WAD(val dataSource: DataSource) {
    val major: Int
    val minor: Int
    val header: ByteArray

    val files = LinkedList<WADFile>()
    val directories = LinkedList<WADFileDirectory>()

    val dataOffset: Long

    val spiralHeader: Optional<ByteArray>

    operator fun component1(): Int = major
    operator fun component2(): Int = minor
    operator fun component3(): List<WADFile> = files
    operator fun component4(): List<WADFileDirectory> = directories
    operator fun component5(): Long = dataOffset

    init {
        val wad = CountingInputStream(dataSource.getInputStream())

        try {
            val agar = wad.readString(4)

            if (agar != "AGAR")
                throw IllegalArgumentException("${dataSource.getLocation()} is either not a WAD file, or a corrupted/invalid one!")

            major = wad.readNumber(4, true).toInt()
            minor = wad.readNumber(4, true).toInt()
            header = wad.readPartialBytes(wad.readNumber(4, true).toInt())

            val numberOfFiles = wad.readNumber(4, true)

            for (i in 0 until numberOfFiles) {
                val len = wad.readNumber(4, true).toInt()
                val name = wad.readString(len)
                val size = wad.readNumber(8, true)
                val offset = wad.readNumber(8, true)

                files.add(WADFile(name, size, offset, this))
            }

            val numberOfDirectories = wad.readNumber(4, true)

            for (i in 0 until numberOfDirectories) {
                val len = wad.readNumber(4, true).toInt()
                val name = wad.readString(len)
                val subfiles = LinkedList<WADFileSubfile>()
                val numberOfSubFiles = wad.readNumber(4, true)

                for (j in 0 until numberOfSubFiles) {
                    val subLen = wad.readNumber(4, true).toInt()
                    val subName = wad.readString(subLen)
                    val isFile = wad.read() == 0
                    subfiles.add(WADFileSubfile(subName, isFile))
                }

                directories.add(WADFileDirectory(name, subfiles))
            }

            dataOffset = wad.count
            wad.close()

            if(files.any { (name) -> name == spiralHeaderName })
                spiralHeader = files.first { (name) -> name == spiralHeaderName }.getData().asOptional()
            else
                spiralHeader = Optional.empty()
        }
        catch(illegal: IllegalArgumentException){
            wad.close()
            throw illegal
        }
    }

    fun hasHeader(): Boolean = header.isNotEmpty()
}

data class WADFile(val name: String, val size: Long, val offset: Long, val wad: WAD): DataSource {
    override fun getLocation(): String = "WAD File ${wad.dataSource.getLocation()}, offset ${wad.dataOffset + offset} bytes"

    override fun getData(): ByteArray = getInputStream().use { it.readPartialBytes(size.toInt()) }

    override fun getInputStream(): InputStream = OffsetInputStream(wad.dataSource.getInputStream(), wad.dataOffset + offset, size)

    override fun getDataSize(): Long = size
}
data class WADFileDirectory(val name: String, val subfiles: List<WADFileSubfile>)
data class WADFileSubfile(val name: String, val isFile: Boolean)

class CustomWAD {
    var major: Long = 0
    var minor: Long = 0
    var header: ByteArray = ByteArray(0)

    private var files: LinkedList<CustomWADFile> = LinkedList()

    fun major(major: Number) {
        this.major = major.toLong()
    }
    fun minor(minor: Number) {
        this.minor = minor.toLong()
    }
    fun header(header: ByteArray) {
        this.header = header
        errPrintln("Warning: Danganronpa does not presently support the header field of a WAD file! Proceed with ***extreme*** caution! (Maybe you're looking for headerFile() ?)")
    }

    fun headerFile(header: ByteArray) {
        data(spiralHeaderName, FunctionDataSource { header })
    }

    fun data(name: String, dataSource: DataSource) {
        val newName = name.replace(File.separator, "/")
        files.remove { (customFile) -> customFile == newName }
        files.add(CustomWADFile(newName, dataSource))
    }

    fun data(name: String, data: ByteArray) {
        data(name, FunctionDataSource { data })
    }

    fun file(file: File, name: String = file.name) {
        data(name, FileDataSource(file))
    }

    fun directory(file: File, names: Map<File, String> = HashMap()) {
        if(!file.isDirectory)
            throw IllegalArgumentException("$file is not a directory")
        file.iterate(false).forEach { file(it, names.getOrElse(it, { it.absolutePath.replace("${file.absolutePath}${File.separator}", "") })) }
    }

    fun wad(wad: WAD) {
        wad.files.forEach { file -> data(file.name, file) }
    }

    fun compile(wad: OutputStream) {
        wad.writeString("AGAR")
        wad.writeNumber(major, 4, true)
        wad.writeNumber(minor, 4, true)
        wad.writeNumber(header.size.toLong(), 4, true)
        wad.write(header)

        wad.writeNumber(files.size.toLong(), 4, true)

        var offset = 0L
        files.forEach {
            val name = it.name.toByteArray(Charset.forName("UTF-8"))
            wad.writeNumber(name.size.toLong(), 4, true)
            wad.write(name)
            wad.writeNumber(it.dataSource.getDataSize(), 8, true)
            wad.writeNumber(offset, 8, true)

            offset += it.dataSource.getDataSize()
        }

        val dirs = HashMap<String, HashSet<String>>()

        files.forEach {
            var str = it.name.getParents()
            var prev = ""

            while (str.lastIndexOf("/") > -1) {
                if (!dirs.containsKey(str))
                    dirs[str] = HashSet()
                if (prev != it.name && prev.isNotBlank())
                    dirs[str]!!.add(prev)
                prev = str
                str = str.getParents()
            }

            if (!dirs.containsKey(str))
                dirs[str] = HashSet()
            if (prev != it.name && prev.isNotBlank())
                dirs[str]!!.add(prev)

            if (!dirs.containsKey(""))
                dirs[""] = HashSet()
            if (str.isNotBlank())
                dirs[""]!!.add(str)

            if (dirs.containsKey(it.name.getParents()))
                dirs[it.name.getParents()]!!.add(it.name)
        }

        val setDirs = getDirs(dirs, "")
        wad.writeNumber(setDirs.size.toLong(), 4, true)

        setDirs.sortedWith(Comparator<String>(String::compareTo)).forEach {
            val name = it.toByteArray(Charset.forName("UTF-8"))
            wad.writeNumber(name.size.toLong(), 4, true)
            wad.write(name)
            wad.writeNumber(dirs[it]!!.size.toLong(), 4, true)

            dirs[it]!!.sortedWith(Comparator<String>(String::compareTo)).forEach {
                val fileName = it.getChild().toByteArray(Charset.forName("UTF-8"))
                wad.writeNumber(fileName.size.toLong(), 4, true)
                wad.write(fileName)
                wad.write(if(dirs.containsKey(it)) 1 else 0)
            }
        }

        files.forEach { it.dataSource.getInputStream().use { it.writeTo(wad) } }
    }

    fun getDirs(dirs: HashMap<String, HashSet<String>>, dir: String = ""): HashSet<String> {
        val set = HashSet<String>()
        set.add(dir)

        dirs[dir]!!
                .filter { dirs.containsKey(it) }
                .forEach { set.addAll(getDirs(dirs, it)) }

        return set
    }
}

data class CustomWADFile(val name: String, val dataSource: DataSource)

fun customWad(init: CustomWAD.() -> Unit): CustomWAD {
    val wad = CustomWAD()
    wad.init()
    return wad
}

/**
 * Very basic and boring, yet crucial archive format.
 * Very basic structure - an unsigned integer dictating how many files there are, followed by the offsets for each file, and then followed by the data, at the offset indicated.
 *
 * There's two important things to note here - the first is the absence of any sort of filenames or format indicators.
 *
 * Filenames are therefore generated as the index of the file, and a format may be guessed based on some information.
 * SPIRAL also has a method to get the name if it has been recorded previously
 *
 * The second thing to note is that the offset, unlike [WAD] offsets, are ***not*** zero indexed. 0 would, in this case, be right at the start of the file
 */
class Pak(val dataSource: DataSource) {
    val offsets: LongArray
    val files: LinkedList<PakFile> = LinkedList()

    init {
        val pak = CountingInputStream(dataSource.getInputStream())
        try {
            val numFiles = pak.readNumber(4, true).toInt().coerceAtMost(1024) //Fair sample size
            if (numFiles <= 0)
                throw IllegalArgumentException("${dataSource.getLocation()} is either not a valid PAK file, or is corrupt ($numFiles <= 0)")
            offsets = LongArray(numFiles + 1)

            for (i in 0 until numFiles) {
                offsets[i] = pak.readNumber(4, true)
                if (offsets[i] < 0)
                    throw IllegalArgumentException("${dataSource.getLocation()} is either not a valid PAK file, or is corrupt (${offsets[i]} < 0)")
                else if (offsets[i] >= dataSource.getDataSize())
                    throw IllegalArgumentException("${dataSource.getLocation()} is either not a valid PAK file, or is corrupt (${offsets[i]} >= ${dataSource.getDataSize()})")
            }

            offsets[numFiles] = dataSource.getDataSize()

            for (i in 0 until numFiles)
                if (offsets[i] >= offsets[i + 1])
                    throw IllegalArgumentException("${dataSource.getLocation()} is either not a valid PAK file, or is corrupt ($i >= ${i + 1})")

            for (i in 0 until numFiles)
                files.add(PakFile("$i", offsets[i + 1] - offsets[i], offsets[i], this))
            pak.close()
        }
        catch(illegal: IllegalArgumentException) {
            pak.close()
            throw illegal
        }
    }
}

data class PakFile(val name: String, val size: Long, val offset: Long, val pak: Pak): DataSource {
    override fun getLocation(): String = "PAK File ${pak.dataSource.getLocation()}, offset $offset bytes"

    override fun getData(): ByteArray = getInputStream().use { it.readPartialBytes(size.toInt()) }

    override fun getInputStream(): InputStream = OffsetInputStream(pak.dataSource.getInputStream(), offset, size)

    override fun getDataSize(): Long = size
}

class CustomPak {
    val data = LinkedList<DataSource>()

    fun dataSource(dataSource: DataSource): CustomPak {
        data.add(dataSource)
        return this
    }

    fun compile(pak: OutputStream) {
        val modified = LinkedList<DataSource>()

        data.forEach {
            val possibleFormat = SpiralFormats.formatForData(it)
            if(possibleFormat.isPresent){
                val format = possibleFormat.get()

                when(format) {
                    is PNGFormat -> {
                        val baos = ByteArrayOutputStream()
                        format.convert(SpiralFormats.TGA, it, baos)
                        val data = baos.toByteArray()
                        modified.add(FunctionDataSource { data })
                        baos.close()
                    }
                    else -> modified.add(it)
                }
            }
            else
                modified.add(it)
        }
        var headerSize = 4 + modified.size.times(4).toLong()

        pak.writeNumber(modified.size.toLong(), unsigned = true)
        modified.forEach {
            pak.writeNumber(headerSize, unsigned = true)
            headerSize += it.getDataSize()
        }
        modified.forEach { it.getInputStream().writeTo(pak, closeAfter = true) }
    }
}

fun customPak(dataSource: DataSource): CustomPak {
    if(SpiralFormats.ZIP.isFormat(dataSource)) {
        val pak = CustomPak()
        val zipIn = ZipInputStream(dataSource.getInputStream())

        while(true) {
            zipIn.nextEntry ?: break
            val data = zipIn.readAllBytes()
            pak.dataSource(FunctionDataSource { data })
        }
        return pak
    }
    else
        throw IllegalArgumentException("${dataSource.getLocation()} is not a ZIP file/stream!")
}

fun customPak(init: CustomPak.() -> Unit): CustomPak {
    val pak = CustomPak()
    pak.init()
    return pak
}

/**
 * *Sigh*
 * Here we are, the script files.
 * Massive writeup for the lin format itself, check Formats.md
 */
class Lin(val dataSource: DataSource) {
    init {
        println("Why do you do this to me")
    }
}

/** Vita */

class CPK(val dataSource: DataSource) {

    val fileTable = LinkedList<CPKFileEntry>()
    val cpkData = HashMap<String, ByteArray>()
    val cpkType = HashMap<String, CPKType>()
    val utf: CPKUTF

    init {
        val cpk = CountingInputStream(dataSource.getInputStream())

        val cpkMagic = cpk.readString(4)

        if(cpkMagic != "CPK ")
            throw IllegalArgumentException("${dataSource.getLocation()} is either not a CPK file, or a corrupted/invalid one!")

        cpk.readNumber(4)

        val utfSize = cpk.readNumber(8, true)

        val cpakEntry = CPKFileEntry(fileName = "CPK_HDR", fileOffset = 0x10, fileSize = utfSize, fileType = "CPK")
        fileTable.add(cpakEntry)

        utf = CPKUTF(cpk.readPartialBytes(utfSize.toInt()), this)

        for(i in 0 until utf.columns.size) {
            cpkData[utf.columns[i].name] = utf.rows[0][i].data
            cpkType[utf.columns[i].name] = utf.rows[0][i].type
        }

        cpkData.forEach { s, bytes -> println("$s: ${bytes.toArrayString()}") }

        println(cpkData["ContentSize"]!!.inputStream().readNumber(8))
    }
}

class CPKUTF(val packet: ByteArray, val cpk: CPK) {

    enum class ColumnFlags(val flag: Int) {
        STORAGE_MASK(0xf0),
        STORAGE_NONE(0x00),
        STORAGE_ZERO(0x10),
        STORAGE_CONSTANT(0x30),
        STORAGE_PERROW(0x50),

        TYPE_MASK(0x0f),
        TYPE_DATA(0x0b),
        TYPE_STRING(0x0a),
        TYPE_FLOAT(0x08),
        TYPE_8BYTE2(0x07),
        TYPE_8BYTE(0x06),
        TYPE_4BYTE2(0x05),
        TYPE_4BYTE(0x04),
        TYPE_2BYTE2(0x03),
        TYPE_2BYTE(0x02),
        TYPE_1BYTE2(0x01),
        TYPE_1BYTE(0x00);

        operator fun component1(): Int = flag
    }

    val tableSize: Int
    val rowsOffset: Int
    val stringsOffset: Int
    val dataOffset: Int

    val tableNameLen: Int
    val numColumns: Int
    val rowLength: Int
    val numRows: Int

    val columns = LinkedList<CPKColumn>()
    val rows = LinkedList<LinkedList<CPKRow>>()

    init {
        val utf = packet.inputStream()
        if(utf.readString(4) != "@UTF")
            throw IllegalArgumentException("${cpk.dataSource.getLocation()} is either not a CPK file, or a corrupted/invalid one!")

        println(packet.copyOfRange(0, 16).toArrayString())

        tableSize = (utf.readNumber(4) + 8).toInt()
        rowsOffset = (utf.readNumber(4) + 8).toInt()
        stringsOffset = (utf.readNumber(4) + 8).toInt()
        dataOffset = (utf.readNumber(4) + 8).toInt()

        tableNameLen = utf.readNumber(4).toInt()
        numColumns = utf.readNumber(2).toInt()
        rowLength = utf.readNumber(2).toInt()
        numRows = utf.readNumber(4).toInt()

        for(i in 0 until numColumns) {
            val column = CPKColumn()
            column.flags = utf.read()
            if(column.flags == 0) {
                utf.skip(3)
                column.flags = utf.read()
            }

            column.name = packet.inputStream().skipBytes(utf.readNumber(4) + stringsOffset).readZeroString()
            columns.add(column)
        }

        for(x in 0 until numRows) {
            val currentEntry = LinkedList<CPKRow>()
            val pos = (rowsOffset + (x * rowLength)).toLong()
            val rowInputStream = packet.inputStream().skipBytes(pos)

            loop@for(y in 0 until numColumns) {
                val currentRow = CPKRow()
                val storageFlag = columns[y].flags and ColumnFlags.STORAGE_MASK.flag

                when(storageFlag) {
                    ColumnFlags.STORAGE_NONE.flag -> {
                        currentEntry.add(currentRow)
                        continue@loop
                    }

                    ColumnFlags.STORAGE_ZERO.flag -> {
                        currentEntry.add(currentRow)
                        continue@loop
                    }

                    ColumnFlags.STORAGE_CONSTANT.flag -> {
                        currentEntry.add(currentRow)
                        continue@loop
                    }
                }

                currentRow.type = CPKType.getValue(columns[y].flags and ColumnFlags.TYPE_MASK.flag)
                currentRow.position = pos

                when(currentRow.type) {
                    CPKType.UINT8 -> currentRow.data = rowInputStream.read().toByte().write()
                    CPKType.UINT16 -> currentRow.data = rowInputStream.readNumber(2).write()
                    CPKType.UINT32 -> currentRow.data = rowInputStream.readNumber(4).write()
                    CPKType.UINT64 -> currentRow.data = rowInputStream.readNumber(8).write()
                    CPKType.UFLOAT -> println("FLOAT!")
                    CPKType.STR -> packet.inputStream().skipBytes(rowInputStream.readNumber(4) + stringsOffset).readZeroString()
                    CPKType.DATA -> {
                        val position = rowInputStream.readNumber(4) + dataOffset
                        currentRow.position = position
                        currentRow.data = packet.copyOfRange(position.toInt(), rowInputStream.readNumber(4).toInt())
                    }
                }

                currentEntry.add(currentRow)
            }
            rows.add(currentEntry)
        }
    }
}

enum class CPKType(vararg val types: Int) {
    UINT8(0, 1),
    UINT16(2, 3),
    UINT32(4, 5),
    UINT64(6, 7),
    UFLOAT(8),
    STR(0xA),
    DATA(0xB);

    companion object {
        fun getValue(type: Int): CPKType {
            val cpkType = values().firstOrNull { it.types.contains(type) } ?: DATA
            return cpkType
        }
    }
}

data class CPKColumn(var flags: Int = 0, var name: String = "")

class CPKRow(var type: CPKType = CPKType.DATA, var data: ByteArray = ByteArray(0), var position: Long = 0) {
    operator fun component1(): CPKType = type
    operator fun component2(): Any {
        when(type) {
            CPKType.UINT8 -> return data.inputStream().readNumber(1, true)
            CPKType.UINT16 -> return data.inputStream().readNumber(2, true)
            CPKType.UINT32 -> return data.inputStream().readNumber(4, true)
            CPKType.UINT64 -> return data.inputStream().readNumber(8, true)
            CPKType.UFLOAT -> return data.inputStream().readFloat(true)
            CPKType.STR -> return String(data, Charset.forName("UTF-8"))
            CPKType.DATA -> return data
        }
    }
}

data class CPKFileEntry(var dirName: String = "", var fileName: String = "", var fileSize: Long = 0, var extractSize: Long = 0, var id: Long = 0, var userString: String = "", var localDir: String = "", var fileOffset: Long = 0, var fileType: String = "")

object SpiralNames {
    val pakNames = HashMap<String, Pair<String, String>>()
    val formats = HashMap<String, Pair<String, SpiralFormat>>()
    val config = File(".spiral_names")

    fun getPakName(pathName: String, data: ByteArray): Optional<String> {
        if(pakNames.containsKey(pathName)) {
            val (sha512, name) = pakNames[pathName]!!
            if(sha512 == data.sha512Hash())
                return name.asOptional()
        }

        return Optional.empty()
    }

    fun getFormat(pathName: String, data: ByteArray): Optional<SpiralFormat> {
        if(formats.containsKey(pathName)) {
            val (sha512, format) = formats[pathName]!!
            if(sha512 == data.sha512Hash())
                return format.asOptional()
        }

        return Optional.empty()
    }

    fun getFormat(pathName: String, data: DataSource): Optional<SpiralFormat> {
        if(formats.containsKey(pathName)) {
            val (sha512, format) = formats[pathName]!!
            if(sha512 == data.getData().sha512Hash())
                return format.asOptional()
        }

        return Optional.empty()
    }

    fun registerFormat(pathName: String, data: ByteArray, format: SpiralFormat) {
        formats[pathName] = Pair(data.sha512Hash(), format)
        save()
    }

    fun save() {
        val writer = PrintWriter(BufferedWriter(FileWriter(config)))
        writer.use {
            pakNames.forEach { path, pair -> it.println("name|$path|${pair.first}|${pair.second}") }
            formats.forEach { path, pair -> it.println("format|$path|${pair.first}|${pair.second.getName()}") }
        }
    }

    init {
        if(!config.exists())
            config.createNewFile()

        val reader = BufferedReader(FileReader(config))
        reader.use {
            it.forEachLine {
                val components = it.split("|")
                when (components[0]) {
                    "name" -> pakNames.put(components[1], Pair(components[2], components[3]))
                    "format" -> {
                        val format = SpiralFormats.formatForName(components[3])
                        if(format.isPresent)
                            formats.put(components[1], Pair(components[2], format.get()))
                    }
                }
            }
        }
    }
}