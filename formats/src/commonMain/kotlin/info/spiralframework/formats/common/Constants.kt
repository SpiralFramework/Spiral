package info.spiralframework.formats.common

import info.spiralframework.base.common.SpiralContext

const val SPIRAL_FORMATS_MODULE = "Spiral Formats"
const val SPIRAL_FORMATS_LOCALE_BUNDLE = "SpiralFormats"
const val BOM_BE = 0xFEFF
const val BOM_LE = 0xFFFE

/** Steam ID for Danganronpa: Trigger Happy Havoc */
const val STEAM_DANGANRONPA_TRIGGER_HAPPY_HAVOC = "413410"
/** Steam ID for Danganronpa 2: Goodbye Despair */
const val STEAM_DANGANRONPA_2_GOODBYE_DESPAIR = "413420"

public inline fun <R> withFormats(context: SpiralContext, block: SpiralContext.() -> R): R = with(context.subcontext(SPIRAL_FORMATS_MODULE), block)