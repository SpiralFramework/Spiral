package info.spiralframework.osl

class SpiralDrillException(val msg: String): RuntimeException(msg) {
    operator fun component1(): String = msg
}
