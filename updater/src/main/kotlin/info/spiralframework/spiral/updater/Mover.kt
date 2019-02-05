package info.spiralframework.spiral.updater

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

object Mover {
    @JvmStatic
    fun main(args: Array<String>) {
        Thread.sleep(500)

        val source = Paths.get(URI(args[0]))
        val destination = Paths.get(URI(args[1]))

        Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING)

        val javaHome = System.getProperty("java.home")
        val javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java"

        ProcessBuilder(javaBin, "-cp", destination.toFile().absolutePath, STUB_CLASS_PATH, DELETE_ARG, source.toUri().toString())
                .inheritIO()
                .start()
    }
}