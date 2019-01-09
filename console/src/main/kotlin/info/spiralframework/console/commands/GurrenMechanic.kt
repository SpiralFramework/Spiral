package info.spiralframework.console.commands

import info.spiralframework.base.forEachFiltered
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
import info.spiralframework.formats.archives.*
import info.spiralframework.formats.utils.copyToStream
import org.parboiled.Action
import org.parboiled.support.Var
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import java.util.zip.ZipFile

@Suppress("unused")
class GurrenMechanic(override val cockpit: Cockpit<*>) : CommandClass {
    companion object {
        val EXTRACTABLE_ARCHIVES = arrayOf(
                AWBFormat, CpkFormat, PakFormat,
                SpcFormat, SRDFormat, WadFormat,
                ZipFormat
        )

        val PERCENT_FORMAT = DecimalFormat("00.00")
    }

    /** Rules */
    open val separatorRule = makeRule { FirstOf(InlineWhitespace(), '=') }
    val extractRule = makeRule {
        val argsVar = Var<ExtractArgs>()
        Sequence(
                Action<Any> { argsVar.set(ExtractArgs()) },
                Localised("commands.mechanic.extract.extract"),
                ParamSeparator(),
                ExistingMechanicFilePath(),
                Action<Any> { argsVar.get().extractPath = pop() as? File; true },
                ZeroOrMore(
                        ParamSeparator(),
                        FirstOf(
                                Sequence(
                                        Localised("commands.mechanic.extract.filter"),
                                        ParamSeparator(),
                                        MechanicParameterNoEscapes(),
                                        Action<Any> { argsVar.get().filter = pop() as? String; true }
                                ),
                                Sequence(
                                        Localised("commands.mechanic.extract.dest_dir"),
                                        ParamSeparator(),
                                        MechanicFilePath(),
                                        Action<Any> { argsVar.get().destDir = pop() as? File; true }
                                ),
                                Sequence(
                                        Localised("commands.mechanic.extract.leave_compressed"),
                                        Action<Any> { argsVar.get().leaveCompressed = true; true }
                                )
                        )
                ),
                Action<Any> { push(argsVar.get()) }
        )
    }

    /** Commands */

    val extract = ParboiledSoldier(extractRule) { stack ->
        val args = (stack[0] as ExtractArgs).makeImmutable(defaultFilter = ".*", defaultLeaveCompressed = false)
        val regex = args.filter!!.toRegex()

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

        val files: Iterator<Pair<String, InputStream>>
        val totalCount: Int

        when (result) {
            null -> {
                printlnErrLocale("commands.mechanic.extract.err_no_format_for", args.extractPath)

                return@ParboiledSoldier FAILURE
            }

            is AWB -> {
                files = result.entries.iterator { entry -> entry.id.toString() to entry.inputStream }
                totalCount = result.entries.count { entry -> entry.id.toString().matches(regex) }
            }

            is CPK -> {
                files = if (args.leaveCompressed!!) {
                    result.files.iterator { entry -> entry.name to entry.rawInputStream }
                } else {
                    result.files.iterator { entry -> entry.name to entry.inputStream }
                }
                totalCount = result.files.count { entry -> entry.name.matches(regex) }
            }

            is Pak -> {
                files = result.files.iterator { entry -> entry.index.toString() to entry.inputStream }
                totalCount = result.files.count { entry -> entry.index.toString().matches(regex) }
            }

            is SPC -> {
                files = if (args.leaveCompressed!!) {
                    result.files.iterator { entry -> entry.name to entry.rawInputStream }
                } else {
                    result.files.iterator { entry -> entry.name to entry.inputStream }
                }
                totalCount = result.files.count { entry -> entry.name.matches(regex) }
            }

            //is SRD -> {}

            is WAD -> {
                files = result.files.iterator { entry -> entry.name to entry.inputStream }
                totalCount = result.files.count { entry -> entry.name.matches(regex) }
            }

            is ZipFile -> {
                files = result.entries().iterator { entry -> entry.name to result.getInputStream(entry) }
                totalCount = result.entries().asSequence().count { entry -> entry.name.matches(regex) }
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

        files.forEachFiltered({ pair -> pair.first.matches(regex) }) { (fileName, raw) ->
            val file = File(args.destDir, fileName)
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