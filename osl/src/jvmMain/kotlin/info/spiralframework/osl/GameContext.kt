package info.spiralframework.osl

import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.game.hpa.*
import info.spiralframework.formats.game.v3.V3

sealed class GameContext {
    abstract class DRGameContext(open val game: DRGame): info.spiralframework.osl.GameContext() {
        companion object {
            operator fun invoke(game: DRGame): info.spiralframework.osl.GameContext.DRGameContext {
                return when (game) {
                    DR1 -> info.spiralframework.osl.GameContext.DR1GameContext
                    DR2 -> info.spiralframework.osl.GameContext.DR2GameContext
                    UDG -> info.spiralframework.osl.GameContext.UDGGameContext
                    UnknownHopesPeakGame -> info.spiralframework.osl.GameContext.UnknownHopesPeakGameContext
                    V3 -> info.spiralframework.osl.GameContext.V3GameContextObject
                    is HopesPeakDRGame -> info.spiralframework.osl.GameContext.CatchAllHopesPeakGameContext(game)
                    is V3 -> info.spiralframework.osl.GameContext.CatchAllV3GameContext(game)
                    else -> info.spiralframework.osl.GameContext.CatchAllDRGameContext(game)
                }
            }
        }
    }

    abstract class HopesPeakGameContext(override val game: HopesPeakDRGame): info.spiralframework.osl.GameContext.DRGameContext(game)
    object DR1GameContext: info.spiralframework.osl.GameContext.HopesPeakGameContext(DR1)
    object DR2GameContext: info.spiralframework.osl.GameContext.HopesPeakGameContext(DR2)
    object UDGGameContext: info.spiralframework.osl.GameContext.HopesPeakGameContext(UDG)
    object UnknownHopesPeakGameContext: info.spiralframework.osl.GameContext.HopesPeakGameContext(UnknownHopesPeakGame)
    open class CatchAllHopesPeakGameContext(game: HopesPeakDRGame): info.spiralframework.osl.GameContext.HopesPeakGameContext(game)

    abstract class HopesPeakTrialContext(override val game: HopesPeakKillingGame): info.spiralframework.osl.GameContext.HopesPeakGameContext(game) {
        companion object {
            operator fun invoke(game: HopesPeakKillingGame): info.spiralframework.osl.GameContext.HopesPeakTrialContext {
                return when (game) {
                    DR1 -> info.spiralframework.osl.GameContext.DR1TrialContext
                    DR2 -> info.spiralframework.osl.GameContext.DR2TrialContext
                    UnknownHopesPeakGame -> info.spiralframework.osl.GameContext.UnknownHopesPeakTrialContext
                    else -> info.spiralframework.osl.GameContext.CatchAllHopesPeakTrialContext(game)
                }
            }
        }
    }

    object DR1TrialContext: info.spiralframework.osl.GameContext.HopesPeakTrialContext(DR1)
    object DR2TrialContext: info.spiralframework.osl.GameContext.HopesPeakTrialContext(DR2)
    object UnknownHopesPeakTrialContext: info.spiralframework.osl.GameContext.HopesPeakTrialContext(UnknownHopesPeakGame)
    open class CatchAllHopesPeakTrialContext(game: HopesPeakKillingGame): info.spiralframework.osl.GameContext.HopesPeakTrialContext(game)

    abstract class HopesPeakMinigameContext(game: HopesPeakKillingGame): info.spiralframework.osl.GameContext.HopesPeakTrialContext(game)
    abstract class NonstopDebateMinigameContext(game: HopesPeakKillingGame): info.spiralframework.osl.GameContext.HopesPeakMinigameContext(game) {
        companion object {
            operator fun invoke(game: HopesPeakKillingGame): info.spiralframework.osl.GameContext.NonstopDebateMinigameContext {
                return when (game) {
                    DR1 -> info.spiralframework.osl.GameContext.DR1NonstopDebateMinigameContext
                    DR2 -> info.spiralframework.osl.GameContext.DR2NonstopDebateMinigameContext
                    UnknownHopesPeakGame -> info.spiralframework.osl.GameContext.UnknownHopesPeakNonstopDebateMinigameContext
                    else -> info.spiralframework.osl.GameContext.CatchAllNonstopDebateMinigameContext(game)
                }
            }
        }
    }

    object DR1NonstopDebateMinigameContext: info.spiralframework.osl.GameContext.NonstopDebateMinigameContext(DR1)
    object DR2NonstopDebateMinigameContext: info.spiralframework.osl.GameContext.NonstopDebateMinigameContext(DR2)
    object UnknownHopesPeakNonstopDebateMinigameContext: info.spiralframework.osl.GameContext.NonstopDebateMinigameContext(UnknownHopesPeakGame)
    open class CatchAllNonstopDebateMinigameContext(game: HopesPeakKillingGame): info.spiralframework.osl.GameContext.NonstopDebateMinigameContext(game)

    abstract class V3GameContext(override val game: V3): info.spiralframework.osl.GameContext.DRGameContext(game)
    object V3GameContextObject: info.spiralframework.osl.GameContext.V3GameContext(V3)
    open class CatchAllV3GameContext(game: V3): info.spiralframework.osl.GameContext.V3GameContext(game)

    open class CatchAllDRGameContext(game: DRGame): info.spiralframework.osl.GameContext.DRGameContext(game)

    open class STXGameContext: info.spiralframework.osl.GameContext() {
        companion object {
            val INSTANCE = info.spiralframework.osl.GameContext.STXGameContext()
        }
    }

    abstract class NonstopDebateDataContext(open val game: HopesPeakKillingGame): info.spiralframework.osl.GameContext()
    companion object {
        operator fun invoke(game: HopesPeakKillingGame): info.spiralframework.osl.GameContext.NonstopDebateDataContext {
            return when (game) {
                DR1 -> info.spiralframework.osl.GameContext.DR1NonstopDebateDataContext
                DR2 -> info.spiralframework.osl.GameContext.DR2NonstopDebateDataContext
                else -> info.spiralframework.osl.GameContext.CatchAllNonstopDebateDataContext(game)
            }
        }
    }
    object DR1NonstopDebateDataContext: info.spiralframework.osl.GameContext.NonstopDebateDataContext(DR1)
    object DR2NonstopDebateDataContext: info.spiralframework.osl.GameContext.NonstopDebateDataContext(DR2)
    object UnknownHopesPeakNonstopDebateDataContext: info.spiralframework.osl.GameContext.NonstopDebateDataContext(UnknownHopesPeakGame)
    open class CatchAllNonstopDebateDataContext(game: HopesPeakKillingGame): info.spiralframework.osl.GameContext.NonstopDebateDataContext(game)

}
