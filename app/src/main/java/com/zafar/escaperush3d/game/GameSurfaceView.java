package com.zafar.escaperush3d.game;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zafar.escaperush3d.game.input.SwipeDetector;
import com.zafar.escaperush3d.game.input.TiltController;
import com.zafar.escaperush3d.util.Prefs;

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "GameSurfaceView";

    private GameThread thread;
    private GameWorld world;
    private final SwipeDetector swipeDetector;
    private TiltController tiltController;
    private GameUiCallbacks callbacks;
    private Context ctx;

    public interface GameUiCallbacks {
        void onGameOver(int score, int coins, boolean newAreaUnlocked);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;

        // Avoid preview crash in XML layout editor
        if (isInEditMode()) {
            swipeDetector = null;
            return;
        }

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

    public void setCallbacks(GameUiCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (isInEditMode()) return; // Skip in preview mode

        Log.d(TAG, "surfaceCreated: Starting game world");

        world = new GameWorld(ctx, (score, coins, unlocked) -> {
            Log.d(TAG, "Game Over triggered in GameSurfaceView");
            if (callbacks != null) post(() -> callbacks.onGameOver(score, coins, unlocked));
        });

        thread = new GameThread(getHolder(), world);
        thread.setRunning(true);
        thread.start();

        if ("tilt".equals(Prefs.getControlScheme())) {
            tiltController.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed: Stopping game thread");
        if (tiltController != null) tiltController.stop();
        if (thread != null) {
            thread.setRunning(false);
            boolean retry = true;
            while (retry) {
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException ignored) {}
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ("swipe".equals(Prefs.getControlScheme()) && swipeDetector != null) {
            swipeDetector.onTouchEvent(event);
            return true; // keep receiving the full gesture stream
        }
        return super.onTouchEvent(event);
    }

    public void pauseGame() {
        if (thread != null) {
            Log.d(TAG, "pauseGame: Game paused");
            thread.setPaused(true);
        }
    }

    public void resumeGame() {
        if (thread != null) {
            Log.d(TAG, "resumeGame: Game resumed");
            thread.setPaused(false);
        }
    }

    public void startNewRun() {
        if (thread != null && world != null) {
            Log.d(TAG, "startNewRun: Resetting game");
            thread.post(world::reset); // do it on the game thread
        }
    }

    public void revivePlayer() {
        if (thread != null && world != null) {
            Log.d(TAG, "revivePlayer: Reviving player");
            thread.post(world::revive); // do it on the game thread
            resumeGame(); // ensure game loop resumes
        }
    }

    /** Called from GameActivity when player revives after ad */
    public void resumeAfterRevive() {
        Log.d(TAG, "resumeAfterRevive: Called from GameActivity");
        revivePlayer();
    }
}
