package org.abimon.spiral.core.objects.archives

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.print
import org.abimon.spiral.core.writeInt
import org.abimon.spiral.core.writeLong
import org.abimon.spiral.util.trace
import org.abimon.visi.io.*
import org.abimon.visi.lang.child
import org.abimon.visi.lang.parents
import java.io.File
import java.io.OutputStream
import java.util.*
import kotlin.collections.HashMap

class CustomWAD {
    var major: Long = 0
    var minor: Long = 0
    var header: ByteArray = ByteArray(0)

    private val files: MutableMap<String, DataSource> = HashMap()
    private val filePriorities: MutableMap<String, Int> = HashMap()
    private val dataCoroutines: MutableList<Job> = ArrayList()

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

    fun data(name: String, dataSource: DataSource, prioritise: Boolean = false) {
        val newName = name.replace(File.separator, "/")
        val (out, source) = CacheHandler.cacheStream()
        launchCoroutine { dataSource.pipe(out) }
        files[newName] = source

        if (prioritise)
            filePriorities[name] = (filePriorities[name] ?: 0) + 1
    }

    fun data(name: String, data: ByteArray, prioritise: Boolean = false) {
        val newName = name.replace(File.separator, "/")
        val (out, source) = CacheHandler.cacheStream()
        launchCoroutine { out.use { stream -> stream.write(data) } }
        files[newName] = source

        if (prioritise)
            filePriorities[name] = (filePriorities[name] ?: 0) + 1
    }

    fun file(file: File, name: String = file.name, prioritise: Boolean = false) {
        val newName = name.replace(File.separator, "/")
        files[newName] = FileDataSource(file)

        if (prioritise)
            filePriorities[name] = (filePriorities[name] ?: 0) + 1
    }

    fun directory(file: File, names: Map<File, String> = HashMap(), prioritise: Boolean = false) {
        if (!file.isDirectory)
            throw IllegalArgumentException("$file is not a directory")
        file.iterate(false).forEach { file(it, names.getOrElse(it, { it.absolutePath.replace("${file.absolutePath}${File.separator}", "", prioritise) })) }
    }

    fun wad(wad: WAD) {
        major(wad.major)
        minor(wad.minor)

        wad.files.forEach { file -> files[file.name.replace(File.separator, "/")] = file }
        filePriorities.putAll(wad.spiralPriorityList)
    }

    fun compile(wad: OutputStream) {
        wad.print("AGAR")
        wad.writeInt(major)
        wad.writeInt(minor)
        wad.writeInt(header.size)
        wad.write(header)

        filePriorities[SpiralData.SPIRAL_PRIORITY_LIST] = 0
        files[SpiralData.SPIRAL_PRIORITY_LIST] = ByteArrayDataSource(SpiralData.MAPPER.writeValueAsBytes(filePriorities))

        wad.writeInt(files.size)

        trace("Waiting on coroutines")
        runBlocking { dataCoroutines.forEach { it.join() } }
        trace("Finished waiting")

        var offset = 0L
        val sortedFiles = files.entries.sortedWith(Comparator { (first), (second) -> (filePriorities[first] ?: 0).compareTo((filePriorities[second] ?: 0)) })

        sortedFiles.forEach { (filename, raf) ->
            val name = filename.toByteArray(Charsets.UTF_8)
            wad.writeInt(name.size)
            wad.write(name)
            wad.writeLong(raf.size)
            wad.writeLong(offset)

            offset += raf.size
        }

        val dirs = HashMap<String, HashSet<String>>()

        sortedFiles.forEach { (filename) ->
            var str = filename.parents
            var prev = ""

            while (str.lastIndexOf("/") > -1) {
                if (!dirs.containsKey(str))
                    dirs[str] = HashSet()
                if (prev != filename && prev.isNotBlank())
                    dirs[str]!!.add(prev)
                prev = str
                str = str.parents
            }

            if (!dirs.containsKey(str))
                dirs[str] = HashSet()
            if (prev != filename && prev.isNotBlank())
                dirs[str]!!.add(prev)

            if (!dirs.containsKey(""))
                dirs[""] = HashSet()
            if (str.isNotBlank())
                dirs[""]!!.add(str)

            if (dirs.containsKey(filename.parents))
                dirs[filename.parents]!!.add(filename)
        }

        val setDirs = getDirs(dirs, "")
        wad.writeInt(setDirs.size)

        setDirs.sortedWith(Comparator<String>(String::compareTo)).forEach {
            val name = it.toByteArray(Charsets.UTF_8)
            wad.writeInt(name.size)
            wad.write(name)
            wad.writeInt(dirs[it]!!.size)

            dirs[it]!!.sortedWith(Comparator<String>(String::compareTo)).forEach {
                val fileName = it.child.toByteArray(Charsets.UTF_8)
                wad.writeInt(fileName.size)
                wad.write(fileName)
                wad.write(if (dirs.containsKey(it)) 1 else 0)
            }
        }

        sortedFiles.forEach { (_, source) -> source.pipe(wad) }
    }

    fun getDirs(dirs: HashMap<String, HashSet<String>>, dir: String = ""): HashSet<String> {
        val set = HashSet<String>()
        set.add(dir)

        dirs[dir]!!
                .filter { dirs.containsKey(it) }
                .forEach { set.addAll(getDirs(dirs, it)) }

        return set
    }

    fun launchCoroutine(op: () -> Unit) {
        val job = launch(CommonPool) { op() }
        dataCoroutines.add(job)
    }
}