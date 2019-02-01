package info.spiralframework.spiral.updater

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

object Stub {
    @JvmStatic
    fun main(args: Array<String>) {
        Thread.sleep(500)

        if (args[0] == DELETE_ARG) {
            Files.delete(Paths.get(URI(args[0])))

            return
        } else {
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
}