package info.spiralframework.formats.errors

import info.spiralframework.base.util.locale
import info.spiralframework.formats.game.DRGame

class HopesPeakMissingGameException(val providedGame: DRGame?):
        IllegalStateException(locale<String>("formats.exceptions.hpa_missing", if (providedGame == null) "(No game provided)" else providedGame.names.firstOrNull() ?: providedGame))