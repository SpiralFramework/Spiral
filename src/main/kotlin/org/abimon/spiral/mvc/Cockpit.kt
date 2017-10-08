package org.abimon.spiral.mvc

import org.abimon.imperator.impl.BasicImperator
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.imperator.impl.InstanceSoldier
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.formats.SRDFormat
import org.abimon.spiral.core.objects.CustomSPC
import org.abimon.spiral.core.objects.CustomSRD
import org.abimon.spiral.mvc.gurren.Gurren
import org.abimon.spiral.mvc.gurren.GurrenOperation
import org.abimon.spiral.mvc.gurren.GurrenPatching
import org.abimon.visi.io.FileDataSource
import org.abimon.visi.lang.make
import java.io.File
import javax.imageio.ImageIO
import kotlin.reflect.full.memberProperties

fun main(args: Array<String>) {
    if(SpiralModel.purgeCache)
        CacheHandler.purge()
    SRDFormat.hook()

    val imperator = BasicImperator()
    val registerSoldiers: Any.() -> Unit = { this.javaClass.kotlin.memberProperties.filter { it.returnType.classifier == InstanceSoldier::class }.forEach { imperator.hireSoldier(it.get(this) as? InstanceSoldier<*> ?: return@forEach) } }

    Gurren.registerSoldiers()
    GurrenOperation.registerSoldiers()
    GurrenPatching.registerSoldiers()

    println("Initialising SPIRAL")

    args.forEach { param ->
        if(param.startsWith("-Soperation=")) {
            val unknown = imperator.dispatch(InstanceOrder<String>("STDIN", scout = null, data = param.split('=', limit = 2).last())).isEmpty()
            Thread.sleep(250)
            if(unknown)
                println("Unknown command")
        }
    }

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

    val hiyoko = ImageIO.read(File("processing-v3/hiyoko1.png"))
    val custom = CustomSRD(FileDataSource(File("processing-v3/otona1.srd")), FileDataSource(File("processing-v3/otona1.srdv")))
    custom.image("otona1.png", hiyoko)

    val srdCopy = File("processing-v3/output.srd")
    val srdvCopy = File("processing-v3/output.srdv")

    val (srdOut, srdIn) = CacheHandler.cacheStream()
    val (srdvOut, srdvIn) = CacheHandler.cacheStream()

    srdOut.use { srd -> srdvOut.use { srdv ->
        custom.patch(srd, srdv)
    } }

    srdCopy.outputStream().use(srdIn::pipe)
    srdvCopy.outputStream().use(srdvIn::pipe)

    val spc = make<CustomSPC> {
        file("otona1.srd", srdIn)
        file("otona1.srdv", srdvIn)
    }

    File("processing-v3/otona1.SPC").outputStream().use(spc::compile)

    if(SpiralModel.purgeCache)
        CacheHandler.purge() //Just in case shutdown hook doesn't go off
}