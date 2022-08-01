package info.spiralframework.gui.javafx

import info.spiralframework.gui.javafx.controls.StatusBar
import info.spiralframework.gui.javafx.controls.content.ContentPane
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.ProgressBar
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
import java.io.File
import kotlin.coroutines.CoroutineContext

class Lagann : Application() {
    object IO : CoroutineScope {
        override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO
    }

    object UI : CoroutineScope by MainScope()

    companion object {
        val STYLESHEET_SET = setOf(File("gui/src/main/resources/style.css").toURI())
        val STYLESHEETS = ReloadingStylesheets(STYLESHEET_SET, UI, IO)

        val THEMED = ThemeHolder("light")
    }

    val rootPane = BorderPane()
    val scene: Scene

    val mainMenuBar = MenuBar()
    val statusBar = StatusBar()
    val contentPane = ContentPane()

    val fileMenu = Menu("File")

    val fileChooser = FileChooser()
    val openFile = MenuItem("Open File", FontIcon(BootstrapIcons.FILE_EARMARK_PLUS))

    override fun start(primaryStage: Stage) {
        primaryStage.scene = scene
        primaryStage.show()

        statusBar.statusText = "Loaded!"
    }

    public fun pickFile() {
        val file = fileChooser.showOpenDialog(scene.window) ?: return

        statusBar.statusGraphic = ProgressBar()
        statusBar.statusText = "Opening ${file.path}..."

//        IO.launch {
//            val result = contentForData(AsyncFileDataSource(file))
//
//            withContext(UI.coroutineContext) {
//                result.doOnFailure {
//                    statusBar.statusGraphic = null
//                    statusBar.statusText = "Error: no recognised file format"
//
//                    delay(10_000L)
//
//                    statusBar.statusText = null
//                }.doOnSuccess { node ->
//                    contentPane = node
//
//                    statusBar.statusGraphic = null
//                    statusBar.statusText = "Loaded ${file.path}"
//                }
//            }
//        }
    }

    init {
        scene = Scene(rootPane, 1280.0, 720.0)
        STYLESHEETS.addScene(scene)
        THEMED.scenes.add(scene)

        rootPane.top = mainMenuBar
        rootPane.center = contentPane
        rootPane.bottom = statusBar

        openFile.setOnAction { pickFile() }
        fileMenu.items.addAll(openFile)

        mainMenuBar.menus.addAll(fileMenu)
    }
}