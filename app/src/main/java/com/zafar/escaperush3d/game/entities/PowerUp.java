package com.zafar.escaperush3d.game.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

/**
 * Power-up pickup block with color-coded type.
 */
public class PowerUp extends Entity {
    private final PowerUpType type;

    public PowerUp(float laneX, float startY, PowerUpType type) {
        this.x = laneX;
        this.y = startY;
        this.w = 60f; this.h = 60f;
        this.type = type;
        switch (type) {
            case MAGNET: paint.setColor(Color.MAGENTA); break;
            case SHIELD: paint.setColor(Color.CYAN); break;
            case DOUBLE_COINS: paint.setColor(Color.GREEN); break;
            case SPEED_BOOST: paint.setColor(Color.BLUE); break;
        }
    }

    @Override
    public void update(float dt, float speedPx) {
        y += dt * speedPx;
    }

    @Override
    public void draw(Canvas c) {
        c.drawRoundRect(new RectF(x - w/2f, y - h/2f, x + w/2f, y + h/2f), 14f, 14f, paint);
    }

    public PowerUpType getType() { return type; }
}