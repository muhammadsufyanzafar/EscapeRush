package com.zafar.escaperush3d.game;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayDeque;

/**
 * Fixed timestep-ish game loop with a small task queue for thread-safe world ops.
 */
public class GameThread extends Thread {
    private final SurfaceHolder holder;
    private final GameWorld world;
    private boolean running = false;
    private boolean paused = false;

    private final ArrayDeque<Runnable> taskQueue = new ArrayDeque<>();

    public GameThread(SurfaceHolder holder, GameWorld world) {
        this.holder = holder;
        this.world = world;
    }

    public void setRunning(boolean running) { this.running = running; }
    public void setPaused(boolean paused) { this.paused = paused; }

    // Post tasks to be executed on the game thread (e.g., world.reset(), world.revive())
    public void post(Runnable r) {
        synchronized (taskQueue) { taskQueue.addLast(r); }
    }

    @Override
    public void run() {
        long last = System.nanoTime();
        final double nsPerSec = 1_000_000_000.0;

        while (running) {
            if (paused) {
                try { sleep(16); } catch (InterruptedException ignored) {}
                last = System.nanoTime();
                continue;
            }

            long now = System.nanoTime();
            float dt = (float) ((now - last) / nsPerSec);
            if (dt > 0.05f) dt = 0.05f; // clamp
            last = now;

            // Run queued tasks on the game thread
            try {
                synchronized (taskQueue) {
                    while (!taskQueue.isEmpty()) {
                        taskQueue.removeFirst().run();
                    }
                }
            } catch (Throwable t) {
                Log.e("GameThread", "Task error", t);
            }

            try {
                world.update(dt);
            } catch (Throwable t) {
                Log.e("GameThread", "Update error", t);
            }

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) world.draw(c);
            } catch (Throwable t) {
                Log.e("GameThread", "Draw error", t);
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }
        }
    }
}