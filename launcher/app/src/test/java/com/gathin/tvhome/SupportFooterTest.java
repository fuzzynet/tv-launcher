package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** The support pill scrolls up until its bottom is inside the overscan margin, and no further. */
public class SupportFooterTest {

    @Test
    public void alreadyInsideTheSafeAreaDoesNotScroll() {
        assertEquals(0, SupportFooter.scrollDy(400, 432, 500));
        assertEquals(0, SupportFooter.scrollDy(400, 500, 500));
    }

    @Test
    public void pillPastTheSafeLineScrollsUpByTheOverflow() {
        // 32px pill whose bottom is 20px into the overscan band.
        assertEquals(20, SupportFooter.scrollDy(400, 520, 500));
    }

    @Test
    public void scrollStopsWhenThePillReachesTheTop() {
        // Overflow is 80, but the pill is only 30px below the top of the grid.
        assertEquals(30, SupportFooter.scrollDy(30, 580, 500));
    }

    @Test
    public void pillAlreadyAtTheTopIsLeftThere() {
        assertEquals(0, SupportFooter.scrollDy(0, 580, 500));
        assertEquals(0, SupportFooter.scrollDy(-8, 580, 500));
    }
}
