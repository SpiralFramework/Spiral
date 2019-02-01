package info.spiralframework.spiral.updater

import java.io.File

object Stub {
    @JvmStatic
    fun main(args: Array<String>) {
        val javaHome = System.getProperty("java.home")
        val javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java"

        val codeSource = Stub::class.java.jarLocation
        ProcessBuilder(javaBin, "-cp", codeSource.toURI().toString(), *args)
                .inheritIO()
                .start()
    }
}