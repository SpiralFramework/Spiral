package info.spiralframework.formats

import info.spiralframework.base.binding.*
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.environment.registerAllModules
import info.spiralframework.formats.archives.*
import info.spiralframework.formats.data.NonstopDebate
import info.spiralframework.formats.data.NonstopDebateV3
import info.spiralframework.formats.common.games.hpa.HopesPeakDRGame
import info.spiralframework.formats.game.hpa.HopesPeakKillingGame
import info.spiralframework.formats.game.v3.V3
import info.spiralframework.formats.images.FontMap
import info.spiralframework.formats.models.RoomObject
import info.spiralframework.formats.scripting.*
import info.spiralframework.formats.text.CustomSTX
import info.spiralframework.formats.text.STX
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.formats.video.SFL

fun SpiralContext.AWB(dataSource: DataSource): AWB? = AWB(this, dataSource)
fun SpiralContext.CPK(dataSource: DataSource): CPK? = CPK(this, dataSource)
fun SpiralContext.Pak(overrideSanityChecks: Boolean = false, dataSource: DataSource): Pak? = Pak(this, overrideSanityChecks, dataSource)
fun SpiralContext.SPC(dataSource: DataSource): SPC? = SPC(this, dataSource)
fun SpiralContext.WAD(dataSource: DataSource): WAD? = WAD(this, dataSource)

fun SpiralContext.FontMap(dataSource: DataSource): FontMap? = FontMap(this, dataSource)

fun SpiralContext.Lin(game: HopesPeakDRGame, dataSource: DataSource): Lin? = Lin(this, game, dataSource)
fun SpiralContext.WordScript(game: V3, dataSource: DataSource): WordScriptFile? = WordScriptFile(this, game, dataSource)

fun SpiralContext.STX(dataSource: DataSource): STX? = STX(this, dataSource)

fun SpiralContext.SFL(dataSource: DataSource): SFL? = SFL(this, dataSource)

fun SpiralContext.NonstopDebate(game: HopesPeakKillingGame, dataSource: DataSource): NonstopDebate? = NonstopDebate(this, game, dataSource)
fun SpiralContext.NonstopDebateV3(game: V3, dataSource: DataSource): NonstopDebateV3? = NonstopDebateV3(this, game, dataSource)

fun SpiralContext.UnsafeAWB(dataSource: DataSource): AWB = AWB.unsafe(this, dataSource)
fun SpiralContext.UnsafeCPK(dataSource: DataSource): CPK = CPK.unsafe(this, dataSource)
fun SpiralContext.UnsafePak(overrideSanityChecks: Boolean = false, dataSource: DataSource): Pak = Pak.unsafe(this, overrideSanityChecks, dataSource)
fun SpiralContext.UnsafeSPC(dataSource: DataSource): SPC = SPC.unsafe(this, dataSource)
fun SpiralContext.UnsafeWAD(dataSource: DataSource): WAD = WAD.unsafe(this, dataSource)

fun SpiralContext.UnsafeFontMap(dataSource: DataSource): FontMap = FontMap.unsafe(this, dataSource)

fun SpiralContext.UnsafeLin(game: HopesPeakDRGame, dataSource: DataSource): Lin = Lin.unsafe(this, game, dataSource)
fun SpiralContext.UnsafeWordScript(game: V3, dataSource: DataSource): WordScriptFile = WordScriptFile.unsafe(this, game, dataSource)

fun SpiralContext.UnsafeSTX(dataSource: DataSource): STX = STX.unsafe(this, dataSource)

fun SpiralContext.UnsafeSFL(dataSource: DataSource): SFL = SFL.unsafe(this, dataSource)

fun SpiralContext.UnsafeNonstopDebate(game: HopesPeakKillingGame, dataSource: DataSource): NonstopDebate = NonstopDebate.unsafe(this, game, dataSource)
fun SpiralContext.UnsafeNonstopDebateV3(game: V3, dataSource: DataSource): NonstopDebateV3 = NonstopDebateV3.unsafe(this, game, dataSource)

fun customWAD(init: CustomWAD.() -> Unit): CustomWAD {
    val customWAD = CustomWAD()
    customWAD.init()
    return customWAD
}

fun customCPK(init: CustomCPK.() -> Unit): CustomCPK {
    val customCPK = CustomCPK()
    customCPK.init()
    return customCPK
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

@ExperimentalUnsignedTypes
suspend fun defaultSpiralContextWithFormats(): SpiralContext {
    val context = DefaultSpiralContext(DefaultSpiralLocale(), DefaultSpiralLogger("DefaultSpiral"), DefaultSpiralConfig(), DefaultSpiralEnvironment(), DefaultSpiralEventBus(), DefaultSpiralCacheProvider(), DefaultSpiralResourceLoader())
    context.addModuleProvider(SpiralModuleFormats())
    context.registerAllModules(context)
    return context
}