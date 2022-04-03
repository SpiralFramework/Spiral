package info.spiralframework.base.binding

import java.util.*

public val JVM_IS_ANSI_SUPPORTED: Boolean by lazy {
    if (System.console() == null) //Pseudo-console support
        return@lazy false

    val os = System.getProperty("os.name").lowercase(Locale.getDefault())

    return@lazy "windows" !in os || System.getenv("ANSICON") != null
}

public actual fun isAnsiSupported(): Boolean = JVM_IS_ANSI_SUPPORTED