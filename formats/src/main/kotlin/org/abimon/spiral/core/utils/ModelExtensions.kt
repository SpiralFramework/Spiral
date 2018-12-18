package org.abimon.spiral.core.utils

import org.abimon.spiral.core.objects.models.*
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

fun Collection<GMOModelChunk>.findChunk(predicate: (GMOModelChunk) -> Boolean): GMOModelChunk? {
    forEach { chunk ->
        if (predicate(chunk))
            return chunk

        when (chunk) {
            is GMOSubfileChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
            is GMOModelSurfaceChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
            is GMOMeshChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
            is GMOMaterialChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
        }
    }

    return null
}

fun Collection<GMOModelChunk>.filterChunks(predicate: (GMOModelChunk) -> Boolean): List<GMOModelChunk> {
    return flatMap { chunk ->
        if (predicate(chunk))
            return@flatMap listOf(chunk)

        when (chunk) {
            is GMOSubfileChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
            is GMOModelSurfaceChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
            is GMOMeshChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
            is GMOMaterialChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
            else -> return@flatMap emptyList<GMOModelChunk>()
        }
    }
}

fun Array<GMOModelChunk>.findChunk(predicate: (GMOModelChunk) -> Boolean): GMOModelChunk? {
    forEach { chunk ->
        if (predicate(chunk))
            return chunk

        when (chunk) {
            is GMOSubfileChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
            is GMOModelSurfaceChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
            is GMOMeshChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
            is GMOMaterialChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
        }
    }

    return null
}

fun Array<GMOModelChunk>.filterChunks(predicate: (GMOModelChunk) -> Boolean): List<GMOModelChunk> {
    return flatMap { chunk ->
        if (predicate(chunk))
            return@flatMap listOf(chunk)

        when (chunk) {
            is GMOSubfileChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
            is GMOModelSurfaceChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
            is GMOMeshChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
            is GMOMaterialChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
            else -> return@flatMap emptyList<GMOModelChunk>()
        }
    }
}

fun <T: GMOModelChunk> Array<GMOModelChunk>.filterChunks(klass: KClass<T>): List<T> = filterChunks { chunk -> klass.isInstance(chunk) }.map { chunk -> klass.cast(chunk) }
fun <T: GMOModelChunk> Collection<GMOModelChunk>.filterChunks(klass: KClass<T>): List<T> = filterChunks { chunk -> klass.isInstance(chunk) }.map { chunk -> klass.cast(chunk) }