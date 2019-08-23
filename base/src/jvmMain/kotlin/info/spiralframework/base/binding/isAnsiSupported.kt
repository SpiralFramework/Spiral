package info.spiralframework.base.binding

val JVM_IS_ANSI_SUPPORTED: Boolean by lazy {
    if (System.console() == null) //Pseudo-console support
        return@lazy false

    val os = System.getProperty("os.name").toLowerCase()

    return@lazy "windows" !in os || System.getenv("ANSICON") != null
}

actual fun isAnsiSupported(): Boolean = JVM_IS_ANSI_SUPPORTED