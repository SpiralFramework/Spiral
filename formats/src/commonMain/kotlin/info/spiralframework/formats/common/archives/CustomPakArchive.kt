package info.spiralframework.formats.common.archives

import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.CountingOutputFlow
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.copyFrom
import dev.brella.kornea.io.common.flow.extensions.writeInt32LE
import dev.brella.kornea.io.common.useInputFlow

@ExperimentalUnsignedTypes
public class CustomPakArchive {
    private val _files: MutableMap<Int, DataSource<*>> = HashMap()
    public val files: List<Map.Entry<Int, DataSource<*>>>
        get() = _files.entries.sortedBy(Map.Entry<Int, DataSource<*>>::key)

    public fun add(dataSource: DataSource<*>): Unit = set(nextFreeIndex(), dataSource)
    public operator fun set(index: Int, dataSource: DataSource<*>) {
        requireNotNull(dataSource.dataSize)
        //TODO: Automatically cache if not reproducible?
        require(dataSource.reproducibility.isStatic() || dataSource.reproducibility.isDeterministic()) //We want reproducible data only
        _files[index] = dataSource
    }

    public suspend fun compile(output: OutputFlow): Unit = compileFrom(output, if (output is CountingOutputFlow) output.streamOffset else 0)
    public suspend fun compileFrom(output: OutputFlow, startingOffset: Long) {
        output.writeInt32LE(_files.size)

        var offset = startingOffset + 4 + (_files.size * 4)

        val range = (_files.keys.maxOrNull() ?: return) + 1

        for (index in 0 until range) {
            val dataSource = _files[index] ?: continue

            output.writeInt32LE(offset)
            offset += dataSource.dataSize!!.toLong()
        }

        for (index in 0 until range) {
            val dataSource = _files[index] ?: continue
            dataSource.useInputFlow { output.copyFrom(it) }
        }
    }

    public fun nextFreeIndex(): Int {
        var prev = 0
        _files.keys.forEach { index ->
            if (index > prev + 1)
                return prev + 1
            prev = index
        }

        return _files.size
    }
}

@ExperimentalUnsignedTypes
public inline fun pakArchive(block: CustomPakArchive.() -> Unit): CustomPakArchive {
    val pak = CustomPakArchive()
    pak.block()
    return pak
}
@ExperimentalUnsignedTypes
public suspend fun OutputFlow.compilePakArchive(block: CustomPakArchive.() -> Unit) {
    val pak = CustomPakArchive()
    pak.block()
    pak.compile(this)
}