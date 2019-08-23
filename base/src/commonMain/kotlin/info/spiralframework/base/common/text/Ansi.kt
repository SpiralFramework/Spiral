package info.spiralframework.base.common.text

object Ansi {
    inline infix fun CURSOR_UP(rows: Int) = "\u001B[$${rows}A"
    inline infix fun CURSOR_DOWN(rows: Int) = "\u001B[${rows}B"
    inline infix fun CURSOR_FORWARD(rows: Int) = "\u001B[${rows}C"
    inline infix fun CURSOR_BACK(rows: Int) = "\u001B[${rows}D"
}