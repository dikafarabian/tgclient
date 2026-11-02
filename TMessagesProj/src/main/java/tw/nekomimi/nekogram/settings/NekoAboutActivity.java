package tw.nekomimi.nekogram.settings;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.ProfileActivity;

import kotlin.Unit;
import tw.nekomimi.nekogram.DatacenterActivity;
import tw.nekomimi.nekogram.helpers.remote.UpdateHelper;
import tw.nekomimi.nekogram.ui.BottomBuilder;
import tw.nekomimi.nekogram.ui.cells.HeaderCell;
import tw.nekomimi.nekogram.utils.AndroidUtil;
import xyz.nextalone.nagram.NaConfig;

public class NekoAboutActivity extends BaseNekoSettingsActivity {

    private int infoHeaderRow;
    private int versionRow;
    private int updatesRow;
    private int toggleLogsRow;
    private int sendLogsRow;
    private int clearLogsRow;
    private int donateInfoRow;

    private int linksHeaderRow;
    private int forkChannelRow;
    private int xChannelRow;
    private int channelTipsRow;
    private int sourceCodeRow;
    private int datacenterStatusRow;
    private int linksShadowRow;

    @Override
    protected void updateRows() {
        super.updateRows();

        infoHeaderRow = addRow();
        versionRow = addRow();
        updatesRow = -1;
        toggleLogsRow = addRow();
        if (BuildVars.LOGS_ENABLED) {
            sendLogsRow = addRow();
            clearLogsRow = addRow();
        } else {
            sendLogsRow = -1;
            clearLogsRow = -1;
        }
        donateInfoRow = -1;

        linksHeaderRow = addRow();
        forkChannelRow = -1;
        xChannelRow = -1;
        channelTipsRow = -1;
        sourceCodeRow = -1;
        datacenterStatusRow = addRow();
        linksShadowRow = addRow();
    }

    @Override
    protected String getActionBarTitle() {
        return getString(R.string.About);
    }

    private String getUpdateChannelDetail() {
        switch (NaConfig.INSTANCE.getAutoUpdateChannel().Int()) {
            case UpdateHelper.UPDATE_OFF:
                return getString(R.string.AutoCheckUpdateOFF);
            case UpdateHelper.UPDATE_CHANNEL_RELEASE:
                return getString(R.string.AutoCheckUpdateRelease);
            case UpdateHelper.UPDATE_CHANNEL_BETA:
                return getString(R.string.AutoCheckUpdateBeta);
            default:
                return getString(R.string.AutoCheckUpdateOFF);
        }
    }

    private String getSimpleVersion() {
        String versionName = BuildConfig.VERSION_NAME.split("-")[0];
        return "luminegram v" + versionName;
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == versionRow) {
            Browser.openUrl(getParentActivity(), "https://github.com/dikafarabian/tgclient");
        } else if (position == updatesRow) {
            showUpdatesDialog();
        } else if (position == toggleLogsRow) {
            boolean wasLogsEnabled = BuildVars.LOGS_ENABLED;
            AndroidUtil.toggleLogs();
            listAdapter.notifyItemChanged(toggleLogsRow);
            if (!wasLogsEnabled && BuildVars.LOGS_ENABLED) {
                sendLogsRow = toggleLogsRow + 1;
                clearLogsRow = toggleLogsRow + 2;
                listAdapter.notifyItemInserted(sendLogsRow);
                listAdapter.notifyItemInserted(clearLogsRow);
                shiftRowsAfterLogsEnabled();
            } else if (wasLogsEnabled && !BuildVars.LOGS_ENABLED) {
                listAdapter.notifyItemRemoved(toggleLogsRow + 1);
                listAdapter.notifyItemRemoved(toggleLogsRow + 1);
                sendLogsRow = -1;
                clearLogsRow = -1;
                shiftRowsAfterLogsDisabled();
            }
        } else if (position == sendLogsRow) {
            ProfileActivity.sendLogs(getParentActivity(), false);
        } else if (position == clearLogsRow) {
            FileLog.cleanupLogs();
        } else if (position == datacenterStatusRow) {
            presentFragment(new DatacenterActivity(0));
        }
    }

    private void shiftRowsAfterLogsEnabled() {
        linksHeaderRow += 2;
        datacenterStatusRow += 2;
        linksShadowRow += 2;
        rowCount += 2;
    }

    private void shiftRowsAfterLogsDisabled() {
        linksHeaderRow -= 2;
        datacenterStatusRow -= 2;
        linksShadowRow -= 2;
        rowCount -= 2;
    }

    private void showUpdatesDialog() {
        BottomBuilder builder = new BottomBuilder(getParentActivity());
        builder.addTitle(getString(R.string.CheckUpdate));
        builder.addItem(getString(R.string.CheckUpdate), R.drawable.msg_retry, (it) -> {
            Browser.openUrl(getParentActivity(), "tg://update");
            return Unit.INSTANCE;
        });
        builder.addItem(getString(R.string.AutoCheckUpdateSwitch) + " - " + getUpdateChannelDetail(), R.drawable.msg_channel, (it) -> {
            showUpdateChannelDialog();
            return Unit.INSTANCE;
        });
        builder.show();
    }

    private void showUpdateChannelDialog() {
        BottomBuilder switchBuilder = new BottomBuilder(getParentActivity());
        switchBuilder.addTitle(getString(R.string.AutoCheckUpdateSwitch));
        switchBuilder.addRadioItem(getString(R.string.AutoCheckUpdateOFF), NaConfig.INSTANCE.getAutoUpdateChannel().Int() == UpdateHelper.UPDATE_OFF, (radioButtonCell) -> {
            NaConfig.INSTANCE.getAutoUpdateChannel().setConfigInt(UpdateHelper.UPDATE_OFF);
            switchBuilder.doRadioCheck(radioButtonCell);
            AndroidUtilities.runOnUIThread(() -> {
                switchBuilder.dismiss();
                UpdateHelper.cleanAppUpdate();
                if (listAdapter != null) {
                    listAdapter.notifyItemChanged(updatesRow);
                }
            }, 500);
            return Unit.INSTANCE;
        });
        switchBuilder.addRadioItem(getString(R.string.AutoCheckUpdateRelease), NaConfig.INSTANCE.getAutoUpdateChannel().Int() == UpdateHelper.UPDATE_CHANNEL_RELEASE, (radioButtonCell) -> {
            NaConfig.INSTANCE.getAutoUpdateChannel().setConfigInt(UpdateHelper.UPDATE_CHANNEL_RELEASE);
            switchBuilder.doRadioCheck(radioButtonCell);
            AndroidUtilities.runOnUIThread(() -> {
                switchBuilder.dismiss();
                Browser.openUrl(getParentActivity(), "tg://update");
                if (listAdapter != null) {
                    listAdapter.notifyItemChanged(updatesRow);
                }
            }, 500);
            return Unit.INSTANCE;
        });
        switchBuilder.addRadioItem(getString(R.string.AutoCheckUpdateBeta), NaConfig.INSTANCE.getAutoUpdateChannel().Int() == UpdateHelper.UPDATE_CHANNEL_BETA, (radioButtonCell) -> {
            NaConfig.INSTANCE.getAutoUpdateChannel().setConfigInt(UpdateHelper.UPDATE_CHANNEL_BETA);
            switchBuilder.doRadioCheck(radioButtonCell);
            AndroidUtilities.runOnUIThread(() -> {
                switchBuilder.dismiss();
                Browser.openUrl(getParentActivity(), "tg://update");
                if (listAdapter != null) {
                    listAdapter.notifyItemChanged(updatesRow);
                }
            }, 500);
            return Unit.INSTANCE;
        });
        showDialog(switchBuilder.create());
    }

    @Override
    protected BaseListAdapter createAdapter(Context context) {
        return new ListAdapter(context);
    }

    private class ListAdapter extends BaseListAdapter {

        public ListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (holder.getItemViewType()) {
                case TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == infoHeaderRow) {
                        headerCell.setText(getString(R.string.NaxAboutInfo));
                    } else if (position == linksHeaderRow) {
                        headerCell.setText(getString(R.string.NaxLinks));
                    }
                    break;
                case TYPE_DETAIL_SETTINGS:
                    TextDetailSettingsCell detailCell = (TextDetailSettingsCell) holder.itemView;
                    if (position == versionRow) {
                        detailCell.setMultilineDetail(true);
                        detailCell.setTextAndValue(getSimpleVersion(), getString(R.string.LumineAboutDesc), false);
                    }
                    break;
                case TYPE_TEXT:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == updatesRow) {
                        textCell.setTextAndValueAndIcon(getString(R.string.CheckUpdate), getUpdateChannelDetail(), R.drawable.msg_retry, true);
                    } else if (position == toggleLogsRow) {
                        textCell.setTextAndIcon(BuildVars.LOGS_ENABLED ? getString(R.string.DebugMenuDisableLogs) : getString(R.string.DebugMenuEnableLogs), R.drawable.bug, sendLogsRow != -1);
                    } else if (position == sendLogsRow) {
                        textCell.setTextAndIcon(getString(R.string.DebugSendLogs), R.drawable.ic_upward, true);
                    } else if (position == clearLogsRow) {
                        textCell.setTextAndIcon(getString(R.string.DebugClearLogs), R.drawable.msg_clear, false);
                    } else if (position == datacenterStatusRow) {
                        textCell.setTextAndIcon(getString(R.string.DatacenterStatus), R.drawable.msg_info, false);
                    }
                    break;
                case TYPE_INFO_PRIVACY:
                    TextInfoPrivacyCell infoCell = (TextInfoPrivacyCell) holder.itemView;
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == infoHeaderRow || position == linksHeaderRow) {
                return TYPE_HEADER;
            } else if (position == versionRow) {
                return TYPE_DETAIL_SETTINGS;
            } else if (position == linksShadowRow) {
                return TYPE_SHADOW;
            } else if (position == donateInfoRow) {
                return TYPE_INFO_PRIVACY;
            } else {
                return TYPE_TEXT;
            }
        }
    }
}
