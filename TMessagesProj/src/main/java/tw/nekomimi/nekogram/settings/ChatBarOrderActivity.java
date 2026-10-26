package tw.nekomimi.nekogram.settings;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;

import java.util.ArrayList;
import java.util.List;

import tw.nekomimi.nekogram.helpers.ChatBarOrderHelper;
import tw.nekomimi.nekogram.ui.cells.HeaderCell;

public class ChatBarOrderActivity extends BaseReorderManagerActivity {

    private static final int MENU_RESET = 1;

    private int activeHeaderRow;
    private int activeRowStart;
    private int activeRowEnd;
    private int activeInfoRow;

    private int hiddenHeaderRow;
    private int hiddenRowStart;
    private int hiddenRowEnd;
    private int hiddenInfoRow;

    private final List<Integer> activeItems = new ArrayList<>();
    private final List<Integer> hiddenItems = new ArrayList<>();

    private ActionBarMenuItem resetItem;
    private ListAdapter listAdapter;

    @Override
    protected String getActionBarTitle() {
        return getString(R.string.ChatBarElements);
    }

    @Override
    protected String getKey() {
        return "chat_bar_order";
    }

    @Override
    public boolean onFragmentCreate() {
        reloadData();
        return super.onFragmentCreate();
    }

    private void reloadData() {
        activeItems.clear();
        activeItems.addAll(ChatBarOrderHelper.getActiveItems());
        hiddenItems.clear();
        hiddenItems.addAll(ChatBarOrderHelper.getHiddenItems());
    }

    @Override
    public View createView(Context context) {
        View view = super.createView(context);

        resetItem = addResetMenuItem(MENU_RESET);
        resetItem.setOnClickListener(v -> resetToDefault());
        updateResetButtonVisibility();

        attachReorder(new ReorderDelegate() {
            @Override
            public boolean isDraggable(int position) {
                return isInActiveSection(position);
            }

            @Override
            public boolean isSameSection(int from, int to) {
                return isInActiveSection(from) && isInActiveSection(to);
            }

            @Override
            public void onMove(int from, int to) {
                if (isInActiveSection(from) && isInActiveSection(to)) {
                    int a = from - activeRowStart;
                    int b = to - activeRowStart;
                    if (a >= 0 && a < activeItems.size() && b >= 0 && b < activeItems.size()) {
                        Integer moved = activeItems.remove(a);
                        activeItems.add(b, moved);
                        listAdapter.notifyItemMoved(from, to);
                    }
                }
            }

            @Override
            public void onReorderFinished() {
                saveData();
                updateResetButtonVisibility();
                listAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (isInActiveSection(position)) {
            int index = position - activeRowStart;
            if (index >= 0 && index < activeItems.size()) {
                if (activeItems.size() <= 1) {
                    return;
                }
                Integer item = activeItems.remove(index);
                hiddenItems.add(item);
                saveData();
                updateRows();
                updateResetButtonVisibility();
            }
        } else if (isInHiddenSection(position)) {
            int index = position - hiddenRowStart;
            if (index >= 0 && index < hiddenItems.size()) {
                Integer item = hiddenItems.remove(index);
                activeItems.add(item);
                saveData();
                updateRows();
                updateResetButtonVisibility();
            }
        }
    }

    private void saveData() {
        ChatBarOrderHelper.save(activeItems, hiddenItems);
    }

    private void resetToDefault() {
        ChatBarOrderHelper.resetToDefault();
        reloadData();
        updateRows();
        updateResetButtonVisibility();
    }

    private void updateResetButtonVisibility() {
        updateResetButtonVisibility(resetItem, ChatBarOrderHelper.isDefault());
    }

    private boolean isInActiveSection(int position) {
        return activeRowStart != -1 && position >= activeRowStart && position <= activeRowEnd;
    }

    private boolean isInHiddenSection(int position) {
        return hiddenRowStart != -1 && position >= hiddenRowStart && position <= hiddenRowEnd;
    }

    @Override
    protected BaseListAdapter createAdapter(Context context) {
        listAdapter = new ListAdapter(context);
        return listAdapter;
    }

    @Override
    protected void updateRows() {
        rebuildRows();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private void rebuildRows() {
        rowCount = 0;

        activeHeaderRow = -1;
        activeRowStart = -1;
        activeRowEnd = -1;
        activeInfoRow = -1;

        hiddenHeaderRow = -1;
        hiddenRowStart = -1;
        hiddenRowEnd = -1;
        hiddenInfoRow = -1;

        activeHeaderRow = rowCount++;
        if (!activeItems.isEmpty()) {
            activeRowStart = rowCount;
            rowCount += activeItems.size();
            activeRowEnd = rowCount - 1;
        }
        activeInfoRow = rowCount++;

        if (!hiddenItems.isEmpty()) {
            hiddenHeaderRow = rowCount++;
            hiddenRowStart = rowCount;
            rowCount += hiddenItems.size();
            hiddenRowEnd = rowCount - 1;
            hiddenInfoRow = rowCount++;
        }
    }

    private class ListAdapter extends BaseListAdapter {

        public ListAdapter(Context context) {
            super(context);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
            clearRowBackgroundForRoundedSection(holder, viewType);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int viewType = holder.getItemViewType();
            if (viewType == TYPE_HEADER) {
                HeaderCell cell = (HeaderCell) holder.itemView;
                if (position == activeHeaderRow) {
                    cell.setText(getString(R.string.ChatBarActiveElements));
                } else if (position == hiddenHeaderRow) {
                    cell.setText(getString(R.string.ChatBarHiddenElements));
                }
            } else if (viewType == TYPE_TEXT) {
                TextCell cell = (TextCell) holder.itemView;
                int itemId = -1;
                boolean isActive = false;
                if (isInActiveSection(position)) {
                    itemId = activeItems.get(position - activeRowStart);
                    isActive = true;
                } else if (isInHiddenSection(position)) {
                    itemId = hiddenItems.get(position - hiddenRowStart);
                    isActive = false;
                }

                ChatBarOrderHelper.Entry entry = ChatBarOrderHelper.getEntry(itemId);
                if (entry != null) {
                    cell.setTextAndIcon(getString(entry.titleRes), entry.iconRes, false);
                }

                if (isActive) {
                    applyReorderHandle(cell);
                } else {
                    if (cell.getValueImageView() != null) {
                        cell.getValueImageView().setVisibility(View.GONE);
                    }
                }
            } else if (viewType == TYPE_INFO_PRIVACY) {
                TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                if (position == activeInfoRow) {
                    cell.setText(getString(R.string.ChatBarReorderHint));
                } else if (position == hiddenInfoRow) {
                    cell.setText("");
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == activeHeaderRow || position == hiddenHeaderRow) {
                return TYPE_HEADER;
            } else if (position == activeInfoRow || position == hiddenInfoRow) {
                return TYPE_INFO_PRIVACY;
            }
            return TYPE_TEXT;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }
    }
}
