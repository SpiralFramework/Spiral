package info.spiralframework.formats.game.hpa

import info.spiralframework.formats.common.games.hpa.HopesPeakDRGame

/**
 * The Hope's Peak arc of killing games
 */
interface HopesPeakKillingGame: HopesPeakDRGame {
    val nonstopDebateOpCodeNames: Map<Int, String>
    val nonstopDebateSectionSize: Int

    val trialCameraNames: Array<String>
    val evidenceNames: Array<String>
}