package org.abimon.spiral.core.objects

import org.abimon.spiral.core.objects.archives.CustomWAD
import org.abimon.spiral.core.objects.archives.Pak
import org.abimon.spiral.core.objects.archives.WAD
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
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