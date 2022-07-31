package info.spiralframework.gui.javafx.controls.content

import info.spiralframework.gui.javafx.add
import info.spiralframework.gui.javafx.config
import info.spiralframework.gui.javafx.withStyles
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.javafx.StackedFontIcon

class LogoPane : VBox() {
    val logoStackIcon = config(::StackedFontIcon)
    private val topLayer = config { FontIcon().withStyles("top-icon-layer") }
    private val bottomLayer = config { FontIcon().withStyles("bottom-icon-layer") }

    val description = Label("No File Selected")

    fun setStartup() {
        children.add(logoStackIcon) {
            children.add(topLayer) { iconCode = BootstrapIcons.QUESTION }
            children.add(bottomLayer) { iconCode = BootstrapIcons.FILE_EARMARK }
        }

        children.add(description)
    }

    init {
        styleClass.add("logo-pane")

        alignment = Pos.CENTER

    }
}