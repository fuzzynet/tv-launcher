package com.gathin.tvhome;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MoveSessionTest {
    @Test public void tileMenuMoveEndsOnFirstDrop() {
        MoveSession s = new MoveSession();
        s.begin(true, false);
        assertTrue(s.endsOnDrop());
    }

    @Test public void reorderButtonStaysOnAfterDrop() {
        MoveSession s = new MoveSession();
        s.begin(false, false);
        assertFalse(s.endsOnDrop());
    }

    @Test public void tileMenuMoveInsideReorderModeKeepsTheMode() {
        MoveSession s = new MoveSession();
        s.begin(false, false);
        s.begin(true, true);
        assertFalse(s.endsOnDrop());
    }

    @Test public void endClearsSingleMove() {
        MoveSession s = new MoveSession();
        s.begin(true, false);
        s.end();
        s.begin(false, false);
        assertFalse(s.endsOnDrop());
    }
}
