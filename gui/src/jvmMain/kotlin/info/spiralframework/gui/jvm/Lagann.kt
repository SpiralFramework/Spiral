package info.spiralframework.gui.jvm

import javafx.application.Application
import javafx.stage.Stage

class Lagann: Application() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Lagann::class.java, *args)
        }
    }

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     *
     *
     * NOTE: This method is called on the JavaFX Application Thread.
     *
     *
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set. The primary stage will be embedded in
     * the browser if the application was launched as an applet.
     * Applications may create other stages, if needed, but they will not be
     * primary stages and will not be embedded in the browser.
     */
    override fun start(primaryStage: Stage) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}