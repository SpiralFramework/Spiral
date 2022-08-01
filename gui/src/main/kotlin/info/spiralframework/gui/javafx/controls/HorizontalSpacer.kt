package info.spiralframework.gui.javafx.controls

import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region

class HorizontalSpacer: Region() {
    init {
        HBox.setHgrow(this, Priority.ALWAYS)
    }
}