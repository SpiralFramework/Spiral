package org.abimon.spiral.core.utils

import java.io.InputStream
import java.io.OutputStream

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

fun OutputStream.writeInt64LE(num: Number) {
    val long = num.toLong()

    write(long.toInt() % 256)
    write((long shr 8).toInt() % 256)
    write((long shr 16).toInt() % 256)
    write((long shr 24).toInt() % 256)
    write((long shr 32).toInt() % 256)
    write((long shr 40).toInt() % 256)
    write((long shr 48).toInt() % 256)
    write((long shr 56).toInt() % 256)
}

fun OutputStream.writeInt64BE(num: Number) {
    val long = num.toLong()

    write((long shr 56).toInt() % 256)
    write((long shr 48).toInt() % 256)
    write((long shr 40).toInt() % 256)
    write((long shr 32).toInt() % 256)
    write((long shr 24).toInt() % 256)
    write((long shr 16).toInt() % 256)
    write((long shr 8).toInt() % 256)
    write(long.toInt() % 256)
}

fun OutputStream.writeInt32LE(num: Number) {
    val int = num.toInt()

    write(int % 256)
    write((int shr 8) % 256)
    write((int shr 16) % 256)
    write((int shr 24) % 256)
}

fun OutputStream.writeInt32BE(num: Number) {
    val int = num.toInt()

    write((int shr 24) % 256)
    write((int shr 16) % 256)
    write((int shr 8) % 256)
    write(int % 256)
}