package com.zafar.escaperush3d.game.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

/**
 * Simple block obstacle.
 */
public class Obstacle extends Entity {
    public Obstacle(float laneX, float startY) {
        this.x = laneX;
        this.y = startY;
        this.w = 140f;
        this.h = 140f;
        paint.setColor(Color.rgb(200, 60, 60));
    }

    @Override
    public void update(float dt, float speedPx) {
        y += dt * speedPx;
    }

    @Override
    public void draw(Canvas c) {
        c.drawRoundRect(new RectF(x - w/2f, y - h/2f, x + w/2f, y + h/2f), 16f, 16f, paint);
    }
}