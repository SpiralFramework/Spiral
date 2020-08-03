package info.spiralframework.formats.common.archives

import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.OutputFlow


@ExperimentalUnsignedTypes
open class CustomWadArchive {
    private val _files: MutableMap<String, DataSource<*>> = LinkedHashMap()
    val files: List<Map.Entry<String, DataSource<*>>>
        get() = _files.entries.toList()

    var major: Int = 1
    var minor: Int = 1

    operator fun set(name: String, dataSource: DataSource<*>) {
        requireNotNull(dataSource.dataSize)
        //TODO: Automatically cache if not reproducible?
        require(dataSource.reproducibility.isStatic() || dataSource.reproducibility.isDeterministic()) //We want reproducible data only
        _files[name] = dataSource
    }

    @ExperimentalStdlibApi
    suspend fun compile(output: OutputFlow) {
        output.writeInt32LE(WadArchive.MAGIC_NUMBER_LE)

        output.writeInt32LE(major)
        output.writeInt32LE(minor)
        output.writeInt32LE(0)

        val fileNames = _files.keys

        output.writeInt32LE(fileNames.size)

        val directories = LinkedHashMap<String, MutableList<String>>()
        var offset = 0L

        for (name in fileNames) {
            val encoded = name.encodeToByteArray()

            output.writeInt32LE(encoded.size)
            output.write(encoded)

            val size = requireNotNull(_files.getValue(name).dataSize).toLong()
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
            val encoded = directoryName.encodeToByteArray()

            output.writeInt32LE(encoded.size)
            output.write(encoded)

            output.writeInt32LE(directoryListings.size)

            for (subEntry in directoryListings) {
                val subEncoded = subEntry.encodeToByteArray()

                output.writeInt32LE(subEncoded.size)
                output.write(subEncoded)

                output.write(if (subEntry in fileNames) 0 else 1)
            }
        }

        fileNames.forEachIndexed { _, name ->
            _files.getValue(name).useInputFlow { output.copyFrom(it) }
        }
    }
}

@ExperimentalUnsignedTypes
inline fun wadArchive(block: CustomWadArchive.() -> Unit): CustomWadArchive {
    val wad = CustomWadArchive()
    wad.block()
    return wad
}
@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun OutputFlow.compileWadArchive(block: CustomWadArchive.() -> Unit) {
    val wad = CustomWadArchive()
    wad.block()
    wad.compile(this)
}