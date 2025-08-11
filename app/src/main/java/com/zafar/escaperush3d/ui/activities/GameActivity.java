package com.zafar.escaperush3d.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zafar.escaperush3d.R;
import com.zafar.escaperush3d.game.GameSurfaceView;
import com.zafar.escaperush3d.util.AdsManager;

public class GameActivity extends AppCompatActivity implements GameSurfaceView.GameUiCallbacks {

    private static final String TAG = "GameActivity";

    private GameSurfaceView gameView;
    private LinearLayout overlayPause, overlayGameOver;
    private Button btnRevive, btnDoubleCoins;
    private TextView txtFinalScore, txtFinalCoins, txtNewArea;

    private int lastRunCoins = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Game view
        gameView = findViewById(R.id.gameView);
        gameView.setCallbacks(this);

        // Overlays
        overlayPause = findViewById(R.id.overlayPause);
        overlayGameOver = findViewById(R.id.overlayGameOver);

        // Game Over UI
        txtFinalScore = findViewById(R.id.txtFinalScore);
        txtFinalCoins = findViewById(R.id.txtFinalCoins);
        txtNewArea = findViewById(R.id.txtNewArea);
        btnRevive = findViewById(R.id.btnRevive);
        btnDoubleCoins = findViewById(R.id.btnDoubleCoins);

        // Pause button
        findViewById(R.id.btnPause).setOnClickListener(v -> {
            gameView.pauseGame();
            overlayPause.setVisibility(View.VISIBLE);
        });

        // Pause overlay buttons
        findViewById(R.id.btnResume).setOnClickListener(v -> {
            overlayPause.setVisibility(View.GONE);
            gameView.resumeGame();
        });

        findViewById(R.id.btnRestart).setOnClickListener(v -> {
            overlayPause.setVisibility(View.GONE);
            overlayGameOver.setVisibility(View.GONE);
            gameView.startNewRun();
        });

        findViewById(R.id.btnMainMenu).setOnClickListener(v -> finish());

        // Game Over overlay buttons
        findViewById(R.id.btnGameOverRestart).setOnClickListener(v -> {
            overlayGameOver.setVisibility(View.GONE);
            gameView.startNewRun();
        });

        findViewById(R.id.btnGameOverMenu).setOnClickListener(v -> finish());

        // Revive button with ad
        btnRevive.setOnClickListener(v -> {
            Log.d(TAG, "Revive button clicked — loading ad");
            AdsManager.getInstance().showRewarded(
                    GameActivity.this,
                    AdsManager.RewardPurpose.REVIVE,
                    new AdsManager.RewardCallback() {
                        @Override
                        public void onReward(AdsManager.RewardPurpose purpose) {
                            Log.d(TAG, "Revive reward granted");
                            overlayGameOver.setVisibility(View.GONE);
                            gameView.resumeAfterRevive();
                        }

                        @Override
                        public void onClosedNoReward() {
                            Log.d(TAG, "Revive ad closed without reward");
                        }
                    }
            );
        });

        // Double coins button with ad
        btnDoubleCoins.setOnClickListener(v -> {
            Log.d(TAG, "Double coins button clicked — loading ad");
            AdsManager.getInstance().showRewarded(
                    GameActivity.this,
                    AdsManager.RewardPurpose.DOUBLE_COINS,
                    new AdsManager.RewardCallback() {
                        @Override
                        public void onReward(AdsManager.RewardPurpose purpose) {
                            Log.d(TAG, "Double coins reward granted");
                            lastRunCoins *= 2;
                            txtFinalCoins.setText("Coins: " + lastRunCoins);
                        }

                        @Override
                        public void onClosedNoReward() {
                            Log.d(TAG, "Double coins ad closed without reward");
                        }
                    }
            );
        });
    }

    @Override
    public void onGameOver(int score, int coins, boolean newAreaUnlocked) {
        Log.d(TAG, "Game Over received from GameSurfaceView");
        runOnUiThread(() -> {
            lastRunCoins = coins;
            txtFinalScore.setText("Score: " + score);
            txtFinalCoins.setText("Coins: " + coins);
            txtNewArea.setVisibility(newAreaUnlocked ? View.VISIBLE : View.GONE);
            overlayGameOver.setVisibility(View.VISIBLE);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resumeGame();
    }
}
