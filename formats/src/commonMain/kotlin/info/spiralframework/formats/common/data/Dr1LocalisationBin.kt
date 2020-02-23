package info.spiralframework.formats.common.data


import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readNullTerminatedString
import info.spiralframework.base.common.io.readString
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.InputFlow

@ExperimentalUnsignedTypes
class Dr1LocalisationBin {
    data class Language(val languageName: String, val languageID: Int, val localisations: Array<String>)

    companion object {
        const val PREFIX = "formats.localisation_bin.dr1"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): Dr1LocalisationBin? = dataSource.useInputFlow { flow -> invoke(context, flow) }
        suspend operator fun invoke(context: SpiralContext, flow: InputFlow): Dr1LocalisationBin? {
            try {
                return unsafe(context, flow)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("$PREFIX.invalid", flow, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): Dr1LocalisationBin = requireNotNull(dataSource.useInputFlow { flow -> unsafe(context, flow) })
        suspend fun unsafe(context: SpiralContext, flow: InputFlow): Dr1LocalisationBin {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("$PREFIX.not_enough_data") }

                val languageCount = requireNotNull(flow.readInt64LE(), notEnoughData).toInt()
                val languages = Array(languageCount) {
                    val languageNameLength = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val languageName = requireNotNull(flow.readString(len = languageNameLength, encoding = TextCharsets.UTF_8), notEnoughData)
                    val languageID = requireNotNull(flow.readInt32LE(), notEnoughData)

                    val localisationCount = requireNotNull(flow.readInt64LE(), notEnoughData).toInt()
                    val localisationLengths = IntArray(localisationCount) { requireNotNull(flow.readInt32LE(), notEnoughData) }
                    val localisations = Array(localisationCount) { i -> requireNotNull(flow.readNullTerminatedString(maxLen = localisationLengths[i], encoding = TextCharsets.UTF_8)) }


                }
            }
        }
    }
}