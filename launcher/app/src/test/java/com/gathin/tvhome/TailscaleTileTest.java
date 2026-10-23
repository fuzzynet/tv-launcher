package com.gathin.tvhome;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tailscale stays on the tile until a connection, then the stored flag keeps it off. */
public class TailscaleTileTest {

    private static byte[] ip(int a, int b, int c, int d) {
        return new byte[] {(byte) a, (byte) b, (byte) c, (byte) d};
    }

    private static TailscaleTile.IfaceAddress on(String iface, int a, int b, int c, int d) {
        return new TailscaleTile.IfaceAddress(iface, ip(a, b, c, d));
    }

    @Test
    public void carrierGradeRangeIs100_64Slash10() {
        assertTrue(TailscaleTile.inCarrierGradeRange(ip(100, 64, 0, 0)));
        assertTrue(TailscaleTile.inCarrierGradeRange(ip(100, 64, 1, 2)));
        assertTrue(TailscaleTile.inCarrierGradeRange(ip(100, 127, 255, 255)));
        assertFalse(TailscaleTile.inCarrierGradeRange(ip(100, 63, 255, 255)));
        assertFalse(TailscaleTile.inCarrierGradeRange(ip(100, 128, 0, 0)));
        assertFalse(TailscaleTile.inCarrierGradeRange(ip(10, 0, 0, 1)));
        assertFalse(TailscaleTile.inCarrierGradeRange(new byte[] {100, 64, 0}));
        assertFalse(TailscaleTile.inCarrierGradeRange(null));
    }

    @Test
    public void onlyATunInterfaceCountsForTheAddress() {
        assertTrue(TailscaleTile.isTunInterface("tun0"));
        assertTrue(TailscaleTile.isTunInterface("tun10"));
        assertFalse(TailscaleTile.isTunInterface("tunl0"));
        assertFalse(TailscaleTile.isTunInterface("wg0"));
        assertFalse(TailscaleTile.isTunInterface("wlan0"));
        assertFalse(TailscaleTile.isTunInterface(null));
        assertTrue(TailscaleTile.connected(false, Collections.singletonList(on("tun0", 100, 100, 1, 2))));
        assertFalse(TailscaleTile.connected(false, Collections.singletonList(on("wlan0", 100, 100, 1, 2))));
        assertFalse(TailscaleTile.connected(false, Collections.singletonList(on("tun0", 10, 1, 2, 3))));
        assertFalse(TailscaleTile.connected(false, Collections.singletonList(on("wg0", 100, 100, 1, 2))));
    }

    @Test
    public void vpnTransportAloneCountsAndEitherSignalIsEnough() {
        assertTrue(TailscaleTile.connected(true, Collections.emptyList()));
        assertTrue(TailscaleTile.connected(true, null));
        assertFalse(TailscaleTile.connected(false, null));
        assertFalse(TailscaleTile.connected(false, Collections.emptyList()));
        assertTrue(TailscaleTile.connected(false, Arrays.asList(
                on("wlan0", 10, 1, 2, 20),
                on("tun0", 100, 64, 0, 1))));
    }

    @Test
    public void everyBuildHidesAfterTheFirstConnectionAndStaysHidden() {
        assertFalse(TailscaleTile.shouldHide(false, false));
        assertTrue(TailscaleTile.shouldHide(false, true));
        assertTrue(TailscaleTile.shouldPersist(false, true));
        assertTrue(TailscaleTile.shouldHide(true, false));
        assertFalse(TailscaleTile.shouldPersist(true, true));
        assertFalse(TailscaleTile.shouldPersist(true, false));
    }
}
