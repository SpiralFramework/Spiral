package info.spiralframework.formats.common

import info.spiralframework.base.common.SpiralContext

public const val SPIRAL_FORMATS_MODULE: String = "Spiral Formats"
public const val SPIRAL_FORMATS_LOCALE_BUNDLE: String = "SpiralFormats"
public const val BOM_BE: Int = 0xFEFF
public const val BOM_LE: Int = 0xFFFE

/** Steam ID for Danganronpa: Trigger Happy Havoc */
public const val STEAM_DANGANRONPA_TRIGGER_HAPPY_HAVOC: String = "413410"
/** Steam ID for Danganronpa 2: Goodbye Despair */
public const val STEAM_DANGANRONPA_2_GOODBYE_DESPAIR: String = "413420"

public const val STEAM_DANGANRONPA_V3_KILLING_HARMONY: String = "567640"
public const val STEAM_DANGANRONPA_ANOTHER_EPISODE_ULTRA_DESPAIR_GIRLS: String = "555950"
public const val STEAM_DANGANRONPA_V3_KILLING_HARMONY_DEMO_VERSION: String = "589120"

public const val STEAM_BUNDLE_DANGANRONPA_1_AND_2: String = "361"
public const val STEAM_BUNDLE_ABSOLUTE_DESPAIR_COLLECTION: String = "3641"
public const val STEAM_BUNDLE_DANGANRONPA_1_AND_2_AND_V3: String = "13789"

@ExperimentalUnsignedTypes
public inline fun <R> withFormats(context: SpiralContext, block: SpiralContext.() -> R): R = with(context.subcontext(SPIRAL_FORMATS_MODULE), block)