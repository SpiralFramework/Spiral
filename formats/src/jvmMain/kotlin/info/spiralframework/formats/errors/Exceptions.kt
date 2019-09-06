package info.spiralframework.formats.errors

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.game.hpa.HopesPeakDRGame

class HopesPeakMissingGameException(msg: String): IllegalStateException(msg) {
    constructor(context: SpiralContext, providedGame: HopesPeakDRGame?): this(context.localise("formats.exceptions.hpa_missing", if (providedGame == null) "(No game provided)" else providedGame.names.firstOrNull() ?: providedGame))
}