package org.abimon.spiral.core.utils

infix fun <A, B, C> Pair<A, B>.and(c: C): Triple<A, B, C> = Triple(first, second, c)