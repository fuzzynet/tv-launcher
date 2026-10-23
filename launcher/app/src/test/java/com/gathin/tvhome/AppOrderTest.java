package com.gathin.tvhome;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Ordering and the preferences encoding. No Android framework. */
public class AppOrderTest {

    private static final int COLS = 4;

    private static AppOrder.App app(String pkg, String label) {
        return new AppOrder.App(pkg, label);
    }

    @Test
    public void missingConfigDockAppsAreSkippedAndTheRowFills() {
        // Netflix and YouTube are not installed. The dock still has four tiles.
        AppOrder.Layout layout = AppOrder.layout(
                Arrays.asList(
                        app("com.amazon.amazonvideo.livingroom.nvidia", "Prime Video"),
                        app("com.disney.disneyplus", "Disney+"),
                        app("com.plexapp.android", "Plex"),
                        app("com.spotify.tv.client", "Spotify"),
                        app("org.xbmc.kodi", "Kodi")),
                Arrays.asList(
                        "com.netflix.ninja",
                        "com.amazon.amazonvideo.livingroom.nvidia",
                        "com.google.android.youtube.tv",
                        "com.disney.disneyplus"),
                Collections.emptySet(),
                AppOrder.State.fresh(),
                COLS);
        assertEquals(Arrays.asList(
                "com.amazon.amazonvideo.livingroom.nvidia",
                "com.disney.disneyplus",
                "org.xbmc.kodi",
                "com.plexapp.android",
                "com.spotify.tv.client"), layout.packages);
        assertEquals(4, layout.dockCount);
        assertEquals(Arrays.asList(
                "com.amazon.amazonvideo.livingroom.nvidia",
                "com.disney.disneyplus"), layout.pinned);
    }

    @Test
    public void userDockReplacesConfigAndStillFillsTheRow() {
        AppOrder.State pinned = new AppOrder.State(
                Collections.emptyList(),
                Collections.singletonList("com.plexapp.android"),
                Collections.emptySet(),
                true);
        AppOrder.Layout layout = AppOrder.layout(
                Arrays.asList(
                        app("com.netflix.ninja", "Netflix"),
                        app("com.plexapp.android", "Plex"),
                        app("com.spotify.tv.client", "Spotify"),
                        app("org.xbmc.kodi", "Kodi"),
                        app("com.disney.disneyplus", "Disney+")),
                Collections.singletonList("com.netflix.ninja"),
                Collections.emptySet(),
                pinned,
                COLS);
        assertEquals(Arrays.asList(
                "com.plexapp.android",
                "com.disney.disneyplus",
                "org.xbmc.kodi",
                "com.netflix.ninja",
                "com.spotify.tv.client"), layout.packages);
        assertEquals(Collections.singletonList("com.plexapp.android"), layout.pinned);
        assertEquals(4, layout.dockCount);
    }

    @Test
    public void emptyUserDockDoesNotBringConfigBack() {
        AppOrder.State taken = new AppOrder.State(
                Arrays.asList("com.plexapp.android", "com.netflix.ninja"),
                Collections.emptyList(),
                Collections.emptySet(),
                true);
        AppOrder.Layout layout = AppOrder.layout(
                Arrays.asList(
                        app("com.netflix.ninja", "Netflix"),
                        app("com.plexapp.android", "Plex")),
                Collections.singletonList("com.netflix.ninja"),
                Collections.emptySet(),
                taken,
                COLS);
        assertEquals(Arrays.asList("com.plexapp.android", "com.netflix.ninja"), layout.packages);
        assertTrue(layout.pinned.isEmpty());
    }

    @Test
    public void newAppsAppendAtTheEndInLabelOrder() {
        AppOrder.State custom = new AppOrder.State(
                Arrays.asList("com.plexapp.android", "com.spotify.tv.client"),
                Collections.emptyList(),
                Collections.emptySet(),
                false);
        AppOrder.Layout layout = AppOrder.layout(
                Arrays.asList(
                        app("com.spotify.tv.client", "Spotify"),
                        app("org.xbmc.kodi", "Kodi"),
                        app("com.plexapp.android", "Plex"),
                        app("com.abc.app", "Alpha")),
                Collections.emptyList(),
                Collections.emptySet(),
                custom,
                COLS);
        assertEquals(Arrays.asList(
                "com.plexapp.android",
                "com.spotify.tv.client",
                "com.abc.app",
                "org.xbmc.kodi"), layout.packages);
    }

    @Test
    public void uninstalledAppsDropOut() {
        AppOrder.State saved = new AppOrder.State(
                Arrays.asList("com.plexapp.android", "com.gone.app", "com.spotify.tv.client"),
                Arrays.asList("com.gone.app", "com.plexapp.android"),
                Collections.emptySet(),
                true);
        AppOrder.State pruned = AppOrder.prune(saved, new LinkedHashSet<>(Arrays.asList(
                "com.plexapp.android", "com.spotify.tv.client")));
        assertEquals(Arrays.asList("com.plexapp.android", "com.spotify.tv.client"), pruned.order);
        assertEquals(Collections.singletonList("com.plexapp.android"), pruned.dock);
        assertTrue(pruned.userDock);
        AppOrder.Layout layout = AppOrder.layout(
                Arrays.asList(
                        app("com.plexapp.android", "Plex"),
                        app("com.spotify.tv.client", "Spotify")),
                Collections.emptyList(),
                Collections.emptySet(),
                pruned,
                COLS);
        assertEquals(Arrays.asList("com.plexapp.android", "com.spotify.tv.client"), layout.packages);
    }

    @Test
    public void hiddenAppsStayOutUntilReset() {
        SetHidden hidden = new SetHidden("com.google.android.tvlauncher");
        AppOrder.State state = AppOrder.hide(AppOrder.State.fresh(), "com.spotify.tv.client");
        AppOrder.Layout layout = AppOrder.layout(
                Arrays.asList(
                        app("com.google.android.tvlauncher", "Android TV Home"),
                        app("com.plexapp.android", "Plex"),
                        app("com.spotify.tv.client", "Spotify"),
                        app("org.xbmc.kodi", "Kodi")),
                Collections.emptyList(),
                hidden.names,
                state,
                COLS);
        assertEquals(Arrays.asList("org.xbmc.kodi", "com.plexapp.android"), layout.packages);
        AppOrder.Layout reset = AppOrder.layout(
                Arrays.asList(
                        app("com.google.android.tvlauncher", "Android TV Home"),
                        app("com.spotify.tv.client", "Spotify"),
                        app("org.xbmc.kodi", "Kodi")),
                Collections.emptyList(),
                hidden.names,
                AppOrder.reset(),
                COLS);
        assertEquals(Arrays.asList("org.xbmc.kodi", "com.spotify.tv.client"), reset.packages);
    }

    @Test
    public void gridMoveKeepsTheConfigDock() {
        AppOrder.Layout start = AppOrder.layout(
                Arrays.asList(
                        app("com.disney.disneyplus", "Disney+"),
                        app("com.aaa.alpha", "Alpha"),
                        app("com.plexapp.android", "Plex"),
                        app("com.spotify.tv.client", "Spotify"),
                        app("org.xbmc.kodi", "Kodi"),
                        app("com.abc.zzz", "Zed")),
                Collections.singletonList("com.disney.disneyplus"),
                Collections.emptySet(),
                AppOrder.State.fresh(),
                COLS);
        // Dock is Disney plus three fillers. The grid is the last two tiles.
        assertEquals(4, start.dockCount);
        int from = start.packages.indexOf("com.abc.zzz");
        int to = start.packages.indexOf("com.spotify.tv.client");
        assertTrue(from >= start.dockCount && to >= start.dockCount);
        AppOrder.State moved = AppOrder.move(AppOrder.State.fresh(), start, from, to);
        assertFalse(moved.userDock);
        AppOrder.Layout again = AppOrder.layout(
                Arrays.asList(
                        app("com.netflix.ninja", "Netflix"),
                        app("com.disney.disneyplus", "Disney+"),
                        app("com.aaa.alpha", "Alpha"),
                        app("com.plexapp.android", "Plex"),
                        app("com.spotify.tv.client", "Spotify"),
                        app("org.xbmc.kodi", "Kodi"),
                        app("com.abc.zzz", "Zed")),
                Arrays.asList("com.netflix.ninja", "com.disney.disneyplus"),
                Collections.emptySet(),
                moved,
                COLS);
        assertEquals("com.netflix.ninja", again.packages.get(0));
        assertEquals("com.disney.disneyplus", again.packages.get(1));
        assertTrue(again.packages.indexOf("com.abc.zzz") < again.packages.indexOf("com.spotify.tv.client"));
    }

    @Test
    public void movingIntoTheDockTakesOwnershipOfThatRow() {
        AppOrder.Layout start = AppOrder.layout(
                Arrays.asList(
                        app("com.disney.disneyplus", "Disney+"),
                        app("com.plexapp.android", "Plex"),
                        app("com.spotify.tv.client", "Spotify"),
                        app("org.xbmc.kodi", "Kodi"),
                        app("com.abc.zzz", "Zed")),
                Collections.singletonList("com.disney.disneyplus"),
                Collections.emptySet(),
                AppOrder.State.fresh(),
                COLS);
        int from = start.packages.indexOf("com.abc.zzz");
        AppOrder.State moved = AppOrder.move(AppOrder.State.fresh(), start, from, 0);
        assertTrue(moved.userDock);
        assertEquals("com.abc.zzz", moved.dock.get(0));
        AppOrder.Layout again = AppOrder.layout(
                Arrays.asList(
                        app("com.netflix.ninja", "Netflix"),
                        app("com.disney.disneyplus", "Disney+"),
                        app("com.plexapp.android", "Plex"),
                        app("com.spotify.tv.client", "Spotify"),
                        app("org.xbmc.kodi", "Kodi"),
                        app("com.abc.zzz", "Zed")),
                Collections.singletonList("com.netflix.ninja"),
                Collections.emptySet(),
                moved,
                COLS);
        assertEquals(moved.dock, again.packages.subList(0, again.dockCount));
        assertEquals(again.packages.size() - 1, again.packages.indexOf("com.netflix.ninja"));
    }

    @Test
    public void pinBumpsTheLastExplicitPinWhenTheRowIsFull() {
        AppOrder.Layout start = AppOrder.layout(
                Arrays.asList(
                        app("a.a", "A"),
                        app("b.b", "B"),
                        app("c.c", "C"),
                        app("d.d", "D"),
                        app("e.e", "E")),
                Arrays.asList("a.a", "b.b", "c.c", "d.d"),
                Collections.emptySet(),
                AppOrder.State.fresh(),
                COLS);
        AppOrder.State pinned = AppOrder.pin(AppOrder.State.fresh(), start, "e.e", COLS);
        assertTrue(pinned.userDock);
        assertEquals(Arrays.asList("a.a", "b.b", "c.c", "e.e"), pinned.dock);
        AppOrder.Layout again = AppOrder.layout(
                Arrays.asList(app("a.a", "A"), app("b.b", "B"), app("c.c", "C"), app("d.d", "D"), app("e.e", "E")),
                Collections.singletonList("com.netflix.ninja"),
                Collections.emptySet(),
                pinned,
                COLS);
        assertEquals(Arrays.asList("a.a", "b.b", "c.c", "e.e", "d.d"), again.packages);
        assertEquals(4, again.dockCount);
    }

    @Test
    public void unpinParksTheAppBelowTheFilledDock() {
        AppOrder.Layout start = AppOrder.layout(
                Arrays.asList(
                        app("com.amazon.amazonvideo.livingroom.nvidia", "Prime Video"),
                        app("com.disney.disneyplus", "Disney+"),
                        app("com.plexapp.android", "Plex"),
                        app("com.spotify.tv.client", "Spotify"),
                        app("org.xbmc.kodi", "Kodi")),
                Arrays.asList(
                        "com.netflix.ninja",
                        "com.amazon.amazonvideo.livingroom.nvidia",
                        "com.disney.disneyplus"),
                Collections.emptySet(),
                AppOrder.State.fresh(),
                COLS);
        AppOrder.State unpinned = AppOrder.unpin(
                AppOrder.State.fresh(), start, "com.amazon.amazonvideo.livingroom.nvidia", COLS);
        assertTrue(unpinned.userDock);
        assertFalse(unpinned.dock.contains("com.amazon.amazonvideo.livingroom.nvidia"));
        AppOrder.Layout again = AppOrder.layout(
                Arrays.asList(
                        app("com.netflix.ninja", "Netflix"),
                        app("com.amazon.amazonvideo.livingroom.nvidia", "Prime Video"),
                        app("com.disney.disneyplus", "Disney+"),
                        app("com.plexapp.android", "Plex"),
                        app("com.spotify.tv.client", "Spotify"),
                        app("org.xbmc.kodi", "Kodi")),
                Collections.singletonList("com.netflix.ninja"),
                Collections.emptySet(),
                unpinned,
                COLS);
        assertEquals(4, again.packages.indexOf("com.amazon.amazonvideo.livingroom.nvidia"));
        assertFalse(again.packages.subList(0, 4).contains("com.netflix.ninja"));
    }

    @Test
    public void encodeRoundTripAndGarbage() {
        AppOrder.State state = new AppOrder.State(
                Arrays.asList("b.b", "a.a"),
                Collections.singletonList("b.b"),
                new LinkedHashSet<>(Arrays.asList("z.z", "m.m")),
                true);
        AppOrder.State back = AppOrder.decode(AppOrder.encode(state));
        assertEquals(state.order, back.order);
        assertEquals(state.dock, back.dock);
        assertEquals(state.hidden, back.hidden);
        assertTrue(back.userDock);
        assertFalse(AppOrder.decode("nope").userDock);
        assertTrue(AppOrder.decode("").order.isEmpty());
        assertFalse(AppOrder.decode(null).userDock);
    }

    /** Small holder so the hidden-set literal stays obvious in the test above. */
    private static final class SetHidden {
        final LinkedHashSet<String> names;

        SetHidden(String... pkgs) {
            names = new LinkedHashSet<>(Arrays.asList(pkgs));
        }
    }
}
