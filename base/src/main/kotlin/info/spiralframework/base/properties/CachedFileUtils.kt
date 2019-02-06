package info.spiralframework.base.properties

import java.io.File

fun <T> cache(file: File, op: (File) -> T): CachedFileReadOnlyProperty<Any, T> = CachedFileReadOnlyProperty(file, op)