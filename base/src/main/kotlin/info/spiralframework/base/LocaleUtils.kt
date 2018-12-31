package info.spiralframework.base

fun assertAsLocaleArgument(statement: Boolean, illegalArgument: String, vararg illegalParams: Any) {
    if (!statement)
        throw IllegalArgumentException(SpiralLocale.localise(illegalArgument, *illegalParams))
}

inline fun <reified T> locale(illegalArgument: String, vararg illegalParams: Any): T = T::class.java.getDeclaredConstructor(String::class.java).newInstance(SpiralLocale.localise(illegalArgument, *illegalParams))