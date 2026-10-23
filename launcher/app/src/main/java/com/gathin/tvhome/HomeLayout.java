package com.gathin.tvhome;

/**
 * Vertical placement of the dock and the rows under it.
 *
 * <p>The old placement pinned the dock to {@code gridHeight - dock - rowsBelowDock * row - 30dp}.
 * With {@code rows_below_dock} at 0 that left only a 30dp peek of the next row, on a 540dp stick
 * and on a 4K Shield alike, because the offset does not depend on the screen being short.
 * Density changes did not help: the extra dp became empty space above the dock.
 *
 * <p>The dock and at least the next row now stay fully on screen at every height. Further rows
 * still scroll in. The hero gap and then the tiles shrink only when the design sizes do not fit,
 * so a tall Shield or onn Pro keeps the design tile and the design gap and stays bottom-weighted.
 * The support code is a row after the last app, not a reserved band on the home screen.
 */
final class HomeLayout {

    /** Gap between the hero block and the dock card at the design size (the old 14dp + 26dp). */
    static final float HERO_GAP_DP = 40f;
    /** Shortest gap that still separates the hero from the dock. */
    static final float MIN_HERO_GAP_DP = 8f;
    /** Dock decoration top (6dp) plus the tile's own top padding (8dp): card top below the row top. */
    static final float CARD_OFFSET_DP = 14f;
    /**
     * The hero may extend this far above the grid, into the margin under the header, before it
     * would draw on the clock.
     */
    static final float HERO_SLACK_DP = 16f;
    /** How far tiles may shrink to keep one row under the dock when the 85% floor is not enough. */
    static final float SHORT_MIN_HEIGHT_FIT = 0.62f;

    private HomeLayout() {}

    static final class Plan {
        final int tileWidthPx;
        final int paddingTopPx;
        final int heroGapPx;
        final int bottomRoomPx;
        /** Rows under the dock whose full height sits inside the grid. 0 means the next row only peeks. */
        final int rowsFullyVisible;
        final int gridHeightPx;

        Plan(int tileWidthPx, int paddingTopPx, int heroGapPx, int bottomRoomPx, int rowsFullyVisible, int gridHeightPx) {
            this.tileWidthPx = tileWidthPx;
            this.paddingTopPx = paddingTopPx;
            this.heroGapPx = heroGapPx;
            this.bottomRoomPx = bottomRoomPx;
            this.rowsFullyVisible = rowsFullyVisible;
            this.gridHeightPx = gridHeightPx;
        }

        /** Bottom of the last fully visible row, or of the dock when the next row only peeks. */
        int visibleBottomPx(float density) {
            int item = AppAdapter.itemHeightPx(AppAdapter.tileHeightPx(tileWidthPx), true, density);
            int dock = item + Math.round(AppAdapter.DOCK_EXTRA_DP * density);
            return paddingTopPx + dock + item * Math.max(0, rowsFullyVisible);
        }
    }

    /**
     * @param heroBlockPx measured hero (name, and the cinematic lines). 0 when the dock title is off.
     * @param minPaddingPx optional floor for the top padding (the app-library clearance). Applied only
     *                     when the visible rows still fit, so it cannot clip them.
     */
    static Plan plan(int innerWidthPx, int gridHeightPx, int columns, int rowsBelowDock, boolean dockTitle,
                     boolean dockRow, float density, int heroBlockPx, int minPaddingPx) {
        int designGap = Math.round(HERO_GAP_DP * density);
        int designTile = AppAdapter.tileWidthPx(innerWidthPx, columns, density);
        if (!dockRow) {
            int w = AppAdapter.tileWidthPx(innerWidthPx, gridHeightPx, columns, rowsBelowDock, dockTitle, false, density);
            return new Plan(w, Math.round(AppAdapter.TOP_ROOM_DP * density), designGap, 0,
                    Math.max(0, rowsBelowDock), gridHeightPx);
        }
        if (gridHeightPx <= 0) {
            return new Plan(designTile, Math.round(AppAdapter.TOP_ROOM_DP * density), designGap,
                    Math.round(AppAdapter.BOTTOM_ROOM_DP * density), 0, gridHeightPx);
        }

        int minGap = Math.round(MIN_HERO_GAP_DP * density);
        int minBottom = Math.round(HomeLayout.MIN_BOTTOM_ROOM_DP * density);
        int designBottom = Math.round(AppAdapter.BOTTOM_ROOM_DP * density);
        int floor = Math.max(Math.round(80 * density), Math.round(designTile * AppAdapter.MIN_HEIGHT_FIT));
        int shortFloor = Math.max(Math.round(80 * density), Math.round(designTile * SHORT_MIN_HEIGHT_FIT));
        floor = Math.min(floor, designTile);
        shortFloor = Math.min(shortFloor, floor);
        int hero = dockTitle ? Math.max(0, heroBlockPx) : 0;
        int rowsWanted = Math.max(1, rowsBelowDock);

        for (int rows = rowsWanted; rows >= 1; rows--) {
            final int rowCount = rows;
            int tile = largest(floor, designTile,
                    t -> fits(gridHeightPx, t, rowCount, hero, minGap, minBottom, dockTitle, density));
            // One row under the dock is required. Extra rows stop at the 85% floor and scroll instead.
            if (tile < 0 && rowCount == 1) {
                tile = largest(shortFloor, Math.max(shortFloor, floor - 1),
                        t -> fits(gridHeightPx, t, rowCount, hero, minGap, minBottom, dockTitle, density));
            }
            if (tile < 0) continue;
            final int chosen = tile;
            int gap = largest(minGap, designGap,
                    g -> fits(gridHeightPx, chosen, rowCount, hero, g, minBottom, dockTitle, density));
            if (gap < 0) gap = minGap;
            final int chosenGap = gap;
            int bottom = largest(minBottom, designBottom,
                    b -> fits(gridHeightPx, chosen, rowCount, hero, chosenGap, b, dockTitle, density));
            if (bottom < 0) bottom = minBottom;
            return place(gridHeightPx, chosen, rowCount, hero, chosenGap, bottom, dockTitle, density, minPaddingPx);
        }
        return peek(gridHeightPx, designTile, shortFloor, hero, minGap, dockTitle, density);
    }

    /** Room under the last fully visible row once the hero has been tightened. */
    private static final float MIN_BOTTOM_ROOM_DP = 12f;

    private static Plan place(int gridHeightPx, int tile, int rows, int hero, int gap, int bottom,
                              boolean dockTitle, float density, int minPaddingPx) {
        int item = item(tile, density);
        int dock = item + Math.round(AppAdapter.DOCK_EXTRA_DP * density);
        int heroPad = heroPadding(hero, gap, dockTitle, density);
        int want = gridHeightPx - dock - rows * item - bottom;
        if (want < heroPad) want = heroPad;
        if (minPaddingPx > want && minPaddingPx + dock + rows * item + bottom <= gridHeightPx) want = minPaddingPx;
        if (want < 0) want = 0;
        int room = gridHeightPx - (want + dock + rows * item);
        return new Plan(tile, want, gap, Math.max(0, room), rows, gridHeightPx);
    }

    /** Dock stays clear of the header; the next row peeks and scrolls in when even the short tile cannot. */
    private static Plan peek(int gridHeightPx, int designTile, int shortFloor, int hero, int minGap,
                             boolean dockTitle, float density) {
        int tile = largest(shortFloor, designTile,
                t -> fits(gridHeightPx, t, 0, hero, minGap, 0, dockTitle, density));
        if (tile < 0) tile = shortFloor;
        int item = item(tile, density);
        int dock = item + Math.round(AppAdapter.DOCK_EXTRA_DP * density);
        int heroPad = heroPadding(hero, minGap, dockTitle, density);
        int room = gridHeightPx - heroPad - dock;
        int peekPx = room > 0 ? Math.min(item / 3, room) : 0;
        int want = room > 0 ? gridHeightPx - dock - peekPx : heroPad;
        if (want < heroPad && room > 0) want = heroPad;
        if (want < 0) want = 0;
        return new Plan(tile, want, minGap, Math.max(0, gridHeightPx - want - dock), 0, gridHeightPx);
    }

    private static boolean fits(int gridHeightPx, int tile, int rows, int hero, int gap, int bottom,
                                boolean dockTitle, float density) {
        int item = item(tile, density);
        int dock = item + Math.round(AppAdapter.DOCK_EXTRA_DP * density);
        int heroPad = heroPadding(hero, gap, dockTitle, density);
        return heroPad + dock + rows * item + bottom <= gridHeightPx;
    }

    /** Padding that keeps the hero under the header. The card sits {@link #CARD_OFFSET_DP} below the row. */
    private static int heroPadding(int heroBlockPx, int gapPx, boolean dockTitle, float density) {
        if (!dockTitle || heroBlockPx <= 0) return 0;
        int slack = Math.round((CARD_OFFSET_DP + HERO_SLACK_DP) * density);
        return Math.max(0, heroBlockPx + gapPx - slack);
    }

    private static int item(int tileWidthPx, float density) {
        return AppAdapter.itemHeightPx(AppAdapter.tileHeightPx(tileWidthPx), true, density);
    }

    /** Largest value in {@code [min, max]} for which {@code ok} is true, or -1. Larger values are harder. */
    private static int largest(int min, int max, Fit ok) {
        if (max < min) return -1;
        int lo = min, hi = max, best = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (ok.ok(mid)) {
                best = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return best;
    }

    private interface Fit {
        boolean ok(int value);
    }
}
