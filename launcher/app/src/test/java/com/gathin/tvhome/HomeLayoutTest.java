package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Before: padding = grid - dock - rowsBelow*row - 30dp. rows_below_dock 0 leaves a 30dp sliver of
 * the next row on every height, which is the cutoff on the 540dp stick, a 720dp panel and a 4K
 * Shield. After: the dock and that row sit fully inside the grid. Design tiles and the 40dp hero
 * gap stay when the height allows.
 */
public class HomeLayoutTest {

    private static final int HERO_56 = 56;

    /** pad top 26 + greeting/clock ~80 + grid margin 10. The support footer is not part of this. */
    private static int gridPx(int screenDp, float density) {
        return Math.round(screenDp * density) - Math.round(116 * density);
    }

    private static int designTile(int innerPx, int columns, float density) {
        return AppAdapter.tileWidthPx(innerPx, columns, density);
    }

    private static void assertRowFullyVisible(HomeLayout.Plan plan, float density) {
        assertTrue("rows " + plan.rowsFullyVisible, plan.rowsFullyVisible >= 1);
        int bottom = plan.visibleBottomPx(density);
        assertTrue(bottom + " > " + plan.gridHeightPx, bottom <= plan.gridHeightPx);
        int heroTop = plan.paddingTopPx + Math.round(HomeLayout.CARD_OFFSET_DP * density)
                - plan.heroGapPx - Math.round(HERO_56 * density);
        assertTrue("hero top " + heroTop, heroTop >= -Math.round(HomeLayout.HERO_SLACK_DP * density) - 1);
    }

    /** The old pin: only BOTTOM_ROOM of the next row is inside the grid. */
    private static int oldPeekPx(int gridHeightPx, int tileWidthPx, float density) {
        int item = AppAdapter.itemHeightPx(AppAdapter.tileHeightPx(tileWidthPx), density);
        int dock = item + Math.round(AppAdapter.DOCK_EXTRA_DP * density);
        int pad = gridHeightPx - dock - Math.round(AppAdapter.BOTTOM_ROOM_DP * density);
        return gridHeightPx - (pad + dock);
    }

    @Test
    public void fixedBottomPinClipsTheSecondRowAtEveryHeight() {
        float[] density = {2f, 1.5f, 2f};
        int[] screen = {540, 720, 1080};
        for (int i = 0; i < screen.length; i++) {
            int grid = gridPx(screen[i], density[i]);
            int tile = designTile(1600, 4, density[i]);
            int peek = oldPeekPx(grid, tile, density[i]);
            int item = AppAdapter.itemHeightPx(AppAdapter.tileHeightPx(tile), density[i]);
            assertTrue(screen[i] + "dp peek " + peek + " of " + item, peek < item / 2);
        }
    }

    @Test
    public void stickShowsBothRows() {
        // 1920x1080 px at 320dpi.
        float d = 2f;
        int grid = gridPx(540, d);
        HomeLayout.Plan plan = HomeLayout.plan(1696, grid, 4, 0, true, true, d, Math.round(HERO_56 * d), 0);
        assertRowFullyVisible(plan, d);
        assertTrue(plan.tileWidthPx >= Math.round(designTile(1696, 4, d) * AppAdapter.MIN_HEIGHT_FIT));
    }

    @Test
    public void tenEightyPAt240DpiKeepsDesignMetricsAndBothRows() {
        // 1920x1080 px at 240dpi is 1280x720 dp.
        float d = 1.5f;
        int grid = gridPx(720, d);
        int design = designTile(1900, 4, d);
        HomeLayout.Plan plan = HomeLayout.plan(1900, grid, 4, 0, true, true, d, Math.round(HERO_56 * d), 0);
        assertRowFullyVisible(plan, d);
        assertEquals(design, plan.tileWidthPx);
        assertEquals(Math.round(HomeLayout.HERO_GAP_DP * d), plan.heroGapPx);
        assertTrue(plan.paddingTopPx > Math.round(80 * d));
    }

    @Test
    public void fourKShieldAndOnnProKeepDesignMetricsAndBothRows() {
        // 3840x2160 px at 320dpi is 1920x1080 dp, the w1800dp bucket. Tiles stay the Java design size.
        float d = 2f;
        int grid = gridPx(1080, d);
        int design = designTile(3200, 4, d);
        HomeLayout.Plan plan = HomeLayout.plan(3200, grid, 4, 0, true, true, d, Math.round(HERO_56 * d), 0);
        assertRowFullyVisible(plan, d);
        assertEquals(design, plan.tileWidthPx);
        assertEquals(Math.round(HomeLayout.HERO_GAP_DP * d), plan.heroGapPx);
        assertTrue("dock should stay low, pad " + plan.paddingTopPx, plan.paddingTopPx > Math.round(200 * d));
    }

    @Test
    public void shortGridShrinksTheHeroGapBeforeItClipsTheRow() {
        float d = 2f;
        int grid = 700;
        int design = designTile(1696, 4, d);
        HomeLayout.Plan plan = HomeLayout.plan(1696, grid, 4, 0, true, true, d, Math.round(HERO_56 * d), 0);
        assertRowFullyVisible(plan, d);
        assertTrue(plan.heroGapPx < Math.round(HomeLayout.HERO_GAP_DP * d)
                || plan.tileWidthPx < design);
        assertTrue(plan.tileWidthPx >= Math.round(design * AppAdapter.MIN_HEIGHT_FIT));
    }

    @Test
    public void veryShortGridMayShrinkPastTheNormalFloorButStillShowsTheRow() {
        float d = 2f;
        int grid = 600;
        int design = designTile(1696, 4, d);
        HomeLayout.Plan plan = HomeLayout.plan(1696, grid, 4, 0, true, true, d, Math.round(HERO_56 * d), 0);
        assertRowFullyVisible(plan, d);
        assertTrue(plan.tileWidthPx >= Math.round(design * HomeLayout.SHORT_MIN_HEIGHT_FIT));
    }

    @Test
    public void extraRowsStayWhenTheyFitAndDoNotForceTinyTiles() {
        float d = 2f;
        int grid = gridPx(1080, d);
        HomeLayout.Plan plan = HomeLayout.plan(3200, grid, 4, 2, true, true, d, Math.round(HERO_56 * d), 0);
        assertTrue(plan.rowsFullyVisible >= 2);
        assertEquals(designTile(3200, 4, d), plan.tileWidthPx);

        // The old 846px grid cannot hold a titled dock plus three more rows at a readable size.
        HomeLayout.Plan tight = HomeLayout.plan(1696, 846, 4, 3, true, true, d, Math.round(HERO_56 * d), 0);
        assertTrue(tight.rowsFullyVisible >= 1);
        assertTrue(tight.rowsFullyVisible < 3);
        assertTrue(tight.tileWidthPx >= Math.round(designTile(1696, 4, d) * AppAdapter.MIN_HEIGHT_FIT));
        assertTrue(tight.visibleBottomPx(d) <= tight.gridHeightPx);
    }

    @Test
    public void plainGridMatchesTheWidthOnlyFit() {
        float d = 2f;
        int old = AppAdapter.tileWidthPx(1696, 846, 6, 3, true, false, d);
        HomeLayout.Plan plan = HomeLayout.plan(1696, 846, 6, 3, true, false, d, 200, 0);
        assertEquals(old, plan.tileWidthPx);
        assertEquals(Math.round(AppAdapter.TOP_ROOM_DP * d), plan.paddingTopPx);
    }

    @Test
    public void noHeightYetKeepsTheDesignTile() {
        HomeLayout.Plan plan = HomeLayout.plan(1696, 0, 4, 1, true, true, 2f, 112, 0);
        assertEquals(AppAdapter.tileWidthPx(1696, 4, 2f), plan.tileWidthPx);
    }
}
