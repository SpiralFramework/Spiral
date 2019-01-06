package info.spiralframework.console.commands

import info.spiralframework.base.iterator
import info.spiralframework.base.printlnErrLocale
import info.spiralframework.base.printlnLocale
import info.spiralframework.console.Cockpit
import info.spiralframework.console.data.mechanic.ExtractArgs
import info.spiralframework.console.imperator.CommandClass
import info.spiralframework.console.imperator.ParboiledSoldier.Companion.FAILURE
import info.spiralframework.console.imperator.ParboiledSoldier.Companion.SUCCESS
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.archives.*
import info.spiralframework.formats.archives.WAD
import info.spiralframework.formats.utils.copyToStream
import org.parboiled.Action
import org.parboiled.support.Var
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

@Suppress("unused")
class GurrenMechanic(override val cockpit: Cockpit<*>) : CommandClass {
    companion object {
        val EXTRACTABLE_ARCHIVES = arrayOf(AWBFormat, CpkFormat, PakFormat, SpcFormat, SRDFormat, WadFormat, ZipFormat)

        val PERCENT_FORMAT = DecimalFormat("00.00")
    }

    /** Rules */
    open val separatorRule = makeRule { FirstOf(InlineWhitespace(), '=') }
    val extractRule = makeRule {
        val argsVar = Var<ExtractArgs>()
        Sequence(
                Action<Any> { argsVar.set(ExtractArgs()) },
                Optional("\""),
                Localised("commands.mechanic.extract.extract"),
                separatorRule,
                Optional("\""),
                ExistingFilePath(),
                Action<Any> { argsVar.get().extractPath = pop() as? File; true },
                Optional("\""),
                Optional("\""),
                ZeroOrMore(
                        InlineWhitespace(),
                        FirstOf(
                                Sequence(
                                        Optional("\""),
                                        Localised("commands.mechanic.extract.filter"),
                                        separatorRule,
                                        Optional("\""),
                                        Parameter(),
                                        Action<Any> { argsVar.get().filter = pop() as? String; true },
                                        Optional("\""),
                                        Optional("\"")
                                ),
                                Sequence(
                                        Optional("\""),
                                        Localised("commands.mechanic.extract.dest_dir"),
                                        separatorRule,
                                        Optional("\""),
                                        FilePath(),
                                        Action<Any> { argsVar.get().destDir = pop() as? File; true },
                                        Optional("\""),
                                        Optional("\"")
                                )
                        )
                ),
                Action<Any> { push(argsVar.get()) }
        )
    }

    /** Commands */

    val extract = ParboiledSoldier(extractRule) { stack ->
        val args = (stack[0] as ExtractArgs).makeImmutable(defaultFilter = ".*")

        if (args.extractPath == null) {
            printlnErrLocale("commands.mechanic.extract.err_no_extract")

            return@ParboiledSoldier FAILURE
        }

        if (!args.extractPath.exists()) {
            printlnErrLocale("errors.files.doesnt_exist", args.extractPath)

            return@ParboiledSoldier FAILURE
        }

        if (args.destDir == null) {
            printlnErrLocale("commands.mechanic.extract.err_no_dest_dir")

            return@ParboiledSoldier FAILURE
        }

        if (!args.destDir.exists() && !args.destDir.mkdirs()) {
            printlnErrLocale("errors.files.cant_create_dir", args.destDir)

            return@ParboiledSoldier FAILURE
        }

        val result = EXTRACTABLE_ARCHIVES.map { format -> format.read(source = args.extractPath::inputStream) }
                .filter(FormatResult<*>::didSucceed)
                .sortedBy(FormatResult<*>::chance)
                .firstOrNull()
                ?.obj

        val files: Iterator<Pair<File, InputStream>>
        val totalCount: Int

        when (result) {
            null -> {
                printlnErrLocale("commands.mechanic.extract.err_no_format_for", args.extractPath)

                return@ParboiledSoldier FAILURE
            }

            is WAD -> {
                files = result.files.iterator { entry -> File(args.destDir, entry.name) to entry.inputStream }
                totalCount = result.files.size
            }
            else -> {
                printlnErrLocale("commands.mechanic.extract.err_unk_format", result)

                return@ParboiledSoldier FAILURE
            }
        }

        val per = (100.0 / totalCount.toDouble())
        var last: Double = 0.0

        printlnLocale("commands.mechanic.extract.extracting_files", totalCount, args.destDir)

        val printOut: (Double) -> Unit = if (cockpit.args.ansiEnabled) { percent ->
            print("\r${PERCENT_FORMAT.format(percent)}%")
        } else { percent ->
            print("\r${PERCENT_FORMAT.format(percent)}%")
        }

        files.forEach { (file, raw) ->
            file.parentFile.mkdirs()

            raw.use { stream -> FileOutputStream(file).use(stream::copyToStream) }
            last += per
            printOut(last)
        }

        println()
        printlnLocale("commands.mechanic.extract.finished")

        return@ParboiledSoldier SUCCESS
    }
}