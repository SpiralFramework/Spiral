package info.spiralframework.gui.javafx;

import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Borrowed from <a href="https://stackoverflow.com/a/58936433">StackOverflow</a>
 */
public class FilteredTreeViewSelectionModel<S> extends MultipleSelectionModel<TreeItem<S>> {

    private final TreeView<S> treeView;
    private final MultipleSelectionModel<TreeItem<S>> selectionModel;
    private final Predicate<TreeItem<S>> filter;

    public FilteredTreeViewSelectionModel(
            TreeView<S> treeView,
            MultipleSelectionModel<TreeItem<S>> selectionModel,
            Predicate<TreeItem<S>> filter) {
        this.treeView = treeView;
        this.selectionModel = selectionModel;
        this.filter = filter;
        selectionModeProperty().bindBidirectional(selectionModel.selectionModeProperty());
    }

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return this.selectionModel.getSelectedIndices();
    }

    @Override
    public ObservableList<TreeItem<S>> getSelectedItems() {
        return this.selectionModel.getSelectedItems();
    }

    private int getRowCount() {
        return this.treeView.getExpandedItemCount();
    }

    @Override
    public boolean isSelected(int index) {
        return this.selectionModel.isSelected(index);
    }

    @Override
    public boolean isEmpty() {
        return this.selectionModel.isEmpty();
    }

    @Override
    public void select(int index) {
        // If the row is -1, we need to clear the selection.
        if (index == -1) {
            this.selectionModel.clearSelection();
        } else if (index >= 0 && index < getRowCount()) {
            // If the tree-item at the specified row-index is selectable, we
            // forward select call to the internal selection-model.
            TreeItem<S> treeItem = this.treeView.getTreeItem(index);
            if (this.filter.test(treeItem)) {
                this.selectionModel.select(index);
            }
        }
    }

    @Override
    public void select(TreeItem<S> treeItem) {
        if (treeItem == null) {
            // If the provided tree-item is null, and we are in single-selection
            // mode we need to clear the selection.
            if (getSelectionMode() == SelectionMode.SINGLE) {
                this.selectionModel.clearSelection();
            }
            // Else, we just forward to the internal selection-model so that
            // the selected-index can be set to -1, and the selected-item
            // can be set to null.
            else {
                this.selectionModel.select(null);
            }
        } else if (this.filter.test(treeItem)) {
            this.selectionModel.select(treeItem);
        }
    }

    @Override
    public void selectIndices(int index, int... indices) {
        // If we have no trailing rows, we forward to normal row-selection.
        if (indices == null || indices.length == 0) {
            select(index);
            return;
        }

        // Filter indices so that we only end up with those indices whose
        // corresponding tree-items are selectable.
        int[] filteredIndices = IntStream.concat(IntStream.of(index), Arrays.stream(indices)).filter(indexToCheck -> {
            TreeItem<S> treeItem = treeView.getTreeItem(indexToCheck);
            return (treeItem != null) && filter.test(treeItem);
        }).toArray();

        // If we have indices left, we proceed to forward to internal selection-model.
        if (filteredIndices.length > 0) {
            int newIndex = filteredIndices[0];
            int[] newIndices = Arrays.copyOfRange(filteredIndices, 1, filteredIndices.length);
            this.selectionModel.selectIndices(newIndex, newIndices);
        }
    }

    @Override
    public void clearAndSelect(int index) {
        // If the index is out-of-bounds we just clear and return.
        if (index < 0 || index >= getRowCount()) {
            clearSelection();
            return;
        }

        // Get tree-item at index.
        TreeItem<S> treeItem = this.treeView.getTreeItem(index);

        // If the tree-item at the specified row-index is selectable, we forward
        // clear-and-select call to the internal selection-model.
        if (this.filter.test(treeItem)) {
            this.selectionModel.clearAndSelect(index);
        }
        // Else, we just do a normal clear-selection call.
        else {
            this.selectionModel.clearSelection();
        }
    }

    @Override
    public void selectAll() {
        int rowCount = getRowCount();

        // If we are in single-selection mode, we exit prematurely as
        // we cannot select all rows.
        if (getSelectionMode() == SelectionMode.SINGLE) {
            return;
        }

        // If we only have a single index to select, we forward to the
        // single-index select-method.
        if (rowCount == 1) {
            select(0);
        }
        // Else, if we have more than one index available, we construct an array
        // of all the indices and forward to the selectIndices-method.
        else if (rowCount > 1) {
            int index = 0;
            int[] indices = IntStream.range(1, rowCount).toArray();
            selectIndices(index, indices);
        }
    }

    @Override
    public void clearSelection(int index) {
        this.selectionModel.clearSelection(index);
    }

    @Override
    public void clearSelection() {
        this.selectionModel.clearSelection();
    }

    @Override
    public void selectFirst() {
        Optional<TreeItem<S>> firstItem = IntStream.range(0, getRowCount())
                .mapToObj(this.treeView::getTreeItem)
                .filter(this.filter)
                .findFirst();
        firstItem.ifPresent(this.selectionModel::select);
    }

    @Override
    public void selectLast() {
        int rowCount = getRowCount();
        Optional<TreeItem<S>> lastItem = IntStream.iterate(rowCount - 1, i -> i - 1)
                .limit(rowCount)
                .mapToObj(this.treeView::getTreeItem)
                .filter(this.filter)
                .findFirst();
        lastItem.ifPresent(this.selectionModel::select);
    }

    private int getFocusedIndex() {
        FocusModel<TreeItem<S>> focusModel = this.treeView.getFocusModel();
        return (focusModel == null) ? -1 : focusModel.getFocusedIndex();
    }

    @Override
    public void selectPrevious() {
        int focusIndex = getFocusedIndex();
        // If we have nothing selected, wrap around to the last index.
        int startIndex = (focusIndex == -1) ? getRowCount() : focusIndex;
        if (startIndex > 0) {
            Optional<TreeItem<S>> previousItem = IntStream.iterate(startIndex - 1, i -> i - 1)
                    .limit(startIndex)
                    .mapToObj(this.treeView::getTreeItem)
                    .filter(this.filter)
                    .findFirst();
            previousItem.ifPresent(this.selectionModel::select);
        }
    }

    @Override
    public void selectNext() {
        // If we have nothing selected, starting at -1 will work out correctly
        // because we'll search from 0 onwards.
        int startIndex = getFocusedIndex();
        if (startIndex < getRowCount() - 1) {
            Optional<TreeItem<S>> nextItem = IntStream.range(startIndex + 1, getRowCount())
                    .mapToObj(this.treeView::getTreeItem)
                    .filter(this.filter)
                    .findFirst();

            nextItem.ifPresent(this.selectionModel::select);
        }
    }
}