package info.spiralframework.gui.jvm

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.KorneaResultConfig

fun main(args: Array<String>) {
    KorneaResult.defaultConfig = KorneaResultConfig(false)
    Lagann.main(args)
}