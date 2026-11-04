// ======================================================
// luminegram - Material Design 3 options group
// ======================================================

package app.lumine.md3;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.ui.Cells.TextCheckCell2;
import org.telegram.ui.Components.RecyclerListView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import tw.nekomimi.nekogram.config.CellGroup;
import tw.nekomimi.nekogram.config.cell.AbstractConfigCell;
import tw.nekomimi.nekogram.config.cell.WithOnClick;

public class ConfigCellMd3Group extends AbstractConfigCell implements WithOnClick {

    private final String title;
    private final Runnable onApplied;
    private final Runnable onRowsChanged;
    private final List<ConfigCellMd3Option> options;
    private boolean expanded = true;

    public ConfigCellMd3Group(String title, Runnable onApplied, Runnable onRowsChanged, ConfigCellMd3Option... options) {
        this.title = title;
        this.onApplied = onApplied;
        this.onRowsChanged = onRowsChanged;
        this.options = Arrays.asList(options);
        for (ConfigCellMd3Option option : this.options) {
            option.attach(this);
        }
    }

    @Override
    public void bindCellGroup(CellGroup cellGroup) {
        super.bindCellGroup(cellGroup);
        for (ConfigCellMd3Option option : options) {
            option.bindCellGroup(cellGroup);
        }
    }

    public List<ConfigCellMd3Option> visibleOptions() {
        return expanded ? options : Collections.emptyList();
    }

    int selectedCount() {
        int count = 0;
        for (ConfigCellMd3Option option : options) {
            if (option.isOn()) count++;
        }
        return count;
    }

    void applied() {
        onApplied.run();
    }

    private void toggleAll() {
        final boolean enable = selectedCount() == 0;
        for (ConfigCellMd3Option option : options) {
            option.set(enable);
        }
        onApplied.run();
    }

    private void toggleExpanded() {
        final RecyclerListView.SelectionAdapter adapter = cellGroup.getListAdapter();
        final int index = cellGroup.rows.indexOf(this);
        if (index < 0) return;
        expanded = !expanded;
        if (expanded) {
            cellGroup.rows.addAll(index + 1, options);
            if (adapter != null) adapter.notifyItemRangeInserted(index + 1, options.size());
        } else {
            cellGroup.rows.removeAll(options);
            if (adapter != null) adapter.notifyItemRangeRemoved(index + 1, options.size());
        }
        if (adapter != null) adapter.notifyItemChanged(index);
        onRowsChanged.run();
    }

    @Override
    public int getType() {
        return CellGroup.ITEM_TYPE_CHECK2;
    }

    @Override
    public CharSequence getTitle() {
        return title;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder) {
        TextCheckCell2 cell = (TextCheckCell2) holder.itemView;
        final int selected = selectedCount();
        cell.setTextAndCheck(title, selected > 0, cellGroup.needSetDivider(this));
        cell.setCollapseArrow(selected + "/" + options.size(), !expanded, this::toggleAll);
    }

    @Override
    public void onClick() {
        toggleExpanded();
    }
}
