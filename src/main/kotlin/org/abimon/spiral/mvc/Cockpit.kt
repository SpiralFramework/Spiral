package org.abimon.spiral.mvc

import org.abimon.imperator.impl.BasicImperator
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.imperator.impl.InstanceSoldier
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.formats.SRDFormat
import org.abimon.spiral.mvc.gurren.Gurren
import org.abimon.spiral.mvc.gurren.GurrenOperation
import kotlin.reflect.full.memberProperties

fun main(args: Array<String>) {
    if(SpiralModel.purgeCache)
        CacheHandler.purge()
    SRDFormat.hook()

    val imperator = BasicImperator()
    val registerSoldiers: Any.() -> Unit = { this.javaClass.kotlin.memberProperties.filter { it.returnType.classifier == InstanceSoldier::class }.forEach { imperator.hireSoldier(it.get(this) as? InstanceSoldier<*> ?: return@forEach) } }

    Gurren.registerSoldiers()
    GurrenOperation.registerSoldiers()

    println("Initialising SPIRAL")
    while(Gurren.keepLooping) {
        try {
            print(SpiralModel.scope.first)
            val unknown = imperator.dispatch(InstanceOrder<String>("STDIN", scout = null, data = readLine() ?: break)).isEmpty()
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