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

    abstract class HopesPeakTrialContext(override val game: HopesPeakKillingGame): HopesPeakGameContext(game) {
        companion object {
            operator fun invoke(game: HopesPeakKillingGame): HopesPeakTrialContext {
                return when (game) {
                    DR1 -> DR1TrialContext
                    DR2 -> DR2TrialContext
                    UnknownHopesPeakGame -> UnknownHopesPeakTrialContext
                    else -> CatchAllHopesPeakTrialContext(game)
                }
            }
        }
    }

    object DR1TrialContext: HopesPeakTrialContext(DR1)
    object DR2TrialContext: HopesPeakTrialContext(DR2)
    object UnknownHopesPeakTrialContext: HopesPeakTrialContext(UnknownHopesPeakGame)
    open class CatchAllHopesPeakTrialContext(game: HopesPeakKillingGame): HopesPeakTrialContext(game)

    abstract class HopesPeakMinigameContext(game: HopesPeakKillingGame): HopesPeakTrialContext(game)
    abstract class NonstopDebateMinigameContext(game: HopesPeakKillingGame): HopesPeakMinigameContext(game) {
        companion object {
            operator fun invoke(game: HopesPeakKillingGame): NonstopDebateMinigameContext {
                return when (game) {
                    DR1 -> DR1NonstopDebateMinigameContext
                    DR2 -> DR2NonstopDebateMinigameContext
                    UnknownHopesPeakGame -> UnknownHopesPeakNonstopDebateMinigameContext
                    else -> CatchAllNonstopDebateMinigameContext(game)
                }
            }
        }
    }

    object DR1NonstopDebateMinigameContext: NonstopDebateMinigameContext(DR1)
    object DR2NonstopDebateMinigameContext: NonstopDebateMinigameContext(DR2)
    object UnknownHopesPeakNonstopDebateMinigameContext: NonstopDebateMinigameContext(UnknownHopesPeakGame)
    open class CatchAllNonstopDebateMinigameContext(game: HopesPeakKillingGame): NonstopDebateMinigameContext(game)

    abstract class V3GameContext(override val game: V3): DRGameContext(game)
    object V3GameContextObject: V3GameContext(V3)
    open class CatchAllV3GameContext(game: V3): V3GameContext(game)

    open class CatchAllDRGameContext(game: DRGame): DRGameContext(game)

    open class STXGameContext: GameContext() {
        companion object {
            val INSTANCE = STXGameContext()
        }
    }

    abstract class NonstopDebateDataContext(open val game: HopesPeakKillingGame): GameContext()
    companion object {
        operator fun invoke(game: HopesPeakKillingGame): NonstopDebateDataContext {
            return when (game) {
                DR1 -> DR1NonstopDebateDataContext
                DR2 -> DR2NonstopDebateDataContext
                else -> CatchAllNonstopDebateDataContext(game)
            }
        }
    }
    object DR1NonstopDebateDataContext: NonstopDebateDataContext(DR1)
    object DR2NonstopDebateDataContext: NonstopDebateDataContext(DR2)
    object UnknownHopesPeakNonstopDebateDataContext: NonstopDebateDataContext(UnknownHopesPeakGame)
    open class CatchAllNonstopDebateDataContext(game: HopesPeakKillingGame): NonstopDebateDataContext(game)

}