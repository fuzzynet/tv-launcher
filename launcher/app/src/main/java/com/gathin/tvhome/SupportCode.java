package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Optional label under the last app row.
 * Letters, digits and hyphens are shown, at most {@link #MAX_LENGTH} characters.
 * An empty result hides the footer.
 *
 * <p>Set {@code "support_code"} in config.json. This build does not accept a code from another app.
 */
final class SupportCode {

    /** Key in config.json. Not a Settings preference. */
    static final String CONFIG_KEY = "support_code";

    static final String PREFS = "support_code";
    static final String PREF_CODE = "code";
    static final int MAX_LENGTH = 16;

    private SupportCode() {}

    /**
     * What the chip shows, or {@code ""} when it should stay hidden.
     * Letters are uppercased. Anything except {@code A-Z}, {@code 0-9} and {@code -} is dropped.
     */
    static String display(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return "";
        StringBuilder out = new StringBuilder(Math.min(trimmed.length(), MAX_LENGTH));
        for (int i = 0; i < trimmed.length() && out.length() < MAX_LENGTH; i++) {
            char c = trimmed.charAt(i);
            if (c >= 'a' && c <= 'z') c = (char) (c - 'a' + 'A');
            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-') out.append(c);
        }
        return out.toString();
    }

    /**
     * Preferences win when the key has been written, even if the stored value is empty.
     * {@code fromConfig} is the {@code support_code} string from config.json, or {@code ""}.
     */
    static String resolve(boolean prefsPresent, String prefValue, String fromConfig) {
        return display(prefsPresent ? prefValue : fromConfig);
    }

    static String resolve(Context ctx, String fromConfig) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean present = prefs.contains(PREF_CODE);
        return resolve(present, present ? prefs.getString(PREF_CODE, "") : "", fromConfig);
    }
}
