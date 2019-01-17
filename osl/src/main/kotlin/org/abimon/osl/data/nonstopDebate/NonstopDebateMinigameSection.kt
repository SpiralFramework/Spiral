package org.abimon.osl.data.nonstopDebate

import org.abimon.spiral.core.objects.scripting.NonstopDebateSection
import org.abimon.spiral.core.objects.scripting.lin.LinScript

data class NonstopDebateMinigameSection(val section: NonstopDebateSection, val debateText: Array<LinScript>?, val correct: Array<LinScript>, val incorrect: Array<LinScript>)