package org.abimon.spiral.mvc

import org.abimon.imperator.impl.InstanceOrder
import org.abimon.imperator.impl.InstanceSoldier
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.formats.SRDFormat
import org.abimon.spiral.modding.PluginManager
import org.abimon.spiral.mvc.gurren.Gurren
import org.abimon.spiral.mvc.gurren.GurrenOperation
import org.abimon.spiral.mvc.gurren.GurrenPatching
import org.abimon.spiral.mvc.gurren.GurrenPlugins
import kotlin.reflect.full.memberProperties

fun main(args: Array<String>) {
    if(SpiralModel.purgeCache)
        CacheHandler.purge()
    PluginManager.scanForPlugins()
    SRDFormat.hook()

    val registerSoldiers: Any.() -> Unit = { this.javaClass.kotlin.memberProperties.filter { it.returnType.classifier == InstanceSoldier::class }.forEach { SpiralModel.imperator.hireSoldier(it.get(this) as? InstanceSoldier<*> ?: return@forEach) } }

    Gurren.registerSoldiers()
    GurrenOperation.registerSoldiers()
    GurrenPatching.registerSoldiers()
    GurrenPlugins.registerSoldiers()

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