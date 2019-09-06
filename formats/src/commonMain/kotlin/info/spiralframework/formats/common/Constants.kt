package info.spiralframework.formats.common

import info.spiralframework.base.common.SpiralContext

const val SPIRAL_FORMATS_MODULE = "SpiralFormats"
const val SPIRAL_FORMATS_LOCALE_BUNDLE = "SpiralFormats"
const val NULL_TERMINATOR = '\u0000'

public inline fun <R> withFormats(context: SpiralContext, block: SpiralContext.() -> R): R = with(context.subcontext(SPIRAL_FORMATS_MODULE), block)