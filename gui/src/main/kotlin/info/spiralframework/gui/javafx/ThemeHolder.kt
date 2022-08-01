package info.spiralframework.gui.javafx

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.Scene

class ThemeHolder(theme: String) {
    public val scenes: ObservableList<Scene> =
        FXCollections.observableArrayList()

    public val themeProperty: StringProperty = SimpleStringProperty(theme)
    public var theme: String? by themeProperty

    private val themeListener: ChangeListener<String> =
        ChangeListener { _, _, newValue ->
            scenes.forEach { scene ->
                scene.root.removeThemes()
                scene.root.addTheme(newValue)
            }
        }

    private val rootListener: ChangeListener<Node> =
        ChangeListener { _, oldValue, newValue ->
            oldValue.removeThemes()
            newValue.addTheme()
        }

    private val sceneListener: ListChangeListener<Scene> =
        listChangeListener(
            hasRemoved = true, hasAdded = true,

            onRemoved = { _, removed, _ ->
                removed.forEach { scene ->
                    scene.root.removeThemes()
                    scene.rootProperty().removeListener(rootListener)
                }
            },
            onAdded = { _, added, _ ->
                added.forEach { scene ->
                    scene.root.addTheme()
                    scene.rootProperty().addListener(rootListener)
                }
            }
        )

    public inline fun Node.removeThemes() {
        this.styleClass.removeAll { it.endsWith("theme") }
    }

    public inline fun Node.addTheme(theme: String? = this@ThemeHolder.themeProperty.value) {
        if (theme != null) this.styleClass.add("$theme-theme")
    }

    init {
        themeProperty.addListener(themeListener)
        scenes.addListener(sceneListener)
    }
}