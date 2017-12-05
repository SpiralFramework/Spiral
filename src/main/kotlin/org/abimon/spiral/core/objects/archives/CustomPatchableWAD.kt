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
import kotlin.collections.HashMap

class CustomPatchableWAD(val wadFile: File) {
    val wad = WAD(FileDataSource(wadFile))

    private val files: MutableMap<String, DataSource> = HashMap()
    private val dataCoroutines: MutableList<Job> = ArrayList()

    fun data(name: String, dataSource: DataSource) {
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

    fun launchCoroutine(op: () -> Unit) {
        val job = launch(CommonPool) { op() }
        dataCoroutines.add(job)
    }
}