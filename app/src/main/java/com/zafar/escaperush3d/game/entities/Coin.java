package com.zafar.escaperush3d.game.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.zafar.escaperush3d.util.Constants;

/**
 * Coin pickup with simple rotation and magnet behavior.
 */
public class Coin extends Entity {
    private float rot = 0f;
    private final Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Coin(float laneX, float startY) {
        this.x = laneX;
        this.y = startY;
        this.w = 40f; this.h = 40f;
        paint.setColor(Color.YELLOW);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(3f);
        border.setColor(Color.rgb(200, 160, 0));
    }

    @Override
    public void update(float dt, float speedPx) {
        y += dt * speedPx;
        rot += dt * 4f;
    }

    public void update(float dt, float speedPx, Player player, boolean magnet) {
        update(dt, speedPx);
        if (magnet) {
            float dx = player.getBounds().centerX() - x;
            float dy = player.getBounds().centerY() - y;
            float d2 = dx*dx + dy*dy;
            if (d2 < Constants.MAGNET_RADIUS * Constants.MAGNET_RADIUS) {
                x += dx * 0.12f;
                y += dy * 0.12f;
            }
        }
    }

    @Override
    public void draw(Canvas c) {
        c.save();
        c.rotate((float) Math.toDegrees(rot), x, y);
        c.drawCircle(x, y, w/2f, paint);
        c.drawCircle(x, y, w/2f, border);
        c.restore();
    }
}