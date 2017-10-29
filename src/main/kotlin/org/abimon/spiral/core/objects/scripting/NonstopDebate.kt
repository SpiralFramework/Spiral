package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.readNumber
import org.abimon.spiral.util.debug
import org.abimon.visi.io.DataSource

/**
 * Data file for the nonstop debates
 */
class NonstopDebate(val dataSource: DataSource, sectionSize: Int? = null) {
    val secondsForDebate: Int
    val bytesPerSection: Int
    val sections = ArrayList<NonstopSection>()

    init {
        val stream = dataSource.inputStream

        try {
            secondsForDebate = stream.readNumber(2, unsigned = true).toInt() * 2

            val numberOfSections = stream.readNumber(2, unsigned = true)
            bytesPerSection = sectionSize ?: (dataSource.size / numberOfSections).toInt()

            if(bytesPerSection % 2 == 1)
                throw IllegalArgumentException("${dataSource.location} is an invalid/corrupt nonstop debate file (bytes per section is not even; is $bytesPerSection)")

            if(bytesPerSection != 60 && bytesPerSection != 68) debug("[Nonstop Debate ${dataSource.location}] Abnormal bytes per section of $bytesPerSection; be wary")

            val entriesPerSection = bytesPerSection / 2
            for(sectionNumber in 0 until numberOfSections) {
                val section = NonstopSection(entriesPerSection)

                for(i in 0 until entriesPerSection)
                    section[i] = stream.readNumber(2, unsigned = true).toInt()

                sections.add(section)
            }

            stream.close()
        }
        catch(illegal: IllegalArgumentException) {
            stream.close()
            throw illegal
        }
    }
}