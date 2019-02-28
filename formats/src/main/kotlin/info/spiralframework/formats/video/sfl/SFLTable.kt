package info.spiralframework.formats.video.sfl

import info.spiralframework.base.WindowedInputStream
import info.spiralframework.formats.video.SFL
import java.io.InputStream

open class SFLTable(val index: Int, val length: Long, val entryCount: Int, val unk: Int, val offset: Long, val sfl: SFL) {
    constructor(index: Int, length: Int, entryCount: Int, unk: Int, offset: Int, sfl: SFL): this(index, length.toLong(), entryCount, unk, offset.toLong(), sfl)
    val inputStream: InputStream
        get() = WindowedInputStream(sfl.dataSource(), offset.toLong(), length.toLong())
}