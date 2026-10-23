package com.gathin.tvhome;

/**
 * How the top-bar Screensaver row starts a dream.
 *
 * Apps cannot call DreamManager (WRITE_DREAM_STATE is privileged), so the row
 * asks a system component to do it. TV Settings exports DaydreamVoiceAction
 * for the "go to sleep" voice action; it starts the configured dream and
 * finishes. Older Android TV builds (the Shield) also ship SystemUI's
 * Somnambulator. Settings opens only when the screensaver is off or neither
 * component starts.
 *
 * Do not use TV Settings' DaydreamActivity: on Google TV 14 it finishes at
 * once, so the user lands on whatever Settings page was open last.
 */
public final class Screensaver {

    /** TV Settings voice action that calls DreamBackend.startDreaming(). */
    public static final String ACTION_SLEEP = "com.google.android.pano.action.SLEEP";
    public static final String VOICE_ACTION = "com.android.tv.settings/.device.display.daydream.DaydreamVoiceAction";
    public static final String SOMNAMBULATOR = "com.android.systemui/.Somnambulator";

    private Screensaver() {
    }

    /**
     * @param enabled    Settings.Secure "screensaver_enabled", or null when unreadable
     * @param components Settings.Secure "screensaver_components", or null when unreadable
     * @return false only when the settings say no dream would start
     */
    public static boolean dreamConfigured(String enabled, String components) {
        if (enabled != null && enabled.trim().equals("0")) return false;
        if (components == null) return true;
        String c = components.trim();
        return !(c.isEmpty() || c.equals("null"));
    }

    /** Components to try in order. Settings is the caller's last resort. */
    public static String[] starters() {
        return new String[]{VOICE_ACTION, SOMNAMBULATOR};
    }
}
