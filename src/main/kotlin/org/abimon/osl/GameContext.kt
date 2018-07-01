package org.abimon.osl

import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.*
import org.abimon.spiral.core.objects.game.v3.V3

sealed class GameContext {
    abstract class DRGameContext(open val game: DRGame): GameContext() {
        companion object {
            operator fun invoke(game: DRGame): DRGameContext {
                return when (game) {
                    DR1 -> DR1GameContext
                    DR2 -> DR2GameContext
                    UDG -> UDGGameContext
                    UnknownHopesPeakGame -> UnknownHopesPeakGameContext
                    V3 -> V3GameContextObject
                    is HopesPeakDRGame -> CatchAllHopesPeakGameContext(game)
                    is V3 -> CatchAllV3GameContext(game)
                    else -> CatchAllDRGameContext(game)
                }
            }
        }
    }

    abstract class HopesPeakGameContext(override val game: HopesPeakDRGame): DRGameContext(game)
    object DR1GameContext: HopesPeakGameContext(DR1)
    object DR2GameContext: HopesPeakGameContext(DR2)
    object UDGGameContext: HopesPeakGameContext(UDG)
    object UnknownHopesPeakGameContext: HopesPeakGameContext(UnknownHopesPeakGame)
    open class CatchAllHopesPeakGameContext(game: HopesPeakDRGame): HopesPeakGameContext(game)

    abstract class V3GameContext(override val game: V3): DRGameContext(game)
    object V3GameContextObject: V3GameContext(V3)
    open class CatchAllV3GameContext(game: V3): V3GameContext(game)

    open class CatchAllDRGameContext(game: DRGame): DRGameContext(game)

    open class STXGameContext: GameContext() {
        companion object {
            val INSTANCE = STXGameContext()
        }
    }

    abstract class NonstopDebateContext(open val game: HopesPeakKillingGame): GameContext()
    companion object {
        operator fun invoke(game: HopesPeakKillingGame): NonstopDebateContext {
            return when (game) {
                DR1 -> DR1NonstopDebateContext
                DR2 -> DR2NonstopDebateContext
                else -> CatchAllNonstopDebateContext(game)
            }
        }
    }
    object DR1NonstopDebateContext: NonstopDebateContext(DR1)
    object DR2NonstopDebateContext: NonstopDebateContext(DR2)
    object UnknownHopesPeakNonstopDebateContext: NonstopDebateContext(UnknownHopesPeakGame)
    open class CatchAllNonstopDebateContext(game: HopesPeakKillingGame): NonstopDebateContext(game)

}