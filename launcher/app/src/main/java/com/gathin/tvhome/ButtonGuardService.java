package com.gathin.tvhome;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Opt-in filter for remote app-shortcut keys. Does nothing until
 * Settings.Global "tvhome_button_map" is set (see tools/launchers.py).
 * Navigation, volume, and power are never consumed.
 *
 * Map format, comma-separated: keycode=none, keycode=home, keycode=app:some.package
 * "none" swallows the key. "home" opens this launcher. "app:" launches that package.
 * Settings.Global "tvhome_button_log"=1 logs keycodes to logcat tag TvHomeButtons
 * and does not swallow unmapped keys.
 */
public class ButtonGuardService extends AccessibilityService {

    static final String TAG = "TvHomeButtons";
    static final String MAP_SETTING = "tvhome_button_map";
    static final String LOG_SETTING = "tvhome_button_log";

    /** Same denylist as tools/launchers.py. A bad map must not trap the remote. */
    private static final Set<Integer> NEVER = new HashSet<>();

    static {
        Collections.addAll(NEVER,
                KeyEvent.KEYCODE_HOME,
                KeyEvent.KEYCODE_BACK,
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_VOLUME_MUTE,
                KeyEvent.KEYCODE_POWER,
                KeyEvent.KEYCODE_MENU,
                KeyEvent.KEYCODE_SEARCH);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        try {
            return handle(event);
        } catch (RuntimeException ex) {
            Log.w(TAG, "ignored key after an error", ex);
            return false;
        }
    }

    private boolean handle(KeyEvent event) {
        int code = event.getKeyCode();
        boolean log = "1".equals(Settings.Global.getString(getContentResolver(), LOG_SETTING));
        if (log && event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            Log.i(TAG, "keyCode=" + code);
        }
        if (NEVER.contains(code)) return false;
        String action = parseMap(Settings.Global.getString(getContentResolver(), MAP_SETTING)).get(code);
        if (action == null) return false;
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            if ("home".equals(action)) openHome();
            else if (action.startsWith("app:")) openApp(action.substring(4));
        }
        return true;
    }

    /** Visible for the format shared with tools/launchers.py. Empty and broken entries are skipped. */
    static Map<Integer, String> parseMap(String raw) {
        Map<Integer, String> out = new HashMap<>();
        if (raw == null) return out;
        for (String part : raw.split(",")) {
            String piece = part.trim();
            int eq = piece.indexOf('=');
            if (eq <= 0) continue;
            int code;
            try {
                code = Integer.parseInt(piece.substring(0, eq).trim());
            } catch (NumberFormatException ex) {
                continue;
            }
            if (NEVER.contains(code)) continue;
            String action = piece.substring(eq + 1).trim();
            if ("none".equals(action) || "home".equals(action) || isAppAction(action)) {
                out.put(code, action);
            }
        }
        return out;
    }

    private static boolean isAppAction(String action) {
        if (!action.startsWith("app:") || action.length() < 6) return false;
        String pkg = action.substring(4);
        return pkg.indexOf('.') > 0 && pkg.indexOf(' ') < 0 && pkg.indexOf('/') < 0;
    }

    private void openHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.setPackage(getPackageName());
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(home);
    }

    private void openApp(String pkg) {
        Intent launch = getPackageManager().getLeanbackLaunchIntentForPackage(pkg);
        if (launch == null) launch = getPackageManager().getLaunchIntentForPackage(pkg);
        if (launch == null) {
            Log.i(TAG, "no activity for " + pkg);
            return;
        }
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(launch);
    }
}
