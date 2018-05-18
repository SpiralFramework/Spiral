package org.abimon.spiral.mvc.gurren

import com.jakewharton.fliptables.FlipTable
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.objects.game.hpa.*
import org.abimon.spiral.core.objects.scripting.NonstopDebate
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.visi.collections.copyFrom
import org.abimon.visi.io.FileDataSource
import org.abimon.visi.io.errPrintln
import org.abimon.visi.io.relativePathFrom
import org.abimon.visi.io.relativePathTo
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.*

object GurrenUtils {
    val echo = Command("echo") { (params) ->
        println(params.copyFrom(1).joinToString(" "))
    }

    val portDr2Lin = Command("port_dr2_lin") { (params) ->
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

    val portOldOSL = Command("port_old_osl") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("Error: No file(s) provided")

        if (Gurren.game == null)
            return@Command errPrintln("Error: No game defined!")

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

                PrintStream(subFile).use { out ->
                    out.println("OSL Script")
                    out.print("Set Game To ")
                    when (Gurren.game) {
                        DR1 -> out.println("DR1")
                        DR2 -> out.println("DR2")
                        UDG -> out.println("UDG")
                        UnknownHopesPeakGame -> out.println("UNK")
                        else -> error("Unsupported game: ${Gurren.game}")
                    }

                    lines.forEach { line ->
                        if (line.trim().startsWith("0x2B1D|")) {
                            out.println(line.replace("0x2B1D\\|1, \\d+".toRegex(), "Word Command 1: ")
                                    .replace("0x2B1D\\|2, \\d+".toRegex(), "Word Command 2: ")
                                    .replace("0x2B1D\\|3, \\d+".toRegex(), "Word Command 3: "))
                        } else if (line.trim().startsWith("0x2B1E|")) {
                            out.println(line.replace("0x2B1E|", "Word String: "))
                        } else if (line.trim().startsWith("DR1 Wait For Input|")) {
                            out.println(line.replace("DR1 Wait For Input|", "0x3A|"))
                        } else if (line.trim().startsWith("DR1 Wait Frame|")) {
                            out.println(line.replace("DR1 Wait Frame|", "0x3B|"))
                        } else
                            out.println(line)
                    }
                }

                results.add(arrayOf(subFile relativePathTo file, lines.size.toString()))
            }

            println(FlipTable.of(arrayOf("File", "Lines"), results.toTypedArray()))
        }
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
        debateMap["sections"] = nonstop.sections.map { section -> section.data.mapIndexed { index, data -> (if (index in SpiralData.nonstopOpCodes) SpiralData.nonstopOpCodes[index] else "0x${index.toString(16)}") to data }.toMap() }

        SpiralData.YAML_MAPPER.writeValue(nonstopOutput, debateMap)
    }

    val batchOperation = Command("batch_operation") { (params) ->
        if (params.size < 4)
            return@Command errPrintln("Missing parameters (batch_operation [folder] [regex] [operation])")

        val folder = File(params[1])
        val regex = params[2].toRegex()
        val operation = params[3]

        if (!folder.isDirectory)
            return@Command errPrintln("$folder is not a folder")

        folder.walk().mapNotNull { file ->
            if (!file.isFile)
                return@mapNotNull null

            val path = (file relativePathFrom folder)
            if (path.matches(regex))
                return@mapNotNull file
            return@mapNotNull null
        }.joinToString("\n") { file ->
            val path = (file relativePathFrom folder)
            val fullPath = file.absolutePath

            operation
                    .replace("\$path_no_extension", path.substringBeforeLast('.', missingDelimiterValue = path))
                    .replace("\$path", path)
                    .replace("%path_no_extension", path.substringBeforeLast('.', missingDelimiterValue = path))
                    .replace("%path", path)
                    .replace("\$full_path_no_extension", fullPath.substringBeforeLast('.', missingDelimiterValue = path))
                    .replace("\$full_path", fullPath)
                    .replace("%full_path_no_extension", fullPath.substringBeforeLast('.', missingDelimiterValue = path))
                    .replace("%full_path", fullPath)
        }.run(this::runCommands)
    }

//    val modelTest = Command("model_test") {
//        val dir = File("/Users/undermybrella/Workspace/KSPIRAL/processing-v3/models/model/chara/stand_012_027/")
//        val modelFile = File(dir, "model.srdi")
//        val stand = ImageIO.read(File(dir, "stand_012_027.png"))
//        val model = SRDIModel(FileDataSource(modelFile))
//
//        val mesh = Area()
//        val uv = Area()
//
//        val w = stand.width
//        val h = stand.height
//
//        val addX = (model.meshes[0].vertices.minBy { (x) -> x }?.first ?: 0.0f) * -1
//        val addY = (model.meshes[0].vertices.minBy { (_, y) -> y }?.second ?: 0.0f) * -1
//
//        var minMeshX = Int.MAX_VALUE
//        var minMeshY = Int.MAX_VALUE
//        var minTexX = Int.MAX_VALUE
//        var minTexY = Int.MAX_VALUE
//
//        var maxMeshX = Int.MIN_VALUE
//        var maxMeshY = Int.MIN_VALUE
//        var maxTexX = Int.MIN_VALUE
//        var maxTexY = Int.MIN_VALUE
//
//        model.meshes[0].faces.forEach { (one, two, three) ->
//            try {
//                val u1 = model.meshes[0].uvs[one]
//                val u2 = model.meshes[0].uvs[two]
//                val u3 = model.meshes[0].uvs[three]
//
//                val v1 = model.meshes[0].vertices[one]
//                val v2 = model.meshes[0].vertices[two]
//                val v3 = model.meshes[0].vertices[three]
//
//                minTexX = minOf(minTexX, (u1.first * w).toInt())
//                minTexX = minOf(minTexX, (u2.first * w).toInt())
//                minTexX = minOf(minTexX, (u3.first * w).toInt())
//
//                minTexY = minOf(minTexY, (u1.second * h).toInt())
//                minTexY = minOf(minTexY, (u2.second * h).toInt())
//                minTexY = minOf(minTexY, (u3.second * h).toInt())
//
//                maxTexX = maxOf(maxTexX, (u1.first * w).toInt())
//                maxTexX = maxOf(maxTexX, (u2.first * w).toInt())
//                maxTexX = maxOf(maxTexX, (u3.first * w).toInt())
//
//                maxTexY = maxOf(maxTexY, (u1.second * h).toInt())
//                maxTexY = maxOf(maxTexY, (u2.second * h).toInt())
//                maxTexY = maxOf(maxTexY, (u3.second * h).toInt())
//
//                minMeshX = minOf(minMeshX, (v1.first * w).toInt())
//                minMeshX = minOf(minMeshX, (v2.first * w).toInt())
//                minMeshX = minOf(minMeshX, (v3.first * w).toInt())
//
//                minMeshY = minOf(minMeshY, (v1.second * w).toInt())
//                minMeshY = minOf(minMeshY, (v2.second * w).toInt())
//                minMeshY = minOf(minMeshY, (v3.second * w).toInt())
//
//                maxMeshX = maxOf(maxMeshX, (v1.first * w).toInt())
//                maxMeshX = maxOf(maxMeshX, (v2.first * w).toInt())
//                maxMeshX = maxOf(maxMeshX, (v3.first * w).toInt())
//
//                maxMeshY = maxOf(maxMeshY, (v1.second * w).toInt())
//                maxMeshY = maxOf(maxMeshY, (v2.second * w).toInt())
//                maxMeshY = maxOf(maxMeshY, (v3.second * w).toInt())
//
//                uv.add(Area(Polygon(intArrayOf((u1.first * w).toInt(), (u2.first * w).toInt(), (u3.first * w).toInt()), intArrayOf((u1.second * h).toInt(), (u2.second * h).toInt(), (u3.second * h).toInt()), 3)))
//                mesh.add(Area(Polygon(intArrayOf((v1.first * w).toInt(), (v2.first * w).toInt(), (v3.first * w).toInt()), intArrayOf((v1.second * w).toInt(), (v2.second * w).toInt(), (v3.second * w).toInt()), 3)))
//            } catch (ioob: IndexOutOfBoundsException) {
//                debug(ioob.exportStackTrace())
//                return@forEach
//            }
//        }
//
//        val meshWidth = (maxMeshX - minMeshX)
//        val meshHeight = (maxMeshY - minMeshY)
//
//        val textureWidth = (maxTexX - minTexX)
//        val textureHeight = (maxTexY - minTexY)
//
//        mesh.transform(AffineTransform.getTranslateInstance(addX.toDouble() * w, addY.toDouble() * h))
//
//        val modelImage = BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB)
//        val modelG = modelImage.createGraphics()
//
//        modelG.composite = AlphaComposite.Src
//        modelG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
//        modelG.color = Color.WHITE
//        modelG.fill(uv.createTransformedArea(AffineTransform.getTranslateInstance(-minTexX.toDouble(), -minTexY.toDouble())))
//
//        modelG.composite = AlphaComposite.SrcIn
//        modelG.color = Color.BLACK
//        modelG.fillRect(0, 0, modelImage.width, modelImage.height)
//        modelG.drawImage(stand, -minTexX, -minTexY, null)
//
//        ImageIO.write(modelImage, "PNG", File(dir, "model_image.png"))
//
//        run {
//            val img = BufferedImage(mesh.bounds.width + mesh.bounds.x, mesh.bounds.height + mesh.bounds.y, BufferedImage.TYPE_INT_ARGB)
//            val g = img.createGraphics()
//
//            g.composite = AlphaComposite.Src
//            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
//            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
//            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
//
//            g.color = Color.WHITE
//            g.fill(mesh.createTransformedArea(AffineTransform.getScaleInstance(-1.0, -1.0)).createTransformedArea(AffineTransform.getTranslateInstance(img.width.toDouble(), img.height.toDouble())))
//
//            g.composite = AlphaComposite.SrcIn
//            //g.drawImage(AffineTransformOp(AffineTransform.getScaleInstance(img.width.toDouble() / modelImage.width, img.height.toDouble() / modelImage.height), AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(modelImage, null), 0, 0, null)
//            g.drawImage(modelImage.antialias(), 0, 0, img.width, img.height, null)
//
//            g.dispose()
//
//            ImageIO.write(img.antialias(), "PNG", File(dir, "model.png"))
//        }
//
//        run {
//            val img = BufferedImage(modelImage.width, modelImage.height, BufferedImage.TYPE_INT_ARGB)
//            val g = img.createGraphics()
//
//            g.composite = AlphaComposite.Src
//            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
//            g.color = Color.WHITE
//            g.fill(
//                    mesh.createTransformedArea(AffineTransform.getScaleInstance(modelImage.width.toDouble() / img.width.toDouble(), modelImage.height.toDouble() / img.height.toDouble()))
//                            .createTransformedArea(AffineTransform.getScaleInstance(-1.0, -1.0))
//                            .createTransformedArea(AffineTransform.getTranslateInstance(modelImage.width.toDouble(), modelImage.height.toDouble()))
//            )
//
//            g.composite = AlphaComposite.SrcIn
//            //g.drawImage(AffineTransformOp(AffineTransform.getScaleInstance(meshWidth.toDouble() / textureWidth, meshHeight.toDouble() / textureHeight), AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(modelImage, null), 0, 0, null)
//            g.drawImage(modelImage, 0, 0, img.width, img.height, null)
//
//            g.dispose()
//
//            ImageIO.write(img, "PNG", File(dir, "model-downscale.png"))
//        }
//
//        run {
//            val img = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
//            val g = img.createGraphics()
//
//            g.fill(uv)
//
//            g.dispose()
//
//            ImageIO.write(img, "PNG", File(dir, "model-uv.png"))
//        }
//
//        run {
//            val img = BufferedImage(mesh.bounds.width + mesh.bounds.x, mesh.bounds.height + mesh.bounds.y, BufferedImage.TYPE_INT_ARGB)
//            val g = img.createGraphics()
//
//            g.fill(
//                    mesh.createTransformedArea(AffineTransform.getScaleInstance(-1.0, -1.0))
//                            .createTransformedArea(AffineTransform.getTranslateInstance(img.width.toDouble(), img.height.toDouble()))
//                            .createTransformedArea(AffineTransform.getScaleInstance(1.0, 1.0))
//            )
//
//            g.dispose()
//
//            ImageIO.write(img, "PNG", File(dir, "model-mesh.png"))
//        }
//    }

    val opCodes = Command("op_codes") {
        println((Gurren.game as? HopesPeakDRGame)?.opCodes?.entries?.sortedBy { (key) -> key }?.joinToString("\n") { (key, value) ->
            return@joinToString "[0x${key.toString(16)}] {${value.first.joinToString()}}, {${value.second}}"
        })
    }

    fun BufferedImage.antialias(): BufferedImage {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()

        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)

        g.drawImage(this, 0, 0, null)

        g.dispose()
        return img
    }

    fun runCommands(input: String) = runCommands(input.toByteArray(Charsets.UTF_8))
    fun runCommands(input: ByteArray) {
        val startingScope = SpiralModel.scope
        val startingAutoConfirm = SpiralModel.autoConfirm
        SpiralModel.scope = "> " to "default"
        SpiralModel.autoConfirm = true

        System.setIn(ByteArrayInputStream(input))

        while (Gurren.keepLooping) {
            try {
                SpiralModel.imperator.dispatch(InstanceOrder<String>("STDIN", scout = null, data = readLine()
                        ?: break)).isEmpty()
            } catch (th: Throwable) {
                th.printStackTrace()
            }
        }

        SpiralModel.scope = startingScope
        SpiralModel.autoConfirm = startingAutoConfirm
        System.setIn(FileInputStream(FileDescriptor.`in`))
    }
}
