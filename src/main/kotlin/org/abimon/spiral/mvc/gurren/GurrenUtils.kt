package org.abimon.spiral.mvc.gurren

import com.jakewharton.fliptables.FlipTable
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.visi.collections.copyFrom
import org.abimon.visi.io.errPrintln
import org.abimon.visi.io.relativePathTo
import java.io.File
import java.io.PrintStream

object GurrenUtils {
    val echo = Command("echo") { (params) ->
        println(params.copyFrom(1).joinToString(" "))
    }

    val portLin = Command("port_dr2_lin") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("Error: No file(s) provided")

        val results = ArrayList<Array<String>>()

        params.copyFrom(1).forEach { fileName ->
            val file = File(fileName)

            if (!file.exists())
                return@forEach

            file.walk().forEach walk@{ subFile ->
                if (subFile.isDirectory)
                    return@walk

                if (subFile.extension != "osl" && subFile.extension != "txt")
                    return@walk

                val lines = subFile.readLines()

                var waitForInput = 0
                var waitFrame = 0
                PrintStream(subFile).use { out ->
                    lines.forEach { line ->
                        if (line.trim().startsWith("Wait For Input DR1|")) {
                            out.println(line.replace("Wait For Input DR1|", "0x3A|"))
                            waitForInput++
                        } else if (line.trim().startsWith("Wait Frame DR1|")) {
                            out.println(line.replace("Wait Frame DR1|", "0x3B|"))
                            waitFrame++
                        } else
                            out.println(line)
                    }
                }

                results.add(arrayOf(subFile relativePathTo file, lines.size.toString(), waitForInput.toString(), waitFrame.toString()))
            }

            println(FlipTable.of(arrayOf("File", "Lines", "\"Wait For Input DR1\" Lines", "\"Wait Frame DR1\" Lines"), results.toTypedArray()))
        }
    }
}