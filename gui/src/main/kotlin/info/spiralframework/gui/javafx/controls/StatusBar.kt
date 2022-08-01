package info.spiralframework.gui.javafx.controls

import info.spiralframework.gui.javafx.Lagann
import info.spiralframework.gui.javafx.getValue
import info.spiralframework.gui.javafx.setValue
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.scene.Node
import javafx.scene.control.*

class StatusBar : ToolBar() {
    val statusLabelProperty: ObjectProperty<Label> = SimpleObjectProperty(null)
    var statusLabel: Label? by statusLabelProperty

    val statusTextProperty: StringProperty = SimpleStringProperty(null)
    var statusText: String? by statusTextProperty

    val statusGraphicProperty: ObjectProperty<Node> = SimpleObjectProperty(null)
    var statusGraphic: Node? by statusGraphicProperty

    val themeGroup = ToggleGroup()
    val themeRadioButtons = listOf("Dark Theme" to "dark", "Light Theme" to "light").map { (name, key) ->
        val item = RadioMenuItem(name)
        item.isSelected = Lagann.THEMED.theme == key
        item.toggleGroup = themeGroup
        item.setOnAction { Lagann.THEMED.theme = key }
        return@map item
    }
    val themeButton = MenuButton("Theme")

    private val statusLabelListener: ChangeListener<Label> = ChangeListener { _, oldLabel, newLabel ->
        oldLabel?.textProperty()?.unbindBidirectional(statusTextProperty)
        oldLabel?.graphicProperty()?.unbindBidirectional(statusGraphicProperty)

        newLabel?.textProperty()?.bindBidirectional(statusTextProperty)
        newLabel?.graphicProperty()?.bindBidirectional(statusGraphicProperty)
    }

    init {
        statusLabelProperty.addListener(statusLabelListener)

        val label = Label()
        statusLabel = label

        themeButton.items.addAll(themeRadioButtons)
        themeButton.setOnAction { event -> event.consume() }
        items.addAll(label, HorizontalSpacer(), themeButton)
    }
}