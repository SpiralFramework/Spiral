package org.abimon.spiral.mvc

import org.abimon.imperator.impl.InstanceOrder
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.fonts.V3SPCFont
import org.abimon.spiral.core.formats.images.SRDFormat
import org.abimon.spiral.core.utils.DataHandler
import org.abimon.spiral.core.utils.md5Hash
import org.abimon.spiral.modding.ModManager
import org.abimon.spiral.modding.PluginManager
import org.abimon.spiral.mvc.gurren.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

fun main(args: Array<String>) = startupSpiral(args)

fun startupSpiral(args: Array<String>) {
    DataHandler.byteArrayToMap = { byteArray -> SpiralData.MAPPER.readValue(byteArray, Map::class.java).mapKeys { (key) -> key.toString() } }
    DataHandler.stringToMap = { string -> SpiralData.MAPPER.readValue(string, Map::class.java).mapKeys { (key) -> key.toString() } }
    DataHandler.fileToMap = { file -> SpiralData.MAPPER.readValue(file, Map::class.java).mapKeys { (key) -> key.toString() } }
    DataHandler.streamToMap = { stream -> SpiralData.MAPPER.readValue(stream, Map::class.java).mapKeys { (key) -> key.toString() } }

    if(SpiralModel.purgeCache)
        CacheHandler.purge()

    ModManager.scanForMods()
    PluginManager.scanForPlugins()

    SRDFormat.hook()
    V3SPCFont.hook()

    SpiralModel.imperator.hireSoldiers(Gurren)
    SpiralModel.imperator.hireSoldiers(GurrenOperation)
    SpiralModel.imperator.hireSoldiers(GurrenPatching)
    SpiralModel.imperator.hireSoldiers(GurrenPlugins)
    SpiralModel.imperator.hireSoldiers(GurrenModding)
    SpiralModel.imperator.hireSoldiers(GurrenUtils)
    SpiralModel.imperator.hireSoldiers(GurrenFileOperation)

    val maxBuild = Gurren.dir.walk().filter { file -> file.name.matches("SPIRAL-\\d+.jar".toRegex()) }.maxBy { file -> Gurren.buildFor(file.inputStream().md5Hash()) }

    if (maxBuild == null || maxBuild.name == Gurren.jarFile.name)
        Gurren.checkForUpdates.turn(InstanceOrder("CHECK-FOR-UPDATE", scout = null, data = "check_for_update"))
    else
        println("SPIRAL version ${Gurren.version}; build ${Gurren.currentBuild} - Downloaded file $maxBuild is on build ${Gurren.buildFor(maxBuild.inputStream().md5Hash())}")

    println("Initialising SPIRAL")

    val baos = ByteArrayOutputStream()
    val out = PrintStream(baos)
    args.forEach { param ->
        if (param.startsWith("-Soperation=") || param.startsWith("cmd=")) {
            param.split('=', limit = 2).last().replace("\\n", "\n").split('\n').forEach(out::println)
        } else if (param.startsWith("cmd_file=")) {
            val filename = param.split('=', limit = 2).last().replace("\\n", "\n")
            val file = File(filename)

            if (file.exists())
                file.useLines { lines -> lines.filter { line -> line.isNotBlank() && !line.startsWith("#") && !line.startsWith("//") }.forEach(out::println) }
        }
    }

    GurrenUtils.runCommands(baos.toByteArray())

//    args.forEach { param ->
//        if(param.startsWith("-Soperation=") || param.startsWith("cmd=")) {
//            val unknown = SpiralModel.imperator.dispatch(InstanceOrder<String>("STDIN", scout = null, data = param.split('=', limit = 2).last())).isEmpty()
//            Thread.sleep(250)
//            if(unknown)
//                println("Unknown command")
//        } else if (param.startsWith("cmd_file=")) {
//            val filename = param.split('=', limit = 2).last()
//            val file = File(filename)
//
//            if(file.exists())
//                file.useLines { lines -> lines.forEach { line ->
//                    val unknown = SpiralModel.imperator.dispatch(InstanceOrder<String>("STDIN", scout = null, data = line)).isEmpty()
//                    Thread.sleep(250)
//                    if(unknown)
//                        println("Unknown command")
//                }
//                }
//        }
//    }

    while(Gurren.keepLooping) {
        try {
            print(SpiralModel.scope.first)
            val unknown = SpiralModel.imperator.dispatch(InstanceOrder<String>("STDIN", scout = null, data = readLine() ?: break)).isEmpty()
            Thread.sleep(250)
            if(unknown)
                println("Unknown command")
        } catch (th: Throwable) {
            th.printStackTrace()
        }
    }

    if(SpiralModel.purgeCache)
        CacheHandler.purge() //Just in case shutdown hook doesn't go off
}