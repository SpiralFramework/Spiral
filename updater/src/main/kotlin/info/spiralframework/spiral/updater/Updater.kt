package info.spiralframework.spiral.updater

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

object Updater {
    @JvmStatic
    fun main(args: Array<String>) {
        Thread.sleep(500)
        val javaHome = System.getProperty("java.home")
        val javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java"

        val originalFile = args[0]
        val codeSource = Updater::class.java.jarLocation

        Files.copy(Paths.get(codeSource.toURI()), Paths.get(URI(originalFile)), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)

        ProcessBuilder(javaBin, "-cp", originalFile, STUB_CLASS_PATH, DELETE_ARG, codeSource.toURI().toString())
                .inheritIO()
                .start()

        ProcessBuilder(javaBin, "-jar", originalFile, *args.drop(1).toTypedArray())
                .inheritIO()
                .start()
    }
}