package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Tile width stays at the design size through 4 columns and shrinks above that. */
public class TileSizeTest {

    @Test
    public void fourColumnsStayAtTheDesignWidth() {
        assertEquals(360, AppAdapter.tileWidthPx(1800, 4, 2f));
        assertEquals(360, AppAdapter.tileWidthPx(900, 4, 2f));
        assertEquals(360, AppAdapter.tileWidthPx(0, 6, 2f));
    }

    @Test
    public void sixColumnsShrinkButStayReadable() {
        int width = AppAdapter.tileWidthPx(1600, 6, 2f);
        assertTrue(width < 360);
        assertTrue(width >= 160);
        assertTrue(AppAdapter.tileHeightPx(width) < AppAdapter.tileHeightPx(360));
    }
}
