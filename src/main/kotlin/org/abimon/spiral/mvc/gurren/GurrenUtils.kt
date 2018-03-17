package org.abimon.spiral.mvc.gurren

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.objects.scripting.NonstopDebate
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.visi.collections.copyFrom
import org.abimon.visi.io.FileDataSource
import org.abimon.visi.io.errPrintln
import java.io.File

object GurrenUtils {
    val echo = Command("echo") { (params) ->
        println(params.copyFrom(1).joinToString(" "))
    }

    val extractNonstop = Command("extract_nonstop") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("Error: no file provided")

        val nonstopFile = File(params[1])
        val nonstopOutput = File(nonstopFile.absolutePath.replace(".dat", ".yaml"))

        val nonstop = NonstopDebate(FileDataSource(nonstopFile))

        if (nonstop.sections.isEmpty())
            return@Command errPrintln("Error: $nonstopFile is not a nonstop debate file")

        val debateMap: MutableMap<String, Any> = HashMap()

        debateMap["duration"] = nonstop.secondsForDebate
        debateMap["sections"] = nonstop.sections.map { section -> section.data.mapIndexed { index, data -> (if(index in SpiralData.nonstopOpCodes) SpiralData.nonstopOpCodes[index] else "0x${index.toString(16)}") to data }.toMap() }

        SpiralData.YAML_MAPPER.writeValue(nonstopOutput, debateMap)
    }
}