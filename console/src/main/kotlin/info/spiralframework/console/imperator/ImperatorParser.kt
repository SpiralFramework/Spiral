package info.spiralframework.console.imperator

import info.spiralframework.core.SpiralCoreData
import org.abimon.osl.AllButMatcher
import org.abimon.osl.SpiralParser
import org.parboiled.Action
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.support.Var
import java.io.File

@BuildParseTree
open class ImperatorParser(parboiled: Boolean) : SpiralParser(parboiled) {
    companion object {
        operator fun invoke(): ImperatorParser = Parboiled.createParser(ImperatorParser::class.java, true)
    }

    open fun Localised(str: String): Rule = FirstOf(
            IgnoreCase(SpiralCoreData.localise(str)),
            IgnoreCase(SpiralCoreData.localiseForEnglish(str))
    )

    open fun Parameter(): Rule {
        val str = Var<String>()

        return FirstOf(
                Sequence(
                        "\"",
                        Action<Any> { str.set("") },
                        Optional(
                                OneOrMore(
                                        FirstOf(
                                                Sequence(
                                                        "\\",
                                                        FirstOf(
                                                                Sequence(
                                                                        FirstOf(
                                                                                "\"",
                                                                                "\\",
                                                                                "/",
                                                                                "b",
                                                                                "f",
                                                                                "n",
                                                                                "r",
                                                                                "t"
                                                                        ),
                                                                        Action<Any> {
                                                                            when (match()) {
                                                                                "\"" -> str.set(str.get() + "\"")
                                                                                "\\" -> str.set(str.get() + "\\")
                                                                                "/" -> str.set(str.get() + "/")
                                                                                "b" -> str.set(str.get() + "\b")
                                                                                "f" -> str.set(str.get() + 0xC.toChar())
                                                                                "n" -> str.set(str.get() + "\n")
                                                                                "r" -> str.set(str.get() + "\r")
                                                                                "t" -> str.set(str.get() + "\t")
                                                                            }

                                                                            return@Action true
                                                                        }
                                                                ),
                                                                Sequence(
                                                                        "u",
                                                                        NTimes(4, Digit(16)),
                                                                        Action<Any> { str.set(str.get() + match().toInt(16).toChar()) }
                                                                )
                                                        )
                                                ),
                                                Sequence(
                                                        AllButMatcher(charArrayOf('\\', '"')),
                                                        Action<Any> { str.set(str.get() + match()) }
                                                )
                                        )
                                )
                        ),
                        Action<Any> { push(str.get()) },
                        "\""
                ),
                Sequence(
                        Action<Any> { str.set("") },
                        Optional(
                                OneOrMore(
                                        FirstOf(
                                                Sequence(
                                                        "\\",
                                                        FirstOf(
                                                                Sequence(
                                                                        FirstOf(
                                                                                "\"",
                                                                                "\\",
                                                                                "/",
                                                                                "b",
                                                                                "f",
                                                                                "n",
                                                                                "r",
                                                                                "t"
                                                                        ),
                                                                        Action<Any> {
                                                                            when (match()) {
                                                                                "\"" -> str.set(str.get() + "\"")
                                                                                "\\" -> str.set(str.get() + "\\")
                                                                                "/" -> str.set(str.get() + "/")
                                                                                "b" -> str.set(str.get() + "\b")
                                                                                "f" -> str.set(str.get() + 0xC.toChar())
                                                                                "n" -> str.set(str.get() + "\n")
                                                                                "r" -> str.set(str.get() + "\r")
                                                                                "t" -> str.set(str.get() + "\t")
                                                                            }

                                                                            return@Action true
                                                                        }
                                                                ),
                                                                Sequence(
                                                                        "u",
                                                                        NTimes(4, Digit(16)),
                                                                        Action<Any> { str.set(str.get() + match().toInt(16).toChar()) }
                                                                )
                                                        )
                                                ),
                                                Sequence(
                                                        AllButMatcher(whitespace.plus('\\')),
                                                        Action<Any> { str.set(str.get() + match()) }
                                                )
                                        )
                                )
                        ),
                        Action<Any> { push(str.get()) }
                )
        )
    }

    open fun ParameterNoEscapes(): Rule {
        val str = Var<String>()

        return FirstOf(
                Sequence(
                        "\"",
                        Action<Any> { str.set("") },
                        Optional(
                                OneOrMore(
                                        FirstOf(
                                                Sequence(
                                                        "\\",
                                                        "\"",
                                                        Action<Any> { str.set(str.get() + "\"") }
                                                ),
                                                Sequence(
                                                        AllButMatcher(charArrayOf('"')),
                                                        Action<Any> { str.set(str.get() + match()) }
                                                )
                                        )
                                )
                        ),
                        Action<Any> { push(str.get()) },
                        "\""
                ),
                Sequence(
                        Action<Any> { str.set("") },
                        Optional(
                                OneOrMore(
                                        FirstOf(
                                                Sequence(
                                                        "\\",
                                                        "\"",
                                                        Action<Any> { str.set(str.get() + "\"") }
                                                ),
                                                Sequence(
                                                        AllButMatcher(whitespace),
                                                        Action<Any> { str.set(str.get() + match()) }
                                                )
                                        )
                                )
                        ),
                        Action<Any> { push(str.get()) }
                )
        )
    }

    open fun FilePath(): Rule =
            Sequence(
                    ParameterNoEscapes(),
                    Action<Any> { push(File(pop().toString())) }
            )

    open fun ExistingFilePath(): Rule =
            Sequence(
                    FilePath(),
                    Action<Any> {
                        val file = pop() as? File ?: return@Action false
                        if (!file.exists())
                            return@Action false
                        push(file)
                    }
            )
}