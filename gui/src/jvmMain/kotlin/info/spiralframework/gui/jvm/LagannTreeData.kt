package info.spiralframework.gui.jvm

import javafx.scene.control.TreeCell
import javafx.scene.control.TreeView
import org.abimon.kornea.io.common.DataSource

sealed class LagannTreeData(val name: String, val dataSource: DataSource<*>?) {
    companion object {
        operator fun invoke(name: String, dataSource: DataSource<*>?): LagannTreeData =
                if (dataSource == null) Directory(name) else SubFile(name, dataSource)
    }

    class SubFile(name: String, dataSource: DataSource<*>): LagannTreeData(name, dataSource)
    class Directory(name: String): LagannTreeData(name, null)
    class PredefinedSubFile(name: String, dataSource: DataSource<*>, val data: DataPair<*>): LagannTreeData(name, dataSource)
}
class LagannTreeDataCell : TreeCell<LagannTreeData>() {
    companion object {
        fun factory(view: TreeView<LagannTreeData>): LagannTreeDataCell = LagannTreeDataCell()
    }
    override fun updateItem(item: LagannTreeData?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            text = null
        } else {
            text = item.name.substringAfterLast('/')
        }
    }
}