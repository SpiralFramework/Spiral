package info.spiralframework.osl.data.nonstopDebate

import info.spiralframework.formats.data.NonstopDebateSection
import info.spiralframework.formats.scripting.lin.LinScript

data class NonstopDebateMinigameSection(val section: NonstopDebateSection, val debateText: Array<LinScript>?, val correct: Array<LinScript>, val incorrect: Array<LinScript>)
