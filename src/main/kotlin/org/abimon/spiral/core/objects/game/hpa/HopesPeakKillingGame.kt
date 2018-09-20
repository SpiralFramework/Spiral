package org.abimon.spiral.core.objects.game.hpa

/**
 * The Hope's Peak arc of killing games
 */
interface HopesPeakKillingGame: HopesPeakDRGame {
    val nonstopDebateOpCodeNames: Map<Int, String>
    val nonstopDebateSectionSize: Int

    val trialCameraNames: Array<String>
}