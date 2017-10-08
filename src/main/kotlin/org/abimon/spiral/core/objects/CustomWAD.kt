package org.abimon.spiral.core.objects

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.print
import org.abimon.spiral.core.writeInt
import org.abimon.spiral.core.writeLong
import org.abimon.visi.collections.remove
import org.abimon.visi.io.*
import org.abimon.visi.lang.child
import org.abimon.visi.lang.parents
import java.io.File
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*

class CustomWAD {
    var major: Long = 0
    var minor: Long = 0
    var header: ByteArray = ByteArray(0)

    private var files: MutableList<CustomWADFile> = LinkedList()

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
        data(SpiralData.SPIRAL_HEADER_NAME, FunctionDataSource { header })
    }

    fun data(name: String, dataSource: DataSource) {
        val newName = name.replace(File.separator, "/")
        files.remove { (customFile) -> customFile == newName }
        files.add(CustomWADFile(newName, dataSource))
    }

    fun data(name: String, data: ByteArray) {
        data(name, ByteArrayDataSource(data))
    }

    fun file(file: File, name: String = file.name) {
        data(name, FileDataSource(file))
    }

    fun directory(file: File, names: Map<File, String> = HashMap()) {
        if (!file.isDirectory)
            throw IllegalArgumentException("$file is not a directory")
        file.iterate(false).forEach { file(it, names.getOrElse(it, { it.absolutePath.replace("${file.absolutePath}${File.separator}", "") })) }
    }

    fun wad(wad: WAD) {
        major(wad.major)
        minor(wad.minor)

        wad.files.forEach { file -> data(file.name, file) }
    }

    fun compile(wad: OutputStream) {
        wad.print("AGAR")
        wad.writeInt(major)
        wad.writeInt(minor)
        wad.writeInt(header.size)
        wad.write(header)

        wad.writeInt(files.size)

        var offset = 0L
        files.forEach {
            val name = it.name.toByteArray(Charset.forName("UTF-8"))
            wad.writeInt(name.size)
            wad.write(name)
            wad.writeLong(it.dataSource.size)
            wad.writeLong(offset)

            offset += it.dataSource.size
        }

        val dirs = HashMap<String, HashSet<String>>()

        files.forEach {
            var str = it.name.parents
            var prev = ""

            while (str.lastIndexOf("/") > -1) {
                if (!dirs.containsKey(str))
                    dirs[str] = HashSet()
                if (prev != it.name && prev.isNotBlank())
                    dirs[str]!!.add(prev)
                prev = str
                str = str.parents
            }

            if (!dirs.containsKey(str))
                dirs[str] = HashSet()
            if (prev != it.name && prev.isNotBlank())
                dirs[str]!!.add(prev)

            if (!dirs.containsKey(""))
                dirs[""] = HashSet()
            if (str.isNotBlank())
                dirs[""]!!.add(str)

            if (dirs.containsKey(it.name.parents))
                dirs[it.name.parents]!!.add(it.name)
        }

        val setDirs = getDirs(dirs, "")
        wad.writeInt(setDirs.size)

        setDirs.sortedWith(Comparator<String>(String::compareTo)).forEach {
            val name = it.toByteArray(Charset.forName("UTF-8"))
            wad.writeInt(name.size)
            wad.write(name)
            wad.writeInt(dirs[it]!!.size)

            dirs[it]!!.sortedWith(Comparator<String>(String::compareTo)).forEach {
                val fileName = it.child.toByteArray(Charset.forName("UTF-8"))
                wad.writeInt(fileName.size)
                wad.write(fileName)
                wad.write(if (dirs.containsKey(it)) 1 else 0)
            }
        }

        files.forEach { it.dataSource.use { it.writeTo(wad) } }
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