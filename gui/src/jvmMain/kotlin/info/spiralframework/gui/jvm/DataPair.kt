package info.spiralframework.gui.jvm

import info.spiralframework.formats.common.archives.srd.TextureSrdEntry
import javafx.scene.control.ListCell
import javafx.util.StringConverter

data class DataPair<T>(val name: String, val data: T, val chance: Double)
class DataPairCell: ListCell<DataPair<*>>() {
    override fun updateItem(item: DataPair<*>?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            text = null
        } else {
            text = "${item.name} (${item.chance * 100}%)"
        }
    }
}
object DataPairCellConverter: StringConverter<DataPair<*>>() {
    override fun toString(item: DataPair<*>?): String = if (item == null) "" else "${item.name} (${item.chance * 100}%)"

    override fun fromString(string: String?): DataPair<*> {
        TODO("Not yet implemented")
    }
}

inline class SrdvTexture(val textures: Array<TextureSrdEntry>)

inline class Utf8String(val string: String)
inline class Utf16String(val string: String)