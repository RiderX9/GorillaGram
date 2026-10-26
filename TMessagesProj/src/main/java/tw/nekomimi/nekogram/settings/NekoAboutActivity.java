package tw.nekomimi.nekogram.settings;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.ProfileActivity;

import tw.nekomimi.nekogram.DatacenterActivity;
import tw.nekomimi.nekogram.ui.cells.HeaderCell;
import tw.nekomimi.nekogram.utils.AndroidUtil;

public class NekoAboutActivity extends BaseNekoSettingsActivity {

    private int infoHeaderRow;
    private int versionRow;
    private int toggleLogsRow;
    private int sendLogsRow;
    private int clearLogsRow;
    private int datacenterStatusRow;
    private int linksShadowRow;

    @Override
    protected void updateRows() {
        super.updateRows();

        infoHeaderRow = addRow();
        versionRow = addRow();
        toggleLogsRow = addRow();
        if (BuildVars.LOGS_ENABLED) {
            sendLogsRow = addRow();
            clearLogsRow = addRow();
        } else {
            sendLogsRow = -1;
            clearLogsRow = -1;
        }
        datacenterStatusRow = addRow();
        linksShadowRow = addRow();
    }

    @Override
    protected String getActionBarTitle() {
        return getString(R.string.About);
    }

    private String getSimpleVersion() {
        String versionName = BuildConfig.VERSION_NAME.split("-")[0];
        return "GorillaGram v" + versionName;
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == toggleLogsRow) {
            boolean wasLogsEnabled = BuildVars.LOGS_ENABLED;
            AndroidUtil.toggleLogs();
            listAdapter.notifyItemChanged(toggleLogsRow);
            if (!wasLogsEnabled && BuildVars.LOGS_ENABLED) {
                sendLogsRow = toggleLogsRow + 1;
                clearLogsRow = toggleLogsRow + 2;
                listAdapter.notifyItemInserted(sendLogsRow);
                listAdapter.notifyItemInserted(clearLogsRow);
            } else if (wasLogsEnabled && !BuildVars.LOGS_ENABLED) {
                int oldSendLogsRow = sendLogsRow;
                int oldClearLogsRow = clearLogsRow;
                sendLogsRow = -1;
                clearLogsRow = -1;
                listAdapter.notifyItemRangeRemoved(oldSendLogsRow, 2);
            }
        } else if (position == sendLogsRow) {
            sendLogs(false);
        } else if (position == clearLogsRow) {
            FileLog.cleanupLogs();
        } else if (position == datacenterStatusRow) {
            presentFragment(new DatacenterActivity(0));
        }
    }

    private void sendLogs(boolean clean) {
        ProfileActivity.sendLogs(getParentActivity(), clean);
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
                    }
                    break;
                case TYPE_DETAIL_SETTINGS:
                    TextDetailSettingsCell detailCell = (TextDetailSettingsCell) holder.itemView;
                    if (position == versionRow) {
                        detailCell.setMultilineDetail(true);
                        detailCell.setTextAndValue(getSimpleVersion(), getString(R.string.NaxAboutDesc), false);
                    }
                    break;
                case TYPE_TEXT:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == toggleLogsRow) {
                        textCell.setTextAndIcon(BuildVars.LOGS_ENABLED ? getString(R.string.DebugMenuDisableLogs) : getString(R.string.DebugMenuEnableLogs), R.drawable.bug, sendLogsRow != -1);
                    } else if (position == sendLogsRow) {
                        textCell.setTextAndIcon(getString(R.string.DebugSendLogs), R.drawable.ic_upward, true);
                    } else if (position == clearLogsRow) {
                        textCell.setTextAndIcon(getString(R.string.DebugClearLogs), R.drawable.msg_clear, false);
                    } else if (position == datacenterStatusRow) {
                        textCell.setTextAndIcon(getString(R.string.DatacenterStatus), R.drawable.msg_info, false);
                    }
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == infoHeaderRow) {
                return TYPE_HEADER;
            } else if (position == versionRow) {
                return TYPE_DETAIL_SETTINGS;
            } else if (position == linksShadowRow) {
                return TYPE_SHADOW;
            } else {
                return TYPE_TEXT;
            }
        }
    }
}
