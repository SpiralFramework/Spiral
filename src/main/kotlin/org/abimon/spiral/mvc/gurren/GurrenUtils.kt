package org.abimon.spiral.mvc.gurren

import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.visi.collections.copyFrom

object GurrenUtils {
    val echo = Command("echo") { (params) ->
        println(params.copyFrom(1).joinToString(" "))
    }
}