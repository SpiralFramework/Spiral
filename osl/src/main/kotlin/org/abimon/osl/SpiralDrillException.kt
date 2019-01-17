package org.abimon.osl

class SpiralDrillException(val msg: String): RuntimeException(msg) {
    operator fun component1(): String = msg
}