package info.spiralframework.formats.common.data


import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readNullTerminatedString
import info.spiralframework.base.common.io.readString
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.readInt64LE
import dev.brella.kornea.io.common.flow.int
import dev.brella.kornea.io.common.flow.withState
import dev.brella.kornea.toolkit.common.useAndFlatMap

@ExperimentalUnsignedTypes
class Dr1LocalisationBin private constructor(val stringIDs: Array<String>, val languages: Map<String, Language>) {
    data class Language(val languageIndex: Int, val languageName: String, val languageID: Int, val localisations: Array<String>)

    companion object {
        const val PREFIX = "formats.localisation_bin.dr1"
        const val NOT_ENOUGH_DATA_KEY = "$PREFIX.not_enough_data"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<Dr1LocalisationBin> = dataSource.openInputFlow().useAndFlatMap { flow -> invoke(context, flow) }
        suspend operator fun invoke(context: SpiralContext, flow: InputFlow): KorneaResult<Dr1LocalisationBin> {
            withFormats(context) {
                val flow = withState { int(flow) }
                val languageCount = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                val languages = Array(languageCount) {
                    val languageIndex = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val languageNameLength = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val languageName = flow.readString(len = languageNameLength, encoding = TextCharsets.UTF_8)
                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val languageID = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val localisationCount = flow.readInt64LE()?.toInt()
                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val localisationLengths = IntArray(localisationCount) {
                        flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    }
                    val localisations = Array(localisationCount) { i -> requireNotNull(flow.readNullTerminatedString(maxLen = localisationLengths[i], encoding = TextCharsets.UTF_8)) }

                    Language(languageIndex, languageName, languageID, localisations)
                }

                return KorneaResult.success(Dr1LocalisationBin(languages.first { lang -> lang.languageID == 0x00 }.localisations, languages.map { lang -> lang.languageName to lang }.toMap()))
            }
        }
    }
}