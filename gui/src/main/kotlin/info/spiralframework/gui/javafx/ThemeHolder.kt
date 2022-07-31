package info.spiralframework.gui.javafx

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.Scene

class ThemeHolder {
    public val scenes: ObservableList<Scene> = FXCollections.observableArrayList() { scene -> arrayOf(scene.rootProperty()) }
    public val theme: StringProperty = SimpleStringProperty()

    private val sceneListener: ListChangeListener<Scene> = ListChangeListener { change ->
//        while (change.next()) {
//            if (change.wasUpdated()) {
//                change.
//            } else {
//                change.removed.forEach { scene ->
//                    scene.root.
//                }
//            }
//        }
    }

    init {

    }
}