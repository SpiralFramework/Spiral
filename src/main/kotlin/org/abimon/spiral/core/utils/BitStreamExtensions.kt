package org.abimon.spiral.core.utils

import java.io.InputStream

fun InputStream.readInt64LE(): Long {
    val a = read().toLong()
    val b = read().toLong()
    val c = read().toLong()
    val d = read().toLong()
    val e = read().toLong()
    val f = read().toLong()
    val g = read().toLong()
    val h = read().toLong()

    return (h shl 56) or (g shl 48) or (f shl 40) or (e shl 32) or
            (d shl 24) or (c shl 16) or (b shl 8) or a
}

fun InputStream.readInt64BE(): Long {
    val a = read().toLong()
    val b = read().toLong()
    val c = read().toLong()
    val d = read().toLong()
    val e = read().toLong()
    val f = read().toLong()
    val g = read().toLong()
    val h = read().toLong()

    return (a shl 56) or (b shl 48) or (c shl 40) or (d shl 32) or
            (e shl 24) or (f shl 16) or (g shl 8) or h
}

fun InputStream.readInt32LE(): Int {
    val a = read()
    val b = read()
    val c = read()
    val d = read()

    return (d shl 24) or (c shl 16) or (b shl 8) or a
}

fun InputStream.readInt32BE(): Int {
    val a = read()
    val b = read()
    val c = read()
    val d = read()

    return (a shl 24) or (b shl 16) or (c shl 8) or d
}

fun InputStream.readUInt32LE(): Long {
    val a = read().toLong()
    val b = read().toLong()
    val c = read().toLong()
    val d = read().toLong()

    return (d shl 24) or (c shl 16) or (b shl 8) or a
}

fun InputStream.readUInt32BE(): Long {
    val a = read().toLong()
    val b = read().toLong()
    val c = read().toLong()
    val d = read().toLong()

    return (a shl 24) or (b shl 16) or (c shl 8) or d
}

fun InputStream.readInt16LE(): Int {
    val a = read()
    val b = read()

    return (b shl 8) or a
}

fun InputStream.readInt16BE(): Int {
    val a = read()
    val b = read()

    return (a shl 8) or b
}