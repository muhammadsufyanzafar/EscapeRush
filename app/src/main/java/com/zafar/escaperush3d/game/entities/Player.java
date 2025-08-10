package com.zafar.escaperush3d.game.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.zafar.escaperush3d.util.Constants;

/**
 * Player character: lane movement, jump/slide, power-ups.
 */
public class Player extends Entity {
    private final float[] lanesX;
    private int currentLane = 1;
    private float targetX;
    private float moveLerp = 12f;

    private float vy = 0f;
    private float groundY;
    private boolean jumping = false;
    private boolean sliding = false;
    private float slideTimer = 0f;
    private boolean dead = false;

    private boolean shield = false;
    private float shieldTimer = 0f;
    private boolean magnet = false;
    private float magnetTimer = 0f;
    private int coinMultiplier = 1;
    private float coinTimer = 0f;

    private final Paint shieldPaint = new Paint();

    public Player(float[] lanesX, int screenH) {
        this.lanesX = lanesX;
        this.w = 120f; this.h = 180f;
        this.x = lanesX[currentLane];
        this.groundY = screenH - 260f;
        this.y = groundY;
        this.targetX = x;

        paint.setColor(Color.rgb(255, 200, 40));
        shieldPaint.setStyle(Paint.Style.STROKE);
        shieldPaint.setColor(Color.CYAN);
        shieldPaint.setStrokeWidth(6f);
    }

    public void update(float dt, int screenH) {
        if (dead) return;

        // Lateral smooth move to target lane
        x += (targetX - x) * Math.min(1f, moveLerp * dt);

        // Jump physics
        if (jumping) {
            vy += Constants.GRAVITY * dt;
            y += vy * dt;
            if (y >= groundY) {
                y = groundY;
                jumping = false;
                vy = 0;
            }
        }

        // Sliding
        if (sliding) {
            slideTimer -= dt;
            if (slideTimer <= 0) {
                sliding = false;
                h = 180f;
            }
        }

        // Timers
        if (shield) { shieldTimer -= dt; if (shieldTimer <= 0) shield = false; }
        if (magnet) { magnetTimer -= dt; if (magnetTimer <= 0) magnet = false; }
        if (coinMultiplier > 1) { coinTimer -= dt; if (coinTimer <= 0) coinMultiplier = 1; }
    }

    @Override public void update(float dt, float speedPx) { /* unused by Player; overloaded above */ }

    @Override public void draw(Canvas c) {
        // Player body (rounded rect)
        float left = x - w/2f, top = y - h/2f;
        c.drawRoundRect(new RectF(left, top, left + w, top + h), 24f, 24f, paint);
        if (shield) {
            c.drawCircle(x, y, Math.max(w, h), shieldPaint);
        }
    }

    public void moveLane(int dir) {
        int next = Math.max(0, Math.min(lanesX.length - 1, currentLane + dir));
        currentLane = next;
        targetX = lanesX[currentLane];
    }

    public void jump() {
        if (dead) return;
        if (!jumping && !sliding) {
            jumping = true;
            vy = Constants.PLAYER_JUMP_VELOCITY;
        }
    }

    public void slide() {
        if (dead) return;
        if (!jumping && !sliding) {
            sliding = true;
            slideTimer = Constants.SLIDE_DURATION;
            h = 120f;
        }
    }

    public boolean isShieldActive() { return shield; }
    public boolean isMagnetActive() { return magnet; }
    public int getCoinMultiplier() { return coinMultiplier; }

    public void activateShield(float duration) { shield = true; shieldTimer = duration; }
    public void activateMagnet(float duration) { magnet = true; magnetTimer = duration; }
    public void activateDoubleCoins(float duration) { coinMultiplier = 2; coinTimer = duration; }

    public void reset() {
        dead = false;
        currentLane = 1;
        x = lanesX[currentLane];
        targetX = x;
        y = groundY; vy = 0; jumping = false; sliding = false; h = 180f;
        shield = false; magnet = false; coinMultiplier = 1;
    }

    public void revive() { dead = false; y = groundY; vy = 0; jumping = false; sliding = false; }

    public void kill() { dead = true; }
    public boolean isDead() { return dead; }
}