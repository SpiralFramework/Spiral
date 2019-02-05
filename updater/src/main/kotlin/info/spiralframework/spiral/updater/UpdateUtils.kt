package info.spiralframework.spiral.updater

import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.Paths

const val STUB_CLASS_PATH = "info.spiralframework.spiral.updater.Stub"
const val UPDATE_CLASS_PATH = "info.spiralframework.spiral.updater.Updater"
const val MOVER_CLASS_PATH = "info.spiralframework.spiral.updater.Mover"

const val DELETE_ARG = "DELETE_ARG"

fun installUpdate(updatePath: String, vararg otherArgs: String) {
    val javaHome = System.getProperty("java.home")
    val javaBin = javaHome +
            File.separator + "bin" +
            File.separator + "java"

    val codeSource = Class.forName(Thread.currentThread().stackTrace[2].className).jarLocation
    ProcessBuilder(javaBin, "-cp", updatePath, STUB_CLASS_PATH, UPDATE_CLASS_PATH, codeSource.toURI().toString(), *otherArgs)
            .inheritIO()
            .start()
}

fun moveUpdate(from: URI, to: URI) {
    val javaHome = System.getProperty("java.home")
    val javaBin = javaHome +
            File.separator + "bin" +
            File.separator + "java"

    ProcessBuilder(javaBin, "-cp", Paths.get(from).toFile().absolutePath, STUB_CLASS_PATH, MOVER_CLASS_PATH, from.toString(), to.toString())
            .inheritIO()
            .start()
}

val Class<*>.jarLocation: URL
    get() = protectionDomain.codeSource.location

val Class<*>.jarLocationAsFile: File
    get() = Paths.get(jarLocation.toURI()).toFile()