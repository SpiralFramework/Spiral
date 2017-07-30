package org.abimon.spiral.mvc

import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

object SpiralModel {
    val wads: MutableSet<File> = ConcurrentSkipListSet()
    var operating: File? = null


}