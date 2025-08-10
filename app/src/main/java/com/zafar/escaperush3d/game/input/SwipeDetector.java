package com.zafar.escaperush3d.game.input;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;

/**
 * High-quality swipe detector tuned for runner games.
 * - Uses dp thresholds (device independent)
 * - Gated by angle to avoid diagonal misfires
 * - Accepts both quick flicks and slower, deliberate swipes
 */
public class SwipeDetector {

    public interface Callback {
        void onSwipeLeft();
        void onSwipeRight();
        void onSwipeUp();
        void onSwipeDown();
    }

    private final Callback callback;

    // Thresholds (in px after conversion)
    private final float slopPx;               // minimal movement before considering direction
    private final float minDistancePx;        // distance to trigger a swipe on move
    private final float minFlingVelocityPx;   // velocity to trigger on finger up (fling)
    private final float axisBias;             // how much one axis must dominate the other

    // Gesture state
    private float startX, startY;
    private int activePointerId = -1;
    private boolean actionSent = false;
    private int directionLock = 0; // 0 = unknown, 1 = horizontal, 2 = vertical
    private VelocityTracker velocityTracker;

    public SwipeDetector(Context context, Callback callback) {
        this.callback = callback;

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float dp = dm.density;

        // Tuned like popular runners. You can fine-tune if you like:
        this.slopPx = 8f * dp;
        this.minDistancePx = 28f * dp;        // quick to trigger, still robust
        this.minFlingVelocityPx = 900f * dp;  // flick speed threshold
        this.axisBias = 1.25f;                // 25% dominance for axis selection
    }

    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                activePointerId = ev.getPointerId(0);
                startX = ev.getX(0);
                startY = ev.getY(0);
                directionLock = 0;
                actionSent = false;

                if (velocityTracker != null) {
                    velocityTracker.clear();
                } else {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.addMovement(ev);
                return true;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                // Ignore additional fingers; stick to the first (common for swipe games)
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                if (activePointerId == -1) return true;
                velocityTracker.addMovement(ev);

                int idx = ev.findPointerIndex(activePointerId);
                if (idx == -1) return true;

                float x = ev.getX(idx);
                float y = ev.getY(idx);
                float dx = x - startX;
                float dy = y - startY;
                float adx = Math.abs(dx);
                float ady = Math.abs(dy);

                // Decide dominant axis once movement exceeds slop
                if (directionLock == 0 && (adx > slopPx || ady > slopPx)) {
                    if (adx > ady * axisBias) {
                        directionLock = 1; // horizontal
                    } else if (ady > adx * axisBias) {
                        directionLock = 2; // vertical
                    } else {
                        // still ambiguous, wait for more movement
                        return true;
                    }
                }

                if (!actionSent && directionLock != 0) {
                    if (directionLock == 1 && adx > minDistancePx) {
                        if (dx > 0) callback.onSwipeRight(); else callback.onSwipeLeft();
                        actionSent = true;
                    } else if (directionLock == 2 && ady > minDistancePx) {
                        if (dy > 0) callback.onSwipeDown(); else callback.onSwipeUp();
                        actionSent = true;
                    }
                }
                return true;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (!actionSent && activePointerId != -1 && velocityTracker != null) {
                    velocityTracker.addMovement(ev);
                    velocityTracker.computeCurrentVelocity(1000); // px/s
                    float vx = velocityTracker.getXVelocity(activePointerId);
                    float vy = velocityTracker.getYVelocity(activePointerId);

                    float adx = Math.abs(ev.getX() - startX);
                    float ady = Math.abs(ev.getY() - startY);

                    boolean horizontalFling = Math.abs(vx) > Math.abs(vy) * axisBias && Math.abs(vx) >= minFlingVelocityPx;
                    boolean verticalFling   = Math.abs(vy) > Math.abs(vx) * axisBias && Math.abs(vy) >= minFlingVelocityPx;

                    if (horizontalFling || adx > minDistancePx) {
                        if (vx > 0) callback.onSwipeRight(); else callback.onSwipeLeft();
                        actionSent = true;
                    } else if (verticalFling || ady > minDistancePx) {
                        if (vy > 0) callback.onSwipeDown(); else callback.onSwipeUp();
                        actionSent = true;
                    }
                }

                // Reset
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                activePointerId = -1;
                directionLock = 0;
                actionSent = false;
                return true;
            }
        }
        return true;
    }
}