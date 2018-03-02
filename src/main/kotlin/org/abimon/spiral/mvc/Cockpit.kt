package org.abimon.spiral.mvc

import org.abimon.imperator.impl.InstanceOrder
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.fonts.V3SPCFont
import org.abimon.spiral.core.formats.images.SRDFormat
import org.abimon.spiral.core.utils.DataMapper
import org.abimon.spiral.modding.ModManager
import org.abimon.spiral.modding.PluginManager
import org.abimon.spiral.mvc.gurren.*

fun main(args: Array<String>) = startupSpiral(args)

fun startupSpiral(args: Array<String>) {
    DataMapper.byteArrayToMap = { byteArray -> SpiralData.MAPPER.readValue(byteArray, Map::class.java).mapKeys { (key) -> key.toString() } }
    DataMapper.stringToMap = { string -> SpiralData.MAPPER.readValue(string, Map::class.java).mapKeys { (key) -> key.toString() } }
    DataMapper.fileToMap = { file -> SpiralData.MAPPER.readValue(file, Map::class.java).mapKeys { (key) -> key.toString() } }
    DataMapper.streamToMap = { stream -> SpiralData.MAPPER.readValue(stream, Map::class.java).mapKeys { (key) -> key.toString() } }

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

    Gurren.checkForUpdates.turn(InstanceOrder("CHECK-FOR-UPDATE", scout = null, data = "check_for_update"))

    println("Initialising SPIRAL")

    args.forEach { param ->
        if(param.startsWith("-Soperation=")) {
            val unknown = SpiralModel.imperator.dispatch(InstanceOrder<String>("STDIN", scout = null, data = param.split('=', limit = 2).last())).isEmpty()
            Thread.sleep(250)
            if(unknown)
                println("Unknown command")
        }
    }

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