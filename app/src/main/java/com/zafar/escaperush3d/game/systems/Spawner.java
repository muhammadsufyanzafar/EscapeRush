package com.zafar.escaperush3d.game.systems;

import com.zafar.escaperush3d.game.entities.Coin;
import com.zafar.escaperush3d.game.entities.Obstacle;
import com.zafar.escaperush3d.game.entities.PowerUp;
import com.zafar.escaperush3d.game.entities.PowerUpType;

import java.util.List;
import java.util.Random;

/**
 * Spawns obstacles, coins, and powerups over time.
 */
public class Spawner {
    private final int screenW, screenH;
    private final float[] lanesX;
    private float obstacleTimer = 0f;
    private float coinTimer = 0f;
    private float powerUpTimer = 0f;

    private final Random rng = new Random();

    public Spawner(int screenW, int screenH, float[] lanesX) {
        this.screenW = screenW;
        this.screenH = screenH;
        this.lanesX = lanesX;
    }

    public void reset() { obstacleTimer = coinTimer = powerUpTimer = 0f; }

    public void update(float dt, float speedPx, List<Obstacle> obstacles, List<Coin> coins, List<PowerUp> powerUps) {
        obstacleTimer -= dt;
        coinTimer -= dt;
        powerUpTimer -= dt;

        float obstacleInterval = Math.max(0.5f, 1.6f - (speedPx / 800f)); // faster => more frequent
        float coinInterval = 0.35f;
        float powerInterval = 8f;

        if (obstacleTimer <= 0) {
            int lane = rng.nextInt(lanesX.length);
            obstacles.add(new Obstacle(lanesX[lane], -screenH * 0.1f));
            obstacleTimer = obstacleInterval;
        }

        if (coinTimer <= 0) {
            int startLane = rng.nextInt(lanesX.length);
            float y = -screenH * 0.15f;
            for (int i = 0; i < 5; i++) {
                coins.add(new Coin(lanesX[startLane], y - i * 80f));
            }
            coinTimer = coinInterval;
        }

        if (powerUpTimer <= 0) {
            int lane = rng.nextInt(lanesX.length);
            PowerUpType type = PowerUpType.values()[rng.nextInt(PowerUpType.values().length)];
            powerUps.add(new PowerUp(lanesX[lane], -screenH * 0.2f, type));
            powerUpTimer = powerInterval + rng.nextFloat() * 5f;
        }
    }
}