package info.spiralframework.base.util

import java.nio.Buffer

fun Buffer.positionSafe(newPosition: Int): Buffer = position(newPosition)
fun Buffer.limitSafe(newLimit: Int): Buffer = limit(newLimit)
fun Buffer.flipSafe(): Buffer = flip()
fun Buffer.clearSafe(): Buffer = clear()
fun Buffer.markSafe(): Buffer = mark()
fun Buffer.resetSafe(): Buffer = reset()
fun Buffer.rewindSafe(): Buffer = rewind()