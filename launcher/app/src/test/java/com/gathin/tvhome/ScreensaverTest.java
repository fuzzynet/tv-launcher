package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScreensaverTest {

    @Test
    public void startsWithTheVoiceActionThenSomnambulator() {
        assertArrayEquals(new String[]{Screensaver.VOICE_ACTION, Screensaver.SOMNAMBULATOR}, Screensaver.starters());
    }

    @Test
    public void neverStartsTheDaydreamSettingsTrampoline() {
        for (String s : Screensaver.starters()) {
            assertFalse(s.endsWith("DaydreamActivity"));
        }
    }

    @Test
    public void configuredDreamStarts() {
        assertTrue(Screensaver.dreamConfigured("1", "com.google.android.apps.tv.dreamx/.service.Backdrop"));
        assertTrue(Screensaver.dreamConfigured(null, null));
        assertTrue(Screensaver.dreamConfigured("1", null));
    }

    @Test
    public void disabledOrEmptyFallsBackToSettings() {
        assertFalse(Screensaver.dreamConfigured("0", "com.google.android.backdrop/.Backdrop"));
        assertFalse(Screensaver.dreamConfigured("1", ""));
        assertFalse(Screensaver.dreamConfigured("1", "null"));
    }
}
