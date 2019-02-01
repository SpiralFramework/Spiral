package info.spiralframework.spiral.updater

import java.io.File
import kotlin.reflect.KFunction

object DeleteUpdate {
    lateinit var mainMethod: KFunction<*>

    @JvmStatic
    fun main(args: Array<String>) {
        val updateFile = args[0]
        File(updateFile).delete()
        //print("\n\r")
        mainMethod.call(*args.drop(1).toTypedArray())
    }
}