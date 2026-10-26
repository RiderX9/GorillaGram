package tw.nekomimi.nekogram.helpers;

import android.content.SharedPreferences;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatBarOrderHelper {

    public static final int ID_EMOJI = 0;
    public static final int ID_ATTACH = 1;
    public static final int ID_MIC_SEND = 2;
    public static final int ID_AI = 3;

    private static final String PREF_ACTIVE = "chat_bar_active_items";
    private static final String PREF_HIDDEN = "chat_bar_hidden_items";

    public static class Entry {
        public final int id;
        @StringRes public final int titleRes;
        @DrawableRes public final int iconRes;

        public Entry(int id, @StringRes int titleRes, @DrawableRes int iconRes) {
            this.id = id;
            this.titleRes = titleRes;
            this.iconRes = iconRes;
        }
    }

    public static final List<Entry> ALL_ENTRIES = Arrays.asList(
            new Entry(ID_EMOJI, R.string.ChatBarItemEmoji, R.drawable.smiles_tab_smiles),
            new Entry(ID_ATTACH, R.string.ChatBarItemAttach, R.drawable.msg_input_attach2),
            new Entry(ID_MIC_SEND, R.string.ChatBarItemMicSend, R.drawable.msg_voice),
            new Entry(ID_AI, R.string.ChatBarItemAI, R.drawable.msg_bot)
    );

    public static Entry getEntry(int id) {
        for (Entry e : ALL_ENTRIES) {
            if (e.id == id) return e;
        }
        return null;
    }

    private static SharedPreferences getPrefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences("gorillagram_chat_bar", 0);
    }

    public static List<Integer> getActiveItems() {
        String saved = getPrefs().getString(PREF_ACTIVE, null);
        List<Integer> list = new ArrayList<>();
        if (saved != null && !saved.trim().isEmpty()) {
            for (String part : saved.split(",")) {
                try {
                    int val = Integer.parseInt(part.trim());
                    if (getEntry(val) != null && !list.contains(val)) {
                        list.add(val);
                    }
                } catch (Exception ignored) {}
            }
        }
        if (list.isEmpty()) {
            list = defaultActive();
        }
        return list;
    }

    public static List<Integer> getHiddenItems() {
        String saved = getPrefs().getString(PREF_HIDDEN, null);
        List<Integer> list = new ArrayList<>();
        if (saved != null && !saved.trim().isEmpty()) {
            for (String part : saved.split(",")) {
                try {
                    int val = Integer.parseInt(part.trim());
                    if (getEntry(val) != null && !list.contains(val)) {
                        list.add(val);
                    }
                } catch (Exception ignored) {}
            }
        }
        return list;
    }

    public static List<Integer> defaultActive() {
        return new ArrayList<>(Arrays.asList(ID_EMOJI, ID_ATTACH, ID_MIC_SEND, ID_AI));
    }

    public static List<Integer> defaultHidden() {
        return new ArrayList<>();
    }

    public static boolean isDefault() {
        List<Integer> active = getActiveItems();
        List<Integer> hidden = getHiddenItems();
        return active.equals(defaultActive()) && hidden.isEmpty();
    }

    public static void save(List<Integer> active, List<Integer> hidden) {
        StringBuilder sbActive = new StringBuilder();
        for (int i = 0; i < active.size(); i++) {
            if (i > 0) sbActive.append(",");
            sbActive.append(active.get(i));
        }
        StringBuilder sbHidden = new StringBuilder();
        for (int i = 0; i < hidden.size(); i++) {
            if (i > 0) sbHidden.append(",");
            sbHidden.append(hidden.get(i));
        }
        getPrefs().edit()
                .putString(PREF_ACTIVE, sbActive.toString())
                .putString(PREF_HIDDEN, sbHidden.toString())
                .apply();

        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface);
    }

    public static void resetToDefault() {
        getPrefs().edit()
                .remove(PREF_ACTIVE)
                .remove(PREF_HIDDEN)
                .apply();

        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface);
    }

    public static boolean isItemActive(int id) {
        return getActiveItems().contains(id);
    }
}
