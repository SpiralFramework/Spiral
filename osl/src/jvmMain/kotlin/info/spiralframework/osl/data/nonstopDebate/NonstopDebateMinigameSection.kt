package info.spiralframework.osl.data.nonstopDebate

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.data.NonstopDebateSection

data class NonstopDebateMinigameSection(val section: NonstopDebateSection, val debateText: Array<LinEntry>?, val correct: Array<LinEntry>, val incorrect: Array<LinEntry>)
