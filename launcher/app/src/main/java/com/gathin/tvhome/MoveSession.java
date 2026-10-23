package com.gathin.tvhome;

/**
 * What putting a carried tile down does. A session started from the tile menu's Move is a single move: dropping
 * the tile (OK or Back) ends reorder mode too, so one press returns the remote to normal. A session started from
 * the Reorder button stays on after a drop, for the next tile, and Back with nothing carried ends it.
 */
final class MoveSession {
    private boolean single;

    /** Reorder mode was entered just to move one tile (tile menu). Ignored if the mode was already on. */
    void begin(boolean singleMove, boolean alreadyReordering) {
        if (!alreadyReordering) single = singleMove;
    }

    /** The carried tile was put down; true when reorder mode should end as well. */
    boolean endsOnDrop() {
        return single;
    }

    void end() {
        single = false;
    }
}
