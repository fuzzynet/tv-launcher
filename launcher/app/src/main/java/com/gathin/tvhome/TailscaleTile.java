package com.gathin.tvhome;

import java.util.List;

/**
 * When the Tailscale tile ({@code com.tailscale.ipn}) leaves the home screen.
 *
 * <p>Every flavor shows it until this launcher has seen Tailscale connected,
 * then keeps it hidden. Connected means an active VPN transport, or an IPv4
 * address in Tailscale's carrier-grade range on a {@code tun} interface.
 * Either one is enough. The stored flag is not cleared when the tunnel later drops.
 */
final class TailscaleTile {

    static final String PACKAGE = "com.tailscale.ipn";
    static final String PREFS = "tailscale_tile";
    static final String PREF_HIDDEN = "hidden";

    private TailscaleTile() {}

    /** One IPv4 address observed on a network interface. */
    static final class IfaceAddress {
        final String iface;
        final byte[] ipv4;

        IfaceAddress(String iface, byte[] ipv4) {
            this.iface = iface;
            this.ipv4 = ipv4;
        }
    }

    /** Tailscale carrier-grade range. Four octets, or no. */
    static boolean inCarrierGradeRange(byte[] ipv4) {
        if (ipv4 == null || ipv4.length != 4) return false;
        int first = ipv4[0] & 0xff;
        int second = ipv4[1] & 0xff;
        return first == 100 && second >= 64 && second <= 127;
    }

    /** {@code tun0}, {@code tun10}. Not {@code tunl0} or {@code wg0}. */
    static boolean isTunInterface(String name) {
        return name != null && name.matches("tun\\d+");
    }

    /**
     * True when a VPN transport is up, or some address is in the Tailscale
     * range on a tun interface.
     */
    static boolean connected(boolean vpnTransport, List<IfaceAddress> addresses) {
        if (vpnTransport) return true;
        if (addresses == null) return false;
        for (IfaceAddress address : addresses) {
            if (isTunInterface(address.iface) && inCarrierGradeRange(address.ipv4)) return true;
        }
        return false;
    }

    /** Hide after a connection or a stored flag. The same rule on every flavor. */
    static boolean shouldHide(boolean stored, boolean connectedNow) {
        return stored || connectedNow;
    }

    /** Write the flag the first time any build sees a connection. */
    static boolean shouldPersist(boolean stored, boolean connectedNow) {
        return !stored && connectedNow;
    }
}
