package info.spiralframework.console

import info.spiralframework.console.data.ParameterParser
import info.spiralframework.console.eventbus.CommandClass
import org.parboiled.parserunners.BasicParseRunner
import org.parboiled.support.ParsingResult
import java.io.File

typealias Runner<T> = BasicParseRunner<T>
typealias Result = ParsingResult<*>

class CommandBuilders(override val parameterParser: ParameterParser): CommandClass {
    val booleanRule = makeRule { Boolean() }
    val booleanRunner = Runner<Boolean>(booleanRule)

    val filterRule = makeRule { MechanicFilter() }
    val filterRunner = Runner<Regex>(filterRule)

    val filePathRule = makeRule { MechanicFilePath() }
    val filePathRunner = Runner<File>(filePathRule)

    val parameterRule = makeRule { MechanicParameter() }
    val parameterRunner = Runner<String>(parameterRule)

    fun boolean(input: String? = readLine()): Boolean? = booleanRunner[input]
    fun filter(input: String? = readLine()): Regex? = filterRunner[input]
    fun filePath(input: String? = readLine()): File? = filePathRunner[input]
    fun parameter(input: String? = readLine()): String? = parameterRunner[input]

    operator fun <T> Runner<T>.get(input: String?): T? = input?.takeIf(String::isNotBlank)?.let(this::run)?.takeIf(Result::matched)?.resultValue
}