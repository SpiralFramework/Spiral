package org.abimon.spiral.core.objects

import org.abimon.spiral.core.objects.archives.*
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.game.hpa.HopesPeakKillingGame
import org.abimon.spiral.core.objects.models.RoomObject
import org.abimon.spiral.core.objects.scripting.*
import org.abimon.spiral.core.objects.text.CustomSTXT
import java.io.InputStream

fun UnsafePak(dataSource: () -> InputStream): Pak = Pak(dataSource)!!
fun UnsafeWAD(dataSource: () -> InputStream): WAD = WAD(dataSource)!!

fun UnsafeLin(game: HopesPeakDRGame, dataSource: () -> InputStream): Lin = Lin(game, dataSource)!!

fun UnsafeNonstopDebate(game: HopesPeakKillingGame, dataSource: () -> InputStream): NonstopDebate = NonstopDebate(game, dataSource)!!

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

fun customSTXT(init: CustomSTXT.() -> Unit): CustomSTXT {
    val customSTXT = CustomSTXT()
    customSTXT.init()
    return customSTXT
}

fun customRoomObject(init: RoomObject.() -> Unit): RoomObject {
    val roomObject = RoomObject(0, 0, 0, 0f, 0f, 0f, 0f, 0f, 0f, 0)
    roomObject.init()
    return roomObject
}

fun customNonstopDebate(init: CustomNonstopDebate.() -> Unit): CustomNonstopDebate {
    val customNonstop = CustomNonstopDebate()
    customNonstop.init()
    return customNonstop
}

fun utfTable(init: UTFTableInfo.() -> Unit): UTFTableInfo {
    val utfTableInfo = UTFTableInfo()
    utfTableInfo.init()
    return utfTableInfo
}