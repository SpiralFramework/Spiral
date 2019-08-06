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

        val originalFile = Paths.get(URI(args[0]))
        val codeSource = Paths.get(Updater::class.java.jarLocation.toURI())

        Files.copy(codeSource, originalFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)

        ProcessBuilder(javaBin, "-cp", originalFile.toFile().absolutePath, STUB_CLASS_PATH, DELETE_ARG, codeSource.toUri().toString())
                .inheritIO()
                .start()

        ProcessBuilder(javaBin, "-jar", originalFile.toFile().absolutePath, *args.drop(1).toTypedArray())
                .inheritIO()
                .start()
    }
}