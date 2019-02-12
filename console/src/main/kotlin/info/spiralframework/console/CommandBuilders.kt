package info.spiralframework.console

import info.spiralframework.console.imperator.CommandClass
import org.parboiled.parserunners.BasicParseRunner
import org.parboiled.support.ParsingResult
import java.io.File

typealias Runner<T> = BasicParseRunner<T>
typealias Result = ParsingResult<*>

class CommandBuilders(override val cockpit: Cockpit<*>): CommandClass {
    val booleanRule = makeRule { Boolean() }
    val booleanRunner = Runner<Boolean>(booleanRule)

    val filterRule = makeRule { MechanicFilter() }
    val filterRunner = Runner<Regex>(filterRule)

    val filePathRule = makeRule { MechanicFilePath() }
    val filePathRunner = Runner<File>(filePathRule)

    fun boolean(input: String? = readLine()): Boolean? = booleanRunner[input]
    fun filter(input: String? = readLine()): Regex? = filterRunner[input]
    fun filePath(input: String? = readLine()): File? = filePathRunner[input]

    operator fun <T> Runner<T>.get(input: String?): T? = input?.takeIf(String::isNotBlank)?.let(this::run)?.takeIf(Result::matched)?.resultValue
}