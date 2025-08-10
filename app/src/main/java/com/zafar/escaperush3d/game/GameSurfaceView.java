package com.zafar.escaperush3d.game;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zafar.escaperush3d.game.input.SwipeDetector;
import com.zafar.escaperush3d.game.input.TiltController;
import com.zafar.escaperush3d.util.Prefs;

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread thread;
    private GameWorld world;
    private final SwipeDetector swipeDetector;
    private final TiltController tiltController;
    private GameUiCallbacks callbacks;

    public interface GameUiCallbacks {
        void onGameOver(int score, int coins, boolean newAreaUnlocked);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setFocusable(true);

        swipeDetector = new SwipeDetector(context, new SwipeDetector.Callback() {
            @Override public void onSwipeLeft()  { if (world != null) world.moveLeft(); }
            @Override public void onSwipeRight() { if (world != null) world.moveRight(); }
            @Override public void onSwipeUp()    { if (world != null) world.jump(); }
            @Override public void onSwipeDown()  { if (world != null) world.slide(); }
        });

        tiltController = new TiltController(context, dx -> {
            if (world != null) world.handleTilt(dx);
        });
    }

    public void setCallbacks(GameUiCallbacks callbacks) { this.callbacks = callbacks; }

    @Override public void surfaceCreated(SurfaceHolder holder) {
        world = new GameWorld(getContext(), (score, coins, unlocked) -> {
            if (callbacks != null) post(() -> callbacks.onGameOver(score, coins, unlocked));
        });
        thread = new GameThread(getHolder(), world);
        thread.setRunning(true);
        thread.start();
        if ("tilt".equals(Prefs.getControlScheme())) tiltController.start();
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        tiltController.stop();
        if (thread != null) {
            thread.setRunning(false);
            boolean retry = true;
            while (retry) {
                try { thread.join(); retry = false; } catch (InterruptedException ignored) {}
            }
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if ("swipe".equals(Prefs.getControlScheme())) {
            swipeDetector.onTouchEvent(event);
            return true; // keep receiving the full gesture stream
        }
        return super.onTouchEvent(event);
    }

    public void pauseGame() {
        if (thread != null) thread.setPaused(true);
    }

    public void resumeGame() {
        if (thread != null) thread.setPaused(false);
    }

    public void startNewRun() {
        if (thread != null && world != null) {
            thread.post(world::reset); // do it on the game thread
        }
    }

    public void revivePlayer() {
        if (thread != null && world != null) {
            thread.post(world::revive); // do it on the game thread
        }
    }
}