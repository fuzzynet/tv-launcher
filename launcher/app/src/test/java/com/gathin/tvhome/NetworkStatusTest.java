package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Pill icon choice. Resource ids only; no window or ConnectivityManager. */
public class NetworkStatusTest {

    @Test
    public void ethernetBeatsWifiOnTheActiveNetwork() {
        assertEquals(NetworkStatus.Kind.ETHERNET, NetworkStatus.kind(true, true, true));
        assertEquals(NetworkStatus.Kind.ETHERNET, NetworkStatus.kind(true, true, false));
        assertEquals(R.drawable.ic_ethernet, NetworkStatus.icon(NetworkStatus.Kind.ETHERNET, 0));
    }

    @Test
    public void wifiUsesSignalBarsAndOfflineUsesTheEmptyIcon() {
        assertEquals(NetworkStatus.Kind.WIFI, NetworkStatus.kind(true, false, true));
        assertEquals(NetworkStatus.Kind.OFFLINE, NetworkStatus.kind(false, false, false));
        assertEquals(NetworkStatus.Kind.OFFLINE, NetworkStatus.kind(false, true, true));
        assertEquals(R.drawable.ic_offline, NetworkStatus.icon(NetworkStatus.Kind.OFFLINE, 4));
        assertEquals(R.drawable.ic_wifi_0, NetworkStatus.icon(NetworkStatus.Kind.WIFI, 0));
        assertEquals(R.drawable.ic_wifi_2, NetworkStatus.icon(NetworkStatus.Kind.WIFI, 2));
        assertEquals(R.drawable.ic_wifi_4, NetworkStatus.icon(NetworkStatus.Kind.WIFI, 4));
        assertEquals(R.drawable.ic_wifi_4, NetworkStatus.icon(NetworkStatus.Kind.WIFI, 9));
    }

    @Test
    public void anotherTransportDoesNotLookDisconnected() {
        assertEquals(NetworkStatus.Kind.WIFI, NetworkStatus.kind(true, false, false));
    }

    @Test
    public void rssiBucketsMatchTheFiveBarWindow() {
        assertEquals(4, NetworkStatus.wifiLevel(NetworkStatus.UNKNOWN_RSSI));
        assertEquals(4, NetworkStatus.wifiLevel(0));
        assertEquals(0, NetworkStatus.wifiLevel(-100));
        assertEquals(0, NetworkStatus.wifiLevel(-90));
        assertEquals(2, NetworkStatus.wifiLevel(-70));
        assertEquals(4, NetworkStatus.wifiLevel(-55));
        assertEquals(4, NetworkStatus.wifiLevel(-40));
    }

    @Test
    public void hiddenSsidFallsBack() {
        assertEquals("Home", NetworkStatus.wifiLabel("\"Home\"", "Wi-Fi"));
        assertEquals("Wi-Fi", NetworkStatus.wifiLabel("<unknown ssid>", "Wi-Fi"));
        assertEquals("Wi-Fi", NetworkStatus.wifiLabel("0x", "Wi-Fi"));
        assertEquals("Wi-Fi", NetworkStatus.wifiLabel(null, "Wi-Fi"));
    }

    @Test
    public void vpnBadgeOnlyWhileOnlineWithATunnel() {
        org.junit.Assert.assertTrue(NetworkStatus.showVpn(true, true));
        org.junit.Assert.assertFalse(NetworkStatus.showVpn(true, false));
        org.junit.Assert.assertFalse(NetworkStatus.showVpn(false, true));
    }

    @Test
    public void vpnOffBadgeIsOptInAndNeverReplacesTheConnectedPadlock() {
        org.junit.Assert.assertFalse(NetworkStatus.showVpnOff(false, true, false));
        org.junit.Assert.assertFalse(NetworkStatus.showVpnOff(true, false, false));
        org.junit.Assert.assertFalse(NetworkStatus.showVpnOff(true, true, true));
        org.junit.Assert.assertTrue(NetworkStatus.showVpnOff(true, true, false));
    }

    @Test
    public void tunnelInterfacesIgnoreTheKernelStubs() {
        org.junit.Assert.assertTrue(NetworkStatus.isTunnelInterface("tun0"));
        org.junit.Assert.assertTrue(NetworkStatus.isTunnelInterface("wg0"));
        org.junit.Assert.assertTrue(NetworkStatus.isTunnelInterface("ppp1"));
        org.junit.Assert.assertTrue(NetworkStatus.isTunnelInterface("ipsec0"));
        org.junit.Assert.assertFalse(NetworkStatus.isTunnelInterface("tunl0"));
        org.junit.Assert.assertFalse(NetworkStatus.isTunnelInterface("ip6tnl0"));
        org.junit.Assert.assertFalse(NetworkStatus.isTunnelInterface("ip6_vti0"));
        org.junit.Assert.assertFalse(NetworkStatus.isTunnelInterface("wlan0"));
        org.junit.Assert.assertFalse(NetworkStatus.isTunnelInterface(null));
    }
}
