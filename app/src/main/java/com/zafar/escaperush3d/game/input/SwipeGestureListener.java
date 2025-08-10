package com.zafar.escaperush3d.game.input;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Detects directional swipes for lane change, jump, slide.
 * Works with both fling (fast) and scroll (slow) gestures.
 */
public abstract class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
    // Defaults tuned to feel good across densities; you can tweak if needed
    private static final int SWIPE_THRESHOLD_PX = 80;         // movement threshold
    private static final int SWIPE_VELOCITY_THRESHOLD_PX = 80; // velocity threshold

    private boolean handledDrag = false;

    public void onSwipeLeft() {}
    public void onSwipeRight() {}
    public void onSwipeUp() {}
    public void onSwipeDown() {}

    @Override
    public boolean onDown(MotionEvent e) {
        // Must return true so we continue receiving subsequent events
        handledDrag = false;
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float dX, float dY) {
        if (e1 == null || e2 == null) return false;
        if (handledDrag) return true;

        float dx = e2.getX() - e1.getX();
        float dy = e2.getY() - e1.getY();

        if (Math.abs(dx) > Math.abs(dy)) {
            if (Math.abs(dx) > SWIPE_THRESHOLD_PX) {
                if (dx > 0) onSwipeRight(); else onSwipeLeft();
                handledDrag = true;
                return true;
            }
        } else {
            if (Math.abs(dy) > SWIPE_THRESHOLD_PX) {
                if (dy > 0) onSwipeDown(); else onSwipeUp();
                handledDrag = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
        if (e1 == null || e2 == null) return false;

        float dx = e2.getX() - e1.getX();
        float dy = e2.getY() - e1.getY();

        if (Math.abs(dx) > Math.abs(dy)) {
            if (Math.abs(dx) > SWIPE_THRESHOLD_PX && Math.abs(vX) > SWIPE_VELOCITY_THRESHOLD_PX) {
                if (dx > 0) onSwipeRight(); else onSwipeLeft();
                return true;
            }
        } else {
            if (Math.abs(dy) > SWIPE_THRESHOLD_PX && Math.abs(vY) > SWIPE_VELOCITY_THRESHOLD_PX) {
                if (dy > 0) onSwipeDown(); else onSwipeUp();
                return true;
            }
        }
        return false;
    }
}