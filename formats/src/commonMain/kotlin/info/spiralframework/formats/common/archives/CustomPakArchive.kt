package info.spiralframework.formats.common.archives

import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.copyFrom
import info.spiralframework.base.common.io.flow.CountingOutputFlow
import info.spiralframework.base.common.io.flow.OutputFlow
import info.spiralframework.base.common.io.useInputFlow
import info.spiralframework.base.common.io.writeInt32LE

@ExperimentalUnsignedTypes
class CustomPakArchive {
    private val _files: MutableMap<Int, DataSource<*>> = HashMap()
    val files: List<Map.Entry<Int, DataSource<*>>>
        get() = _files.entries.sortedBy(Map.Entry<Int, DataSource<*>>::key)

    fun add(dataSource: DataSource<*>) = set(nextFreeIndex(), dataSource)
    operator fun set(index: Int, dataSource: DataSource<*>) {
        requireNotNull(dataSource.dataSize)
        //TODO: Automatically cache if not reproducible?
        require(dataSource.reproducibility.isStatic() || dataSource.reproducibility.isDeterministic()) //We want reproducible data only
        _files[index] = dataSource
    }

    suspend fun compile(output: OutputFlow) = compileFrom(output, if (output is CountingOutputFlow) output.streamOffset else 0)
    suspend fun compileFrom(output: OutputFlow, startingOffset: Long) {
        output.writeInt32LE(_files.size)

        var offset = startingOffset + 4 + (_files.size * 4)

        val range = (_files.keys.max() ?: return) + 1

        for (index in 0 until range) {
            val dataSource = _files[index] ?: continue

            output.writeInt32LE(offset)
            offset += dataSource.dataSize!!.toLong()
        }

        for (index in 0 until range) {
            val dataSource = _files[index] ?: continue
            dataSource.useInputFlow(output::copyFrom)
        }
    }

    fun nextFreeIndex(): Int {
        var prev = 0
        _files.keys.forEach { index ->
            if (index > prev + 1)
                return prev + 1
            prev = index
        }

        return _files.size
    }
}