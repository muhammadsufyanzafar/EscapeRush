package com.zafar.escaperush3d.game.systems;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Lightweight parallax-like background: stripes sliding vertically.
 */
public class ParallaxBackground {
    private int w, h;
    private int baseColor = Color.rgb(30, 30, 35);
    private float y2 = 0;

    private final Paint pStripe = new Paint();

    public ParallaxBackground(int w, int h) {
        this.w = w; this.h = h;
        pStripe.setColor(Color.argb(30, 255, 255, 255));
    }

    public void setBaseColor(int color) {
        baseColor = color;
    }

    public void update(float dt, float speedPx) {
        float s = speedPx * 0.1f;
        y2 += dt * s * 1.3f;
        if (y2 > h) y2 -= h;
    }

    public void draw(Canvas c) {
        c.drawColor(baseColor);
        // simple stripes moving down
        for (int i = -2; i < 4; i++) {
            float y = i * h / 3f + (y2 % (h / 3f));
            c.drawRect(0, y, w, y + 4, pStripe);
        }
    }
}