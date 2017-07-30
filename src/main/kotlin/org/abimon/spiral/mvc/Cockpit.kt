package org.abimon.spiral.mvc

import org.abimon.imperator.impl.BasicImperator
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.imperator.impl.InstanceSoldier
import kotlin.reflect.full.memberProperties

fun main(args: Array<String>) {
    val imperator = BasicImperator()

    Gurren::class.memberProperties.filter { it.returnType.classifier == InstanceSoldier::class }.forEach { imperator.hireSoldier(it.get(Gurren) as? InstanceSoldier<*> ?: return@forEach) }

    while(Gurren.keepLooping) {
        print("> ")
        imperator.dispatch(InstanceOrder<String>("STDIN", scout = null, data = readLine() ?: break))
    }
}