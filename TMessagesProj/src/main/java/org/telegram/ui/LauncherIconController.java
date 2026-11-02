package org.telegram.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class LauncherIconController {
    public static void tryFixLauncherIconIfNeeded() {
        android.content.SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences("gorillagram_app", Context.MODE_PRIVATE);
        boolean initialized = prefs.getBoolean("gorilla_icon_initialized_v4", false);
        if (!initialized) {
            String[] oldAliases = {
                "GoogleIcon", "ColorfulIcon", "DarkGreenIcon", "NeonIcon",
                "NielloIcon", "BlueIcon", "DarkBlueIcon", "BlurBlueIcon"
            };
            for (String alias : oldAliases) {
                try {
                    ComponentName cn = new ComponentName(ApplicationLoader.applicationContext.getPackageName(), "org.telegram.messenger." + alias);
                    ApplicationLoader.applicationContext.getPackageManager().setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                } catch (Exception ignored) {}
            }
            setIcon(LauncherIcon.DEFAULT);
            prefs.edit().putBoolean("gorilla_icon_initialized_v4", true).apply();
            return;
        }

        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }

        setIcon(LauncherIcon.DEFAULT);
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || (i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT);
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        PackageManager pm = ctx.getPackageManager();
        for (LauncherIcon i : LauncherIcon.values()) {
            pm.setComponentEnabledSetting(i.getComponentName(ctx), i == icon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public enum LauncherIcon {
        DEFAULT("DefaultIcon", R.drawable.icon_gorilla_background, R.mipmap.icon_foreground, R.string.AppIconDefault),
        RED("RedIcon", R.drawable.icon_red_background, R.mipmap.icon_foreground, R.string.AppIconRed),
        TELEGRAM("TelegramIcon", R.drawable.icon_background, R.mipmap.icon_foreground, R.string.AppIconTelegram),
        VINTAGE("VintageIcon", R.drawable.icon_6_background_sa, R.mipmap.icon_6_foreground_sa, R.string.AppIconVintage),
        AQUA("AquaIcon", R.drawable.icon_4_background_sa, R.mipmap.icon_foreground_sa, R.string.AppIconAqua),
        PREMIUM("PremiumIcon", R.drawable.icon_3_background_sa, R.mipmap.icon_3_foreground_sa, R.string.AppIconPremium),
        TURBO("TurboIcon", R.drawable.icon_5_background_sa, R.mipmap.icon_5_foreground_sa, R.string.AppIconTurbo),
        NOX("NoxIcon", R.mipmap.icon_2_background_sa, R.mipmap.icon_foreground_sa, R.string.AppIconNox);

        public final String key;
        public final int background;
        public final int foreground;
        public final int title;
        public final boolean premium;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), "org.telegram.messenger." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int background, int foreground, int title) {
            this(key, background, foreground, title, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.premium = premium;
        }

        public boolean isNekoX() {
            return false;
        }
    }
}
