package com.gathin.tvhome;

/**
 * Keeps the support pill inside the TV's overscan-safe area.
 *
 * <p>The footer row can sit with its bottom on the screen edge. On a 1080p UI that crops about
 * 5 percent, and on a 4K framebuffer where the pill is twice as tall, that puts the chip halfway
 * off the panel. {@link #scrollDy} is how far the grid must scroll so the pill's bottom is at or
 * above the safe line. The row also pads by {@code overscan_safe_bottom}, so a scroll that only
 * brings the row onto the screen still leaves the chip inside that margin.
 */
final class SupportFooter {

    private SupportFooter() {}

    /**
     * @param pillTop    top of the pill in the grid, px. Negative means it starts above the grid.
     * @param pillBottom bottom of the pill in the grid, px.
     * @param safeBottom lowest y where the pill may end, px. Below this is the overscan margin.
     * @return positive pixels to scroll the content up, or 0 when the pill is already safe or
     *         already touches the top of the grid.
     */
    static int scrollDy(int pillTop, int pillBottom, int safeBottom) {
        if (pillBottom <= safeBottom) return 0;
        if (pillTop <= 0) return 0;
        return Math.min(pillBottom - safeBottom, pillTop);
    }
}
