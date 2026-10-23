package com.gathin.tvhome;

/**
 * What the top-bar network pill should draw.
 *
 * Ethernet wins when the active network reports both transports. No active
 * network is the offline icon. A connection that is neither Ethernet nor
 * Wi-Fi keeps the Wi-Fi icon so a VPN does not look disconnected.
 * RSSI uses the same -100..-55 window as the platform signal buckets, in
 * five steps. An unreadable RSSI stays on the full icon.
 */
public final class NetworkStatus {

    public enum Kind {
        ETHERNET,
        WIFI,
        OFFLINE
    }

    /** getRssi() returns this when Wi-Fi is not associated. */
    public static final int UNKNOWN_RSSI = -127;
    static final int MIN_RSSI = -100;
    static final int MAX_RSSI = -55;
    public static final int LEVELS = 5;

    private NetworkStatus() {
    }

    public static Kind kind(boolean hasNetwork, boolean ethernet, boolean wifi) {
        if (!hasNetwork) return Kind.OFFLINE;
        if (ethernet) return Kind.ETHERNET;
        if (wifi) return Kind.WIFI;
        return Kind.WIFI;
    }

    /** 0 is the weakest bar, 4 is full. Unknown or impossible readings are full. */
    public static int wifiLevel(int rssi) {
        if (rssi >= 0 || rssi <= UNKNOWN_RSSI) return LEVELS - 1;
        if (rssi <= MIN_RSSI) return 0;
        if (rssi >= MAX_RSSI) return LEVELS - 1;
        return (int) ((rssi - MIN_RSSI) * (LEVELS - 1) / (float) (MAX_RSSI - MIN_RSSI));
    }

    public static int icon(Kind kind, int wifiLevel) {
        if (kind == Kind.ETHERNET) return R.drawable.ic_ethernet;
        if (kind == Kind.OFFLINE) return R.drawable.ic_offline;
        int level = Math.max(0, Math.min(LEVELS - 1, wifiLevel));
        switch (level) {
            case 0: return R.drawable.ic_wifi_0;
            case 1: return R.drawable.ic_wifi_1;
            case 2: return R.drawable.ic_wifi_2;
            case 3: return R.drawable.ic_wifi_3;
            default: return R.drawable.ic_wifi_4;
        }
    }

    /** The VPN badge shows only while online and some VPN network or tunnel is up. */
    public static boolean showVpn(boolean hasNetwork, boolean vpnUp) {
        return hasNetwork && vpnUp;
    }

    /**
     * The red "VPN off" mark. Opt-in, and never while the connected padlock is showing.
     * {@code vpn_app} does not turn this on.
     */
    public static boolean showVpnOff(boolean optedIn, boolean hasNetwork, boolean vpnUp) {
        return optedIn && hasNetwork && !vpnUp;
    }

    /** tun0, wg0, ppp0, ipsec0 and similar. Not the always-present tunl0 / ip6tnl0 / ip6_vti0 stubs. */
    public static boolean isTunnelInterface(String name) {
        if (name == null) return false;
        return name.matches("(tun|wg|ppp|utun)\\d+|ipsec\\d*");
    }

    /** SSID for the pill, or the fallback when the platform hides it. */
    public static String wifiLabel(String raw, String fallback) {
        if (raw == null) return fallback;
        String ssid = raw.replace("\"", "").trim();
        if (ssid.isEmpty() || ssid.contains("unknown") || ssid.equals("0x")) return fallback;
        return ssid;
    }
}
