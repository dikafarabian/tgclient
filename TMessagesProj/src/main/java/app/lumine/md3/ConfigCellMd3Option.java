// ======================================================
// luminegram - Material Design 3 option row
// ======================================================

package app.lumine.md3;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.ui.Cells.CheckBoxCell;

import tw.nekomimi.nekogram.config.CellGroup;
import tw.nekomimi.nekogram.config.cell.AbstractConfigCell;
import tw.nekomimi.nekogram.config.cell.WithOnClick;

public class ConfigCellMd3Option extends AbstractConfigCell implements WithOnClick {

    public interface Getter {
        boolean get();
    }

    public interface Setter {
        void set(boolean on);
    }

    private final String title;
    private final Getter getter;
    private final Setter setter;
    private ConfigCellMd3Group group;

    public ConfigCellMd3Option(String title, Getter getter, Setter setter) {
        this.title = title;
        this.getter = getter;
        this.setter = setter;
    }

    void attach(ConfigCellMd3Group group) {
        this.group = group;
    }

    boolean isOn() {
        return getter.get();
    }

    void set(boolean on) {
        setter.set(on);
    }

    @Override
    public int getType() {
        return CellGroup.ITEM_TYPE_CHECK_BOX;
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
        CheckBoxCell cell = (CheckBoxCell) holder.itemView;
        cell.setText(title, "", isOn(), cellGroup.needSetDivider(this), true);
    }

    @Override
    public void onClick() {
        set(!isOn());
        if (group != null) {
            group.applied();
        }
    }
}
