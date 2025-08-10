package com.zafar.escaperush3d.game.systems;

import android.graphics.RectF;

/**
 * Simple AABB collision checks.
 */
public class CollisionDetector {
    public boolean overlap(RectF a, RectF b) {
        return RectF.intersects(a, b);
    }
}