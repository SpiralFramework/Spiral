package info.spiralframework.spiral.updater

import java.io.File

const val STUB_CLASS_PATH = "info.spiralframework.spiral.updater.Stub"
const val UPDATE_CLASS_PATH = "info.spiralframework.spiral.updater.Updater"
const val DELETE_CLASS_PATH = "info.spiralframework.spiral.updater.DeleteUpdate"

fun installUpdate(updatePath: String, vararg otherArgs: String) {
    val javaHome = System.getProperty("java.home")
    val javaBin = javaHome +
            File.separator + "bin" +
            File.separator + "java"

    println(Thread.currentThread().stackTrace[2].className)
    val codeSource = Class.forName(Thread.currentThread().stackTrace[2].className).jarLocation
    ProcessBuilder(javaBin, "-cp", updatePath, STUB_CLASS_PATH, UPDATE_CLASS_PATH, codeSource, *otherArgs)
            .inheritIO()
            .start()
}

val Class<*>.jarLocation: String
    get() = protectionDomain.codeSource.location.toExternalForm()