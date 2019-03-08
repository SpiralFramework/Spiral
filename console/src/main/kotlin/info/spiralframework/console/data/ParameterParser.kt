package info.spiralframework.console.data

import info.spiralframework.base.SpiralLocale
import info.spiralframework.console.data.errors.LocaleError
import info.spiralframework.osl.SpiralParser
import org.parboiled.Action
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.annotations.Cached
import org.parboiled.support.Var
import java.io.File

@BuildParseTree
open class ParameterParser(parboiled: Boolean) : SpiralParser(parboiled) {
    companion object {
        const val MECHANIC_SEPARATOR = '\u001D'
        operator fun invoke(): ParameterParser = Parboiled.createParser(ParameterParser::class.java, true)
    }

    @Cached
    open fun Localised(str: String): Rule = FirstOf(
            IgnoreCase(SpiralLocale.localise(str)),
            IgnoreCase(SpiralLocale.localiseForEnglish(str))
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
                                                        info.spiralframework.osl.AllButMatcher(charArrayOf('\\', '"')),
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
                                                        info.spiralframework.osl.AllButMatcher(whitespace.plus('\\')),
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
                                                        info.spiralframework.osl.AllButMatcher(charArrayOf('"')),
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
                                                        info.spiralframework.osl.AllButMatcher(whitespace),
                                                        Action<Any> { str.set(str.get() + match()) }
                                                )
                                        )
                                )
                        ),
                        Action<Any> { push(str.get()) }
                )
        )
    }

    open fun MechanicParameter(): Rule {
        val str = Var<String>()

        return Sequence(
                Action<Any> { str.set("") },
                Optional('"'),
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
                                                info.spiralframework.osl.AllButMatcher(charArrayOf('\\', MECHANIC_SEPARATOR)),
                                                Action<Any> { str.set(str.get() + match()) }
                                        )
                                )
                        )
                ),
                Optional('"'),
                Action<Any> { push(str.get()) }
        )
    }

    open fun MechanicParameterNoEscapes(): Rule {
        val str = Var<String>()

        return Sequence(
                Action<Any> { str.set("") },
                Optional('"'),
                Optional(
                        OneOrMore(
                                FirstOf(
                                        Sequence(
                                                "\\",
                                                "\"",
                                                Action<Any> { str.set(str.get() + "\"") }
                                        ),
                                        Sequence(
                                                info.spiralframework.osl.AllButMatcher(charArrayOf('"', MECHANIC_SEPARATOR)),
                                                Action<Any> { str.set(str.get() + match()) }
                                        )
                                )
                        )
                ),
                Optional('"'),
                Action<Any> { push(str.get()) }
        )
    }

    open fun FilePath(): Rule =
            Sequence(
                    ParameterNoEscapes(),
                    Action<Any> { push(File(pop().toString())) }
            )

    open fun MechanicFilePath(): Rule =
            Sequence(
                    MechanicParameterNoEscapes(),
                    Action<Any> { push(File(pop().toString())) }
            )

    open fun ExistingMechanicFilePath(): Rule =
            Sequence(
                    MechanicParameterNoEscapes(),
                    Action<Any> {
                        val str = pop().toString()
                        val file = File(str)
                        if (file.exists()) {
                            return@Action push(file)
                        } else {
                            context.parseErrors.add(LocaleError(context, "errors.files.doesnt_exist", str))
                            return@Action false
                        }
                    }
            )

    open fun ExistingFilePath(): Rule {
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
                                                        info.spiralframework.osl.AllButMatcher(charArrayOf('"')),
                                                        Action<Any> { str.set(str.get() + match()) }
                                                )
                                        )
                                )
                        ),
                        Action<Any> { context ->
                            File(str.get()).let { file ->
                                if (file.exists()) {
                                    push(file)
                                } else {
                                    context.parseErrors.add(LocaleError(context, "errors.files.doesnt_exist", str.get()))
                                    false
                                }
                            }
                        },
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
                                                        info.spiralframework.osl.AllButMatcher(whitespace),
                                                        Action<Any> { str.set(str.get() + match()) }
                                                ),
                                                Sequence(
                                                        Action<Any> { !File(str.get()).let { file -> file.exists() || file.isFile } },
                                                        Action<Any> { File(str.get().substringBeforeLast('/')).let { file -> file.exists() && file.listFiles()?.any { subfile -> subfile.absolutePath.contains(str.get()) } ?: false } },
                                                        OneOrMore(AnyOf(whitespace)),
                                                        Action<Any> { str.set(str.get() + match()) }
                                                )
                                        )
                                )
                        ),
                        Action<Any> { context ->
                            File(str.get()).let { file ->
                                if (file.exists()) {
                                    push(file)
                                } else {
                                    context.parseErrors.add(LocaleError(context, "errors.files.doesnt_exist", str.get()))
                                    false
                                }
                            }
                        }
                )
        )
    }

    open fun ParamSeparator(): Rule = IgnoreCase(MECHANIC_SEPARATOR)

    val TRUE_NAMES = arrayOf("yes", "true", "affirmative")
    val FALSE_NAMES = arrayOf("no", "false", "negative")
    open fun Boolean(): Rule = FirstOf(
            Sequence(FirstOf(TRUE_NAMES), Action<Any> { push(true) }),
            Sequence(FirstOf(FALSE_NAMES), Action<Any> { push(false) })
    )

    open fun Filter(): Rule =
            FirstOf(
                    Sequence("*", Action<Any> { push(".*".toRegex()) }),
                    Sequence(
                            ParameterNoEscapes(),
                            Action<Any> {
                                val regex = pop() as String
                                return@Action kotlin.runCatching { regex.toRegex() }.getOrNull()?.let(this::push) ?: false
                            }
                    )
            )

    open fun MechanicFilter(): Rule =
            FirstOf(
                    Sequence("*", Action<Any> { push(".*".toRegex()) }),
                    Sequence(
                            MechanicParameterNoEscapes(),
                            Action<Any> {
                                val regex = pop() as String
                                return@Action kotlin.runCatching { regex.toRegex() }.getOrNull()?.let(this::push) ?: false
                            }
                    )
            )
}