package org.abimon.spiral.core.objects

import org.abimon.spiral.core.objects.archives.*
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.scripting.CustomLin
import org.abimon.spiral.core.objects.scripting.CustomWordScript
import org.abimon.spiral.core.objects.scripting.Lin
import java.io.InputStream

fun UnsafePak(dataSource: () -> InputStream): Pak = Pak(dataSource)!!
fun UnsafeWAD(dataSource: () -> InputStream): WAD = WAD(dataSource)!!

fun UnsafeLin(game: HopesPeakDRGame, dataSource: () -> InputStream): Lin = Lin(game, dataSource)!!

fun customWAD(init: CustomWAD.() -> Unit): CustomWAD {
    val customWAD = CustomWAD()
    customWAD.init()
    return customWAD
}

fun customPak(init: CustomPak.() -> Unit): CustomPak {
    val customPak = CustomPak()
    customPak.init()
    return customPak
}

fun customLin(init: CustomLin.() -> Unit): CustomLin {
    val customLin = CustomLin()
    customLin.init()
    return customLin
}

fun customSPC(init: CustomSPC.() -> Unit): CustomSPC {
    val customSPC = CustomSPC()
    customSPC.init()
    return customSPC
}

fun customWordScript(init: CustomWordScript.() -> Unit): CustomWordScript {
    val customWordScript = CustomWordScript()
    customWordScript.init()
    return customWordScript
}