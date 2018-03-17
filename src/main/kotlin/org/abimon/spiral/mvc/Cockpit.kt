package org.abimon.spiral.mvc

import org.abimon.imperator.impl.InstanceOrder
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.formats.fonts.V3SPCFont
import org.abimon.spiral.core.formats.images.SRDFormat
import org.abimon.spiral.modding.ModManager
import org.abimon.spiral.modding.PluginManager
import org.abimon.spiral.mvc.gurren.*
import java.io.*

fun main(args: Array<String>) = startupSpiral(args)

fun startupSpiral(args: Array<String>) {
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

    Gurren.checkForUpdates.turn(InstanceOrder("CHECK-FOR-UPDATE", scout = null, data = "check_for_update"))

    println("Initialising SPIRAL")

    val startingScope = SpiralModel.scope
    val startingAutoConfirm = SpiralModel.autoConfirm
    SpiralModel.scope = "> " to "default"
    SpiralModel.autoConfirm = true


    val baos = ByteArrayOutputStream()
    val out = PrintStream(baos)
    args.forEach { param ->
        if (param.startsWith("-Soperation=") || param.startsWith("cmd=")) {
            param.split('=', limit = 2).last().split('\n').forEach(out::println)
        } else if (param.startsWith("cmd_file=")) {
            val filename = param.split('=', limit = 2).last()
            val file = File(filename)

            if (file.exists())
                file.useLines { lines -> lines.filter { line -> line.isNotBlank() && !line.startsWith("#") && !line.startsWith("//") }.forEach(out::println) }
        }
    }

    System.setIn(ByteArrayInputStream(baos.toByteArray()))

    while(Gurren.keepLooping) {
        try {
            val unknown = SpiralModel.imperator.dispatch(InstanceOrder<String>("STDIN", scout = null, data = readLine() ?: break)).isEmpty()
            Thread.sleep(250)
        } catch (th: Throwable) {
            th.printStackTrace()
        }
    }

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

    SpiralModel.scope = startingScope
    SpiralModel.autoConfirm = startingAutoConfirm
    System.setIn(FileInputStream(FileDescriptor.`in`))

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