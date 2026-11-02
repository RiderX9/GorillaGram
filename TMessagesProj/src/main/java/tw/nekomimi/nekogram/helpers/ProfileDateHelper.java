package tw.nekomimi.nekogram.helpers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ProfileDateHelper {
    private static String JSON_FILE = "id_date.json";
    private static final ArrayList<ProfileDateData> profileDateDataList = new ArrayList<>();

    private static void loadData() {
        try {
            InputStream in = ApplicationLoader.applicationContext.getAssets().open(JSON_FILE);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[10240];
            int c;
            while ((c = in.read(buffer)) != -1) {
                bos.write(buffer, 0, c);
            }
            bos.close();
            in.close();
            String json = bos.toString("UTF-8");
            JSONObject object = new JSONObject(json);
            JSONArray data = object.getJSONArray("data");
            profileDateDataList.clear();
            for (int i = 0; i<data.length(); i++){
                JSONObject o = data.getJSONObject(i);
                profileDateDataList.add(new ProfileDateData(o.getLong("id"), o.getLong("date")));
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static String getUserTime(String prefix, long date) {
        String st = LocaleController.formatString(R.string.formatDateAtTime, LocaleController.getInstance().getFormatterYear().format(new Date(date)), LocaleController.getInstance().getFormatterDay().format(new Date(date)));
        return prefix + " " + st;
    }

    public static String getUserTime(Long userId) {
        if (profileDateDataList.isEmpty()) {
            loadData();
        }
        if (profileDateDataList.isEmpty()) {
            return "unknown";
        }
        for (int i = 1; i < profileDateDataList.size(); i++){
            ProfileDateData data1 = profileDateDataList.get(i - 1);
            ProfileDateData data2 = profileDateDataList.get(i);
            if (userId >= data1.getId() && userId <= data2.getId()) {
                long idx = userId - data1.getId();
                long idxRange = data2.getId() - data1.getId();
                double t = (double) idx / idxRange;
                long date1 = data1.getDate();
                long date2 = data2.getDate();
                double date = (date1 + t * (date2 - date1)) * 1000.0;
                long dateLong = Math.round(date);
                return getUserTime("~", dateLong);
            }
        }
        if (userId <= 1000000) {
            return getUserTime("=", 1380326400000L);
        }
        return getUserTime(">", 1711889200000L);
    }

    public static String getRegistrationDate(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        if (profileDateDataList.isEmpty()) {
            loadData();
        }
        if (profileDateDataList.isEmpty()) {
            return null;
        }
        for (int i = 1; i < profileDateDataList.size(); i++) {
            ProfileDateData data1 = profileDateDataList.get(i - 1);
            ProfileDateData data2 = profileDateDataList.get(i);
            if (userId >= data1.getId() && userId <= data2.getId()) {
                long idx = userId - data1.getId();
                long idxRange = data2.getId() - data1.getId();
                double t = (double) idx / idxRange;
                long date1 = data1.getDate();
                long date2 = data2.getDate();
                double date = (date1 + t * (date2 - date1)) * 1000.0;
                long dateLong = Math.round(date);
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", LocaleController.getInstance().getCurrentLocale());
                    return sdf.format(new Date(dateLong));
                } catch (Exception e) {
                    return null;
                }
            }
        }
        if (userId <= 1000000) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", LocaleController.getInstance().getCurrentLocale());
                return sdf.format(new Date(1380326400000L));
            } catch (Exception e) {
                return null;
            }
        }
        if (profileDateDataList.size() >= 2) {
            ProfileDateData last = profileDateDataList.get(profileDateDataList.size() - 1);
            ProfileDateData prev = profileDateDataList.get(profileDateDataList.size() - 2);
            long idx = userId - last.getId();
            long idxRange = last.getId() - prev.getId();
            double t = (double) idx / idxRange;
            double date = (last.getDate() + t * (last.getDate() - prev.getDate())) * 1000.0;
            long dateLong = Math.min(System.currentTimeMillis(), Math.round(date));
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", LocaleController.getInstance().getCurrentLocale());
                return sdf.format(new Date(dateLong));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static class ProfileDateData {
        private final long id;
        private final long date;

        public ProfileDateData(long id, long date) {
            this.id = id;
            this.date = date;
        }

        public long getId() {
            return id;
        }

        public long getDate() {
            return date;
        }
    }
}
