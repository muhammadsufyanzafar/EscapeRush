package com.zafar.escaperush3d.game.entities;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Base entity with bounds and drawing.
 */
public abstract class Entity {
    protected float x, y, w, h;
    protected final RectF bounds = new RectF();
    protected final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public abstract void update(float dt, float speedPx);
    public abstract void draw(Canvas c);

    public RectF getBounds() {
        bounds.set(x - w/2f, y - h/2f, x + w/2f, y + h/2f);
        return bounds;
    }

    public boolean isOffscreen(int screenH) {
        return y - h/2f > screenH + 50;
    }
}