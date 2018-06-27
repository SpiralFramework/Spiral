package org.abimon.osl

import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.*
import org.abimon.spiral.core.objects.game.v3.V3

sealed class GameContext {
    abstract class DRGameContext(open val game: DRGame): GameContext()

    abstract class HopesPeakGameContext(override val game: HopesPeakDRGame): DRGameContext(game)
    object DR1GameContext: HopesPeakGameContext(DR1)
    object DR2GameContext: HopesPeakGameContext(DR2)
    object UDGGameContext: HopesPeakGameContext(UDG)
    object UnknownHopesPeakGameContext: HopesPeakGameContext(UnknownHopesPeakGame)
    open class CatchAllHopesPeakGameContext(game: HopesPeakDRGame): HopesPeakGameContext(game)

    abstract class V3GameContext(override val game: V3): DRGameContext(game)
    object V3GameContextObject: V3GameContext(V3)
    open class CatchAllV3GameContext(game: V3): V3GameContext(game)

    open class STXGameContext: GameContext()
}