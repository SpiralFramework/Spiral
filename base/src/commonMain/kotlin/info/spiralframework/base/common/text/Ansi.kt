@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.text

public object Ansi {
    public inline infix fun CURSOR_UP(rows: Int): String = "\u001B[$${rows}A"
    public inline infix fun CURSOR_DOWN(rows: Int): String = "\u001B[${rows}B"
    public inline infix fun CURSOR_FORWARD(rows: Int): String = "\u001B[${rows}C"
    public inline infix fun CURSOR_BACK(rows: Int): String = "\u001B[${rows}D"
}