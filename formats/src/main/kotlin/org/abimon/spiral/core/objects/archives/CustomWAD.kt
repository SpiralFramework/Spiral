package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.writeInt32LE
import org.abimon.spiral.core.utils.writeInt64LE
import java.io.File
import java.io.OutputStream
import java.util.*

class CustomWAD: ICustomArchive {
    val files: MutableMap<String, Pair<Long, (OutputStream) -> Unit>> = HashMap()
    var major: Int = 1
    var minor: Int = 1

    fun add(wad: WAD) {
        for (entry in wad.files)
            add(entry.name, entry.size, entry::inputStream)
    }

    override val dataSize: Long
        get() = 24L + run {
            val fileNames = files.keys.sorted()
            val directories = TreeMap<String, MutableList<String>>()

            val fileSizes = fileNames.sumBy { name ->
                val size = files[name]?.first ?: 0L
                val encoded = name.toByteArray(Charsets.UTF_8)

                return@sumBy (4 + encoded.size + 16 + size).toInt()
            }

            for (name in fileNames) {
                var subbedName = name

                do {
                    val newSub = subbedName.substringBeforeLast('/')

                    if (!directories.containsKey(newSub))
                        directories[newSub] = ArrayList()

                    directories[newSub]!!.add(subbedName)
                    subbedName = newSub
                } while (subbedName.contains('/'))

                if (!directories.containsKey(""))
                    directories[""] = ArrayList()

                directories[""]!!.add(subbedName)
            }

            val realDirectories = directories.filterKeys { directoryName -> directoryName !in fileNames }
            val directorySizes = realDirectories.entries.sumBy { (directoryName, directoryListings) ->
                return@sumBy 8 + directoryName.toByteArray(Charsets.UTF_8).size + directoryListings.sumBy sub@{ subEntry ->
                    return@sub 5 + subEntry.toByteArray(Charsets.UTF_8).size
                }
            }

            return@run fileSizes + directorySizes
        }

    override fun add(dir: File) = add(dir.absolutePath.length + 1, dir)
    fun add(parentLength: Int, dir: File) {
        for (subfile in dir.listFiles { file -> !file.isHidden && !file.name.startsWith(".") && !file.name.startsWith("__") })
            if (subfile.isDirectory)
                add(parentLength, subfile)
            else
                add(subfile.absolutePath.substring(parentLength), subfile)
    }

    override fun addSink(name: String, size: Long, sink: (OutputStream) -> Unit) {
        files[name.replace(File.separator, "/")] = size to sink
    }

    override fun compile(output: OutputStream) = compileWithProgress(output) { _, _ -> }

    fun compileWithProgress(output: OutputStream, progress: (String, Int) -> Unit) {
        output.writeInt32LE(WAD.MAGIC_NUMBER)

        output.writeInt32LE(major)
        output.writeInt32LE(minor)
        output.writeInt32LE(0)

        val fileNames = files.keys.sorted()

        output.writeInt32LE(fileNames.size)

        val directories = TreeMap<String, MutableList<String>>()
        var offset = 0L

        for (name in fileNames) {
            val encoded = name.toByteArray(Charsets.UTF_8)

            output.writeInt32LE(encoded.size)
            output.write(encoded)

            val size = files[name]?.first ?: 0L
            output.writeInt64LE(size)
            output.writeInt64LE(offset)

            offset += size

            var subbedName = name

            do {
                val newSub = subbedName.substringBeforeLast('/')

                if (!directories.containsKey(newSub))
                    directories[newSub] = ArrayList()

                directories[newSub]!!.add(subbedName)
                subbedName = newSub
            } while (subbedName.contains('/'))

            if (!directories.containsKey(""))
                directories[""] = ArrayList()

            directories[""]!!.add(subbedName)
        }

        val realDirectories = directories.filterKeys { directoryName -> directoryName !in fileNames }
        output.writeInt32LE(realDirectories.size)

        for ((directoryName, directoryListings) in realDirectories) {
            val encoded = directoryName.toByteArray(Charsets.UTF_8)

            output.writeInt32LE(encoded.size)
            output.write(encoded)

            output.writeInt32LE(directoryListings.size)

            for (subEntry in directoryListings) {
                val subEncoded = subEntry.toByteArray(Charsets.UTF_8)

                output.writeInt32LE(subEncoded.size)
                output.write(subEncoded)

                if(subEntry in fileNames)
                    output.write(0)
                else
                    output.write(1)
            }
        }

        fileNames.forEachIndexed { index, name ->
            val (_, sink) = files[name] ?: return@forEachIndexed

            sink(output)
            progress(name, index)
        }
    }
}