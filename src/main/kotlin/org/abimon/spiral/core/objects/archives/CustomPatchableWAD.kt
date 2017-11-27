package org.abimon.spiral.core.objects.archives

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.util.longToByteArray
import org.abimon.spiral.util.trace
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.FileDataSource
import org.abimon.visi.io.readChunked
import java.io.File
import java.io.RandomAccessFile
import java.util.ArrayList
import java.util.HashSet
import kotlin.collections.HashMap

class CustomPatchableWAD(val wadFile: File) {
    val wad = WAD(FileDataSource(wadFile))

    private val files: MutableMap<String, DataSource> = HashMap()
    private val dataCoroutines: MutableList<Job> = ArrayList()

    fun data(name: String, dataSource: DataSource, prioritise: Boolean = false) {
        val newName = name.replace(File.separator, "/")

        if(wad.files.any { entry -> entry.name == newName }) {
            val wadFile = wad.files.first { entry -> entry.name == newName }
            if(wadFile.data contentEquals dataSource.data)
                return
        }

        val (out, source) = CacheHandler.cacheStream()
        launchCoroutine { dataSource.pipe(out) }
        files[newName] = source
    }

    fun data(name: String, data: ByteArray) {
        val newName = name.replace(File.separator, "/")

        if(wad.files.any { entry -> entry.name == newName }) {
            val wadFile = wad.files.first { entry -> entry.name == newName }
            if(wadFile.data contentEquals data)
                return
        }

        val (out, source) = CacheHandler.cacheStream()
        launchCoroutine { out.use { stream -> stream.write(data) } }
        files[newName] = source
    }

    fun patch() {
        val patchingFile = RandomAccessFile(wadFile, "rw")

        trace("Waiting on coroutines")
        runBlocking { dataCoroutines.forEach { it.join() } }
        trace("Finished waiting")

        //Okay, now the awkward part
        //We need to write every entry with a priority equal to or greater than the lowest priority to a cache

        val lowestPriority = files.map { (name) -> wad.spiralPriorityList[name] ?: 0 }.min() ?: return //Bail; if there's no files then there's no patch
        val newPriorities = wad.spiralPriorityList.mapValues { (name, priority) -> if(files.containsKey(name)) priority + 1 else priority }
        val rewrite = wad.files.filter { (name) -> (wad.spiralPriorityList[name] ?: 0) >= lowestPriority }.sortedBy { (name) -> (newPriorities[name] ?: 0) }

        var dataOffset = (rewrite.firstOrNull() ?: return).offset

        rewrite.forEach outerLoop@{ entry ->
            var offset = 20L

            patchingFile.seek(dataOffset + wad.dataOffset)
            if(entry.name in files) {
                val source = files[entry.name]!!
                source.use { stream -> stream.readChunked { chunk -> patchingFile.write(chunk) } }
            } else {
                entry.use { stream -> stream.readChunked { chunk -> patchingFile.write(chunk) } }
            }

            wad.files.forEach { tmpEntry ->
                if(tmpEntry.name == entry.name) {
                    if(entry.name in files) {
                        val source = files[entry.name]!!

                        patchingFile.seek(offset + 4 + tmpEntry.name.toByteArray(Charsets.UTF_8).size)
                        patchingFile.write(longToByteArray(source.size, little = true))
                        patchingFile.write(longToByteArray(dataOffset, little = true))

                        dataOffset += source.size
                    } else {
                        patchingFile.seek(offset + 4 + tmpEntry.name.toByteArray(Charsets.UTF_8).size)
                        patchingFile.write(longToByteArray(entry.size, little = true))
                        patchingFile.write(longToByteArray(dataOffset, little = true))

                        dataOffset += entry.size
                    }

                    return@outerLoop
                } else
                    offset += 4 + tmpEntry.name.toByteArray(Charsets.UTF_8).size + 16
            }
        }
    }

//    fun compile(wad: OutputStream) {
//        wad.print("AGAR")
//        wad.writeInt(major)
//        wad.writeInt(minor)
//        wad.writeInt(header.size)
//        wad.write(header)
//
//        wad.writeInt(files.size)
//
//        trace("Waiting on coroutines")
//        runBlocking { dataCoroutines.forEach { it.join() } }
//        trace("Finished waiting")
//
//        var offset = 0L
//        val sortedFiles = files.entries.sortedWith(Comparator { (first), (second) -> (filePriorities[first] ?: 0).compareTo((filePriorities[second] ?: 0)) })
//
//        sortedFiles.forEach { (filename, raf) ->
//            val name = filename.toByteArray(Charsets.UTF_8)
//            wad.writeInt(name.size)
//            wad.write(name)
//            wad.writeLong(raf.length())
//            wad.writeLong(offset)
//
//            offset += raf.length()
//        }
//
//        val dirs = HashMap<String, HashSet<String>>()
//
//        sortedFiles.forEach { (filename) ->
//            var str = filename.parents
//            var prev = ""
//
//            while (str.lastIndexOf("/") > -1) {
//                if (!dirs.containsKey(str))
//                    dirs[str] = HashSet()
//                if (prev != filename && prev.isNotBlank())
//                    dirs[str]!!.add(prev)
//                prev = str
//                str = str.parents
//            }
//
//            if (!dirs.containsKey(str))
//                dirs[str] = HashSet()
//            if (prev != filename && prev.isNotBlank())
//                dirs[str]!!.add(prev)
//
//            if (!dirs.containsKey(""))
//                dirs[""] = HashSet()
//            if (str.isNotBlank())
//                dirs[""]!!.add(str)
//
//            if (dirs.containsKey(filename.parents))
//                dirs[filename.parents]!!.add(filename)
//        }
//
//        val setDirs = getDirs(dirs, "")
//        wad.writeInt(setDirs.size)
//
//        setDirs.sortedWith(Comparator<String>(String::compareTo)).forEach {
//            val name = it.toByteArray(Charsets.UTF_8)
//            wad.writeInt(name.size)
//            wad.write(name)
//            wad.writeInt(dirs[it]!!.size)
//
//            dirs[it]!!.sortedWith(Comparator<String>(String::compareTo)).forEach {
//                val fileName = it.child.toByteArray(Charsets.UTF_8)
//                wad.writeInt(fileName.size)
//                wad.write(fileName)
//                wad.write(if (dirs.containsKey(it)) 1 else 0)
//            }
//        }
//
//        val wadChannel = Channels.newChannel(wad)
//        sortedFiles.forEach { (_, raf) -> raf.channel.transferTo(0, raf.length(), wadChannel) }
//    }

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