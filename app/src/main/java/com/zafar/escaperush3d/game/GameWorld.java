// app/src/main/java/com/zafar/escaperush3d/game/GameWorld.java
package com.zafar.escaperush3d.game;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowMetrics;

import com.zafar.escaperush3d.R;
import com.zafar.escaperush3d.game.entities.Coin;
import com.zafar.escaperush3d.game.entities.Entity;
import com.zafar.escaperush3d.game.entities.Obstacle;
import com.zafar.escaperush3d.game.entities.Player;
import com.zafar.escaperush3d.game.entities.PowerUp;
import com.zafar.escaperush3d.game.entities.PowerUpType;
import com.zafar.escaperush3d.game.systems.CollisionDetector;
import com.zafar.escaperush3d.game.systems.LevelManager;
import com.zafar.escaperush3d.game.systems.ParallaxBackground;
import com.zafar.escaperush3d.game.systems.Spawner;
import com.zafar.escaperush3d.util.Constants;
import com.zafar.escaperush3d.util.Prefs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Core game world: update, draw, spawn, collisions, scoring.
 */
public class GameWorld {
    public interface GameOverListener { void onGameOver(int score, int coins, boolean newAreaUnlocked); }

    private static final String TAG = "GameWorld";

    private final Context context;
    private final GameOverListener listener;

    // Screen and pacing
    private int screenW, screenH;
    private float speedPx;           // current speed in px/s
    private float gameTime = 0f;

    // Entities
    private final Player player;
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<Coin> coins = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();

    // Systems
    private final float[] lanesX;
    private final Spawner spawner;
    private final CollisionDetector collisionDetector = new CollisionDetector();
    private final ParallaxBackground bg;

    // HUD
    private final Paint hudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hudBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint.FontMetrics hudFm;
    private float hudLineHeight;
    private float hudPaddingPx;
    private float hudCornerPx;
    private float hudLeftPx;
    private float hudTopPx;
    private final Paint lanePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Score/state
    private int score = 0;
    private int coinCount = 0;
    private boolean gameOver = false;
    private boolean newAreaUnlocked = false;

    // Tilt handling (smooth, hysteresis, cooldown)
    private float tiltSmoothed = 0f;
    private long lastTiltMoveMs = 0L;
    private int tiltLatch = 0; // 0 neutral, +1 latched to right, -1 latched to left

    // Tunables for tilt feel
    private static final float TILT_DEAD_ZONE = 0.12f;   // ignore tiny motions
    private static final float TILT_TRIGGER  = 0.38f;    // cross to trigger a move
    private static final float TILT_RELEASE  = 0.22f;    // must come back inside this to rearm
    private static final long  TILT_COOLDOWN_MS = 260L;  // avoid rapid multi-lane skips

    // Runtime debug flag (no BuildConfig needed)
    private final boolean isDebugBuild;

    public GameWorld(Context ctx, GameOverListener l) {
        context = ctx;
        listener = l;

        isDebugBuild = (ctx.getApplicationInfo().flags
                & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;

        // Determine screen size
        computeScreenSize(ctx);

        // Lanes centered across width
        lanesX = new float[Constants.LANE_COUNT];
        float laneWidth = screenW / (float) Constants.LANE_COUNT;
        for (int i = 0; i < Constants.LANE_COUNT; i++) {
            lanesX[i] = laneWidth * i + laneWidth / 2f;
        }

        // Scale base speed by display height for similar feel across devices
        speedPx = Constants.BASE_SPEED * (screenH / 1920f);
        player = new Player(lanesX, screenH);

        spawner = new Spawner(screenW, screenH, lanesX);
        bg = new ParallaxBackground(screenW, screenH);

        // HUD sizing/positioning
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float density = dm.density;
        float scaledDensity = dm.scaledDensity;
        float dpWidth = screenW / density;

        float spSize;
        if (dpWidth >= 600)      spSize = 22f;
        else if (dpWidth >= 480) spSize = 20f;
        else if (dpWidth >= 411) spSize = 18f;
        else if (dpWidth >= 392) spSize = 17f;
        else                     spSize = 16f;

        hudPaint.setColor(Color.WHITE);
        hudPaint.setTextSize(spSize * scaledDensity); // Canvas uses px; convert sp->px
        hudPaint.setShadowLayer(3f * density, 0f, 2f * density, Color.argb(180, 0, 0, 0));
        hudFm = hudPaint.getFontMetrics();
        hudLineHeight = hudFm.bottom - hudFm.top;

        hudBgPaint.setColor(Color.argb(80, 0, 0, 0)); // translucent dark

        hudPaddingPx = 8f * density;
        hudCornerPx = 10f * density;
        hudLeftPx = 12f * density;
        hudTopPx = getStatusBarHeightPx(context) + 12f * density; // respect status bar

        lanePaint.setColor(Color.argb(50, 255, 255, 255));

        applyAreaTheme(Prefs.getSelectedArea());
    }

    private void computeScreenSize(Context ctx) {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                WindowManager wm = ctx.getSystemService(WindowManager.class);
                if (wm != null) {
                    WindowMetrics metrics = wm.getCurrentWindowMetrics();
                    Rect b = metrics.getBounds();
                    screenW = b.width();
                    screenH = b.height();
                    return;
                }
            }
            // Fallback for older devices
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wmOld = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            if (wmOld != null) {
                //noinspection deprecation
                wmOld.getDefaultDisplay().getRealMetrics(dm);
                screenW = dm.widthPixels;
                screenH = dm.heightPixels;
                return;
            }
        } catch (Throwable ignored) { }
        // Last resort
        DisplayMetrics dmRes = ctx.getResources().getDisplayMetrics();
        screenW = dmRes.widthPixels;
        screenH = dmRes.heightPixels;
    }

    private float getStatusBarHeightPx(Context ctx) {
        int id = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? ctx.getResources().getDimensionPixelSize(id) : 0f;
    }

    public void update(float dt) {
        if (gameOver) return;

        gameTime += dt;
        speedPx += Constants.SPEED_INCREASE_PER_SEC * dt;

        bg.update(dt, speedPx);

        // Spawn entities
        spawner.update(dt, speedPx, obstacles, coins, powerUps);

        // Update entities
        player.update(dt, screenH);
        for (Obstacle o : obstacles) o.update(dt, speedPx);
        for (Coin c : coins) c.update(dt, speedPx, player, isMagnetActive());
        for (PowerUp p : powerUps) p.update(dt, speedPx);

        // Clean up off-screen
        removeOffscreen(obstacles);
        removeOffscreen(coins);
        removeOffscreen(powerUps);

        // Collisions: coins
        Iterator<Coin> ic = coins.iterator();
        while (ic.hasNext()) {
            Coin c = ic.next();
            if (collisionDetector.overlap(player.getBounds(), c.getBounds())) {
                coinCount += player.getCoinMultiplier();
                ic.remove();
            }
        }

        // Power-up pickups
        Iterator<PowerUp> ip = powerUps.iterator();
        while (ip.hasNext()) {
            PowerUp p = ip.next();
            if (collisionDetector.overlap(player.getBounds(), p.getBounds())) {
                applyPowerUp(p.getType());
                ip.remove();
            }
        }

        // Obstacles
        Iterator<Obstacle> io = obstacles.iterator();
        while (io.hasNext()) {
            Obstacle o = io.next();
            if (collisionDetector.overlap(player.getBounds(), o.getBounds())) {
                if (player.isShieldActive()) {
                    io.remove(); // shield absorbs obstacle
                } else {
                    player.kill();
                    handleDeath();
                    return;
                }
            }
        }

        // Score increases with time and speed
        score += (int) (dt * (speedPx / 10f));

        // LevelManager tick (if you later want dynamic difficulty/unlocks)
        LevelManager.getInstance().tickForUnlock(coinCount);
    }

    private void handleDeath() {
        gameOver = true;

        // Predict if a new area will unlock after adding this run's coins
        int beforeCount = Prefs.getUnlockedAreas().size();
        int totalAfter = Prefs.getTotalCoins() + coinCount;
        int afterCount = 0;
        for (int threshold : Constants.AREA_COIN_THRESHOLDS) {
            if (totalAfter >= threshold) afterCount++;
        }
        newAreaUnlocked = afterCount > beforeCount;

        if (listener != null) listener.onGameOver(score, coinCount, newAreaUnlocked);
    }

    private boolean isMagnetActive() { return player.isMagnetActive(); }

    private void applyPowerUp(PowerUpType type) {
        switch (type) {
            case MAGNET: player.activateMagnet(Constants.POWERUP_DURATION); break;
            case SHIELD: player.activateShield(Constants.POWERUP_DURATION); break;
            case DOUBLE_COINS: player.activateDoubleCoins(Constants.POWERUP_DURATION); break;
            case SPEED_BOOST: speedPx *= 1.25f; break;
        }
    }

    private <T extends Entity> void removeOffscreen(List<T> list) {
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            if (it.next().isOffscreen(screenH)) it.remove();
        }
    }

    public void draw(Canvas c) {
        bg.draw(c);

        // Subtle lane dividers (reused Paint to avoid per-frame allocations)
        for (int i = 1; i < Constants.LANE_COUNT; i++) {
            float x = (screenW / (float) Constants.LANE_COUNT) * i;
            c.drawRect(x - 2, 0, x + 2, screenH, lanePaint);
        }

        player.draw(c);
        for (Obstacle o : obstacles) o.draw(c);
        for (Coin coin : coins) coin.draw(c);
        for (PowerUp p : powerUps) p.draw(c);

        // HUD text
        String s1 = "Score: " + score;
        String s2 = "Coins: " + coinCount;
        String s3 = "Lives: " + (player.isDead() ? 0 : 1);

        // Measure the widest line
        float w = Math.max(hudPaint.measureText(s1),
                Math.max(hudPaint.measureText(s2), hudPaint.measureText(s3)));
        float h = hudLineHeight * 3f;

        // Card background
        float left = hudLeftPx;
        float top = hudTopPx;
        float right = left + w + hudPaddingPx * 2f;
        float bottom = top + h + hudPaddingPx * 2f;
        c.drawRoundRect(left, top, right, bottom, hudCornerPx, hudCornerPx, hudBgPaint);

        // Baseline helpers: drawText expects a baseline; offset by -font top
        float xText = left + hudPaddingPx;
        float yBase = top + hudPaddingPx - hudFm.top;

        c.drawText(s1, xText, yBase, hudPaint);
        yBase += hudLineHeight;
        c.drawText(s2, xText, yBase, hudPaint);
        yBase += hudLineHeight;
        c.drawText(s3, xText, yBase, hudPaint);
    }

    public void moveLeft() { player.moveLane(-1); }
    public void moveRight() { player.moveLane(1); }
    public void jump() { player.jump(); }
    public void slide() { player.slide(); }

    /**
     * Smooth tilt handling with dead zone, hysteresis and cooldown.
     * analog: expected range [-1..1], right tilt positive.
     */
    public void handleTilt(float analog) {
        // Smooth a bit in-game to be extra stable
        final float alpha = 0.15f; // higher = more responsive, less smooth
        tiltSmoothed += alpha * (analog - tiltSmoothed);
        float v = tiltSmoothed;

        long now = SystemClock.uptimeMillis();

        // Rearm latch when returning toward center
        if (tiltLatch == 1 && v < TILT_RELEASE) tiltLatch = 0;
        if (tiltLatch == -1 && v > -TILT_RELEASE) tiltLatch = 0;

        // Dead zone
        if (Math.abs(v) < TILT_DEAD_ZONE) return;

        // Rate-limit moves
        if (now - lastTiltMoveMs < TILT_COOLDOWN_MS) return;

        // Trigger moves with hysteresis
        if (v > TILT_TRIGGER && tiltLatch >= 0) {
            moveRight();
            tiltLatch = 1;
            lastTiltMoveMs = now;
            if (isDebugBuild) Log.d(TAG, "Tilt -> moveRight (v=" + v + ")");
        } else if (v < -TILT_TRIGGER && tiltLatch <= 0) {
            moveLeft();
            tiltLatch = -1;
            lastTiltMoveMs = now;
            if (isDebugBuild) Log.d(TAG, "Tilt -> moveLeft (v=" + v + ")");
        }
    }

    public void reset() {
        obstacles.clear();
        coins.clear();
        powerUps.clear();
        player.reset();
        score = 0;
        coinCount = 0;
        speedPx = Constants.BASE_SPEED * (screenH / 1920f);
        gameTime = 0f;
        gameOver = false;
        newAreaUnlocked = false;
        spawner.reset();
        applyAreaTheme(Prefs.getSelectedArea());

        // Reset tilt state
        tiltSmoothed = 0f;
        tiltLatch = 0;
        lastTiltMoveMs = 0L;
    }

    public void revive() {
        player.revive();
        gameOver = false;

        // When reviving, clear tilt latch so the next tilt is intentional
        tiltLatch = 0;
    }

    private void applyAreaTheme(int areaIndex) {
        TypedArray ta = context.getResources().obtainTypedArray(R.array.area_bg_colors);
        int color = ta.getColor(Math.max(0, Math.min(ta.length() - 1, areaIndex)), Color.DKGRAY);
        ta.recycle();
        bg.setBaseColor(color);
        LevelManager.getInstance().setCurrentArea(areaIndex);
    }
}