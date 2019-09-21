package info.spiralframework.formats.errors

import info.spiralframework.base.SpiralLocale
import info.spiralframework.formats.game.DRGame

class HopesPeakMissingGameException(val providedGame: DRGame?):
        IllegalStateException(SpiralLocale.localise("formats.exceptions.hpa_missing", if (providedGame == null) "(No game provided)" else providedGame.names.firstOrNull() ?: providedGame))