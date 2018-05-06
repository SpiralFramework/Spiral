package org.abimon.spiral.mvc.gurren

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.objects.archives.*
import org.abimon.spiral.core.objects.models.SRDIModel
import org.abimon.spiral.core.objects.models.collada.ColladaPojo
import org.abimon.spiral.modding.HookManager
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.visi.io.errPrintln
import org.abimon.visi.io.relativePathFrom
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

@Suppress("unused")
object GurrenFileOperation {
    private var backingFile: Any? = null
    private var backingFileList: List<Pair<String, () -> InputStream>>? = null

    val fileList: List<Pair<String, () -> InputStream>>
        get() = backingFileList
                ?: throw IllegalStateException("Attempt to get the archive while operating is null, this is a bug!")

    val info = Command("info", "file-operate") { (params) ->
        val regex = (if (params.size == 1) ".*" else params[1]).toRegex()

        val matching = fileList.filter { (name) -> name.matches(regex) }.map(Pair<String, () -> InputStream>::first)
        if (matching.isEmpty())
            errPrintln("No files matching ${regex.pattern}")
        else
            println(matching.joinToString("\n") { str -> "\t$str" })
    }

    val extractModel = Command("extract_model") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("Error: No output provided")

        val output = params[1]
        val models = (if (params.size <= 2) ".*" else params[2]).toRegex()

        val baseNames = fileList.map { (name) -> name.substringBeforeLast('.') }.distinct().filter(models::matches)

        when (output.substringAfterLast('.').toUpperCase()) {
            "DAE" -> {
                val modelName = baseNames.first { name -> fileList.any { (fileName) -> fileName == "$name.srd" } && fileList.any { (fileName) -> fileName == "$name.srdi" } }

                val srd = SRD(fileList.first { (name) -> name == "$modelName.srd" }.second)
                val srdi = SRDIModel(srd, fileList.first { (name) -> name == "$modelName.srdi" }.second)

                SpiralData.XML_MAPPER.writeValue(File(output), ColladaPojo(srdi))
            }
            "ZIP" -> {
                val modelNames = baseNames.filter { name -> fileList.any { (fileName) -> fileName == "$name.srd" } && fileList.any { (fileName) -> fileName == "$name.srdi" } }

                ZipOutputStream(FileOutputStream(File(output))).use { stream ->
                    for (modelName in modelNames) {
                        stream.putNextEntry(ZipEntry("$modelName.dae"))

                        val srd = SRD(fileList.first { (name) -> name == "$modelName.srd" }.second)
                        val srdi = SRDIModel(srd, fileList.first { (name) -> name == "$modelName.srdi" }.second)

                        SpiralData.XML_MAPPER.writeValue(stream, ColladaPojo(srdi))

                        stream.closeEntry()
                    }
                }
            }
        }
    }

    val extractTexture = Command("extract_texture") {

    }

    val exit = Command("exit", "file-operate") {
        SpiralModel.scope = "> " to "default"
        SpiralModel.fileOperation = null
    }

    val operateOnFile = Command("operate_file", "default") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("Error: No file specified")

        val file = File(params[1])

        if (file.exists()) {
            SpiralModel.fileOperation = file

            if (backingFile != null) {
                SpiralModel.scope = "[File Operation ${file.nameWithoutExtension}]|> " to "file-operate"
                println("Now operating on ${file.nameWithoutExtension}")
            }
        }
    }

    fun onArchiveChange(old: File?, new: File?, proceed: Boolean): Boolean {
        if (!proceed || new == null || !new.exists()) {
            backingFile = null
            backingFileList = null
            return false
        }

        if (new.isDirectory) {
            backingFile = new
            backingFileList = new.walkTopDown().map { file -> file relativePathFrom new to file::inputStream }.toList()
            return true
        }

        if (!new.isFile) {
            backingFile = null
            backingFileList = null
            return false
        }

        val ds = new::inputStream
        val pak = Pak(ds)

        if (pak != null) {
            backingFile = pak
            backingFileList = pak.files.map { entry -> entry.index.toString() to entry::inputStream }
            return true
        }

        val wad = WAD(ds)

        if (wad != null) {
            backingFile = wad
            backingFileList = wad.files.map { entry -> entry.name to entry::inputStream }
            return true
        }

        val cpk = try {
            CPK(ds)
        } catch (iae: IllegalArgumentException) {
            null
        }

        if (cpk != null) {
            backingFile = cpk
            backingFileList = cpk.files.map { entry -> "${entry.directoryName}/${entry.fileName}" to entry::inputStream }
            return true
        }

        val spc = try {
            SPC(ds)
        } catch (iae: IllegalArgumentException) {
            null
        }

        if (spc != null) {
            backingFile = spc
            backingFileList = spc.files.map { entry -> entry.name to entry::inputStream }
            return true
        }

        val zip = try {
            ZipFile(new)
        } catch (io: IOException) {
            null
        }

        if (zip != null) {
            backingFile = zip
            backingFileList = zip.entries().toList().map { entry -> entry.name to { zip.getInputStream(entry) } }
            return true
        }

        backingFile = null
        backingFileList = null
        return false
    }

    init {
        HookManager.BEFORE_FILE_OPERATING_CHANGE.add(SpiralData.BASE_PLUGIN to this::onArchiveChange)
        onArchiveChange(null, SpiralModel.fileOperation, true)
    }
}