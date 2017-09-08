package org.abimon.spiral.mvc

import org.abimon.imperator.impl.BasicImperator
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.imperator.impl.InstanceSoldier
import org.abimon.spiral.mvc.gurren.Gurren
import org.abimon.spiral.mvc.gurren.GurrenOperation
import kotlin.reflect.full.memberProperties

fun main(args: Array<String>) {
    val imperator = BasicImperator()
    val registerSoldiers: Any.() -> Unit = { this.javaClass.kotlin.memberProperties.filter { it.returnType.classifier == InstanceSoldier::class }.forEach { imperator.hireSoldier(it.get(this) as? InstanceSoldier<*> ?: return@forEach) } }

    Gurren.registerSoldiers()
    GurrenOperation.registerSoldiers()

    println("Initialising SPIRAL")
    while(Gurren.keepLooping) {
        try {
            print(SpiralModel.scope.first)
            imperator.dispatch(InstanceOrder<String>("STDIN", scout = null, data = readLine() ?: break))
            Thread.sleep(250)
        } catch (th: Throwable) {
            th.printStackTrace()
        }
    }
}