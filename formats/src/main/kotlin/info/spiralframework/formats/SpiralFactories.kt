package info.spiralframework.formats

import info.spiralframework.formats.archives.*
import info.spiralframework.formats.data.NonstopDebate
import info.spiralframework.formats.data.NonstopDebateV3
import info.spiralframework.formats.game.hpa.HopesPeakDRGame
import info.spiralframework.formats.game.hpa.HopesPeakKillingGame
import info.spiralframework.formats.game.v3.V3
import info.spiralframework.formats.images.FontMap
import info.spiralframework.formats.models.RoomObject
import info.spiralframework.formats.scripting.*
import info.spiralframework.formats.text.CustomSTX
import info.spiralframework.formats.text.STX
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.formats.video.SFL

fun UnsafeAWB(dataSource: DataSource): AWB = AWB.unsafe(dataSource)
fun UnsafeCPK(dataSource: DataSource): CPK = CPK.unsafe(dataSource)
fun UnsafePak(overrideSanityChecks: Boolean = false, dataSource: DataSource): Pak = Pak.unsafe(overrideSanityChecks, dataSource)
fun UnsafeSPC(dataSource: DataSource): SPC = SPC.unsafe(dataSource)
fun UnsafeWAD(dataSource: DataSource): WAD = WAD.unsafe(dataSource)

fun UnsafeFontMap(dataSource: DataSource): FontMap = FontMap.unsafe(dataSource)

fun UnsafeLin(game: HopesPeakDRGame, dataSource: DataSource): Lin = Lin.unsafe(game, dataSource)
fun UnsafeWordScript(game: V3, dataSource: DataSource): WordScriptFile = WordScriptFile.unsafe(game, dataSource)

fun UnsafeSTX(dataSource: DataSource): STX = STX.unsafe(dataSource)

fun UnsafeSFL(dataSource: DataSource): SFL = SFL.unsafe(dataSource)

fun UnsafeNonstopDebate(game: HopesPeakKillingGame, dataSource: DataSource): NonstopDebate = NonstopDebate.unsafe(game, dataSource)
fun UnsafeNonstopDebateV3(game: V3, dataSource: DataSource): NonstopDebateV3 = NonstopDebateV3.unsafe(game, dataSource)

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

fun customSTXT(init: CustomSTX.() -> Unit): CustomSTX {
    val customSTXT = CustomSTX()
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