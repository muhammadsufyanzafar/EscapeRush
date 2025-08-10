package com.zafar.escaperush3d.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zafar.escaperush3d.R;
import com.zafar.escaperush3d.data.GameRepository;
import com.zafar.escaperush3d.game.GameSurfaceView;
import com.zafar.escaperush3d.util.AdsManager;

/**
 * Game screen hosting the SurfaceView and overlays.
 */
public class GameActivity extends AppCompatActivity implements GameSurfaceView.GameUiCallbacks {
    private GameSurfaceView gameView;
    private View pauseOverlay;
    private View gameOverOverlay;
    private TextView txtFinalScore, txtFinalCoins, txtNewArea;
    private int finalScore = 0, finalCoins = 0;
    private boolean reviveUsed = false; // allow one revive per run

    private boolean resultsCommitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.gameView);
        gameView.setCallbacks(this);

        pauseOverlay = findViewById(R.id.overlayPause);
        gameOverOverlay = findViewById(R.id.overlayGameOver);
        txtFinalScore = findViewById(R.id.txtFinalScore);
        txtFinalCoins = findViewById(R.id.txtFinalCoins);
        txtNewArea = findViewById(R.id.txtNewArea);
        txtNewArea.setVisibility(View.GONE);

        findViewById(R.id.btnPause).setOnClickListener(v -> {
            gameView.pauseGame();
            pauseOverlay.setVisibility(View.VISIBLE);
        });
        findViewById(R.id.btnResume).setOnClickListener(v -> {
            pauseOverlay.setVisibility(View.GONE);
            gameView.resumeGame();
        });
        findViewById(R.id.btnRestart).setOnClickListener(v -> restartRun());
        findViewById(R.id.btnMainMenu).setOnClickListener(v -> finish());

        Button btnRevive = findViewById(R.id.btnRevive);
        Button btnDoubleCoins = findViewById(R.id.btnDoubleCoins);
        Button btnGameOverMenu = findViewById(R.id.btnGameOverMenu);
        Button btnGameOverRestart = findViewById(R.id.btnGameOverRestart);

        btnRevive.setOnClickListener(v -> {
            if (reviveUsed) return;
            AdsManager.getInstance().showRewarded(this, AdsManager.RewardPurpose.REVIVE, new AdsManager.RewardCallback() {
                @Override public void onReward(AdsManager.RewardPurpose purpose) {
                    reviveUsed = true;
                    gameOverOverlay.setVisibility(View.GONE);
                    gameView.revivePlayer(); // do NOT commit results yet
                }
                @Override public void onClosedNoReward() {}
            });
        });

        btnDoubleCoins.setOnClickListener(v -> {
            AdsManager.getInstance().showRewarded(this, AdsManager.RewardPurpose.DOUBLE_COINS, new AdsManager.RewardCallback() {
                @Override public void onReward(AdsManager.RewardPurpose purpose) {
                    finalCoins *= 2;                 // change pending
                    txtFinalCoins.setText("Coins: " + finalCoins);
                    updateNewAreaLabel();            // recompute unlock preview
                }
                @Override public void onClosedNoReward() {}
            });
        });

        btnGameOverMenu.setOnClickListener(v -> {
            commitResultsIfNeeded();
            finish();
        });

        btnGameOverRestart.setOnClickListener(v -> restartRun());
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (pauseOverlay.getVisibility() == View.VISIBLE) {
            pauseOverlay.setVisibility(View.GONE);
            gameView.resumeGame();
        } else {
            gameView.pauseGame();
            pauseOverlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onGameOver(int score, int coins, boolean newAreaUnlocked) {
        finalScore = score;
        finalCoins = coins;
        resultsCommitted = false;

        txtFinalScore.setText("Score: " + finalScore);
        txtFinalCoins.setText("Coins: " + finalCoins);
        // Predict unlock with current totals + pending coins
        updateNewAreaLabel();

        gameOverOverlay.setVisibility(View.VISIBLE);

        AdsManager.getInstance().showInterstitialIfReady(this, null);
    }

    private void updateNewAreaLabel() {
        int[] thresholds = com.zafar.escaperush3d.util.Constants.AREA_COIN_THRESHOLDS;
        int beforeTotal = com.zafar.escaperush3d.util.Prefs.getTotalCoins();
        int beforeUnlocked = 0, afterUnlocked = 0;
        for (int t : thresholds) if (beforeTotal >= t) beforeUnlocked++;
        int afterTotal = beforeTotal + finalCoins;
        for (int t : thresholds) if (afterTotal >= t) afterUnlocked++;
        txtNewArea.setVisibility(afterUnlocked > beforeUnlocked ? View.VISIBLE : View.GONE);
    }

    private void commitResultsIfNeeded() {
        if (resultsCommitted) return;
        GameRepository.addScore(finalScore, finalCoins);
        resultsCommitted = true;
    }

    private void restartRun() {
        // Hide overlays
        if (pauseOverlay.getVisibility() == View.VISIBLE) pauseOverlay.setVisibility(View.GONE);
        gameOverOverlay.setVisibility(View.GONE);

        // Commit the last run before restarting
        commitResultsIfNeeded();

        reviveUsed = false;
        gameView.resumeGame();
        gameView.startNewRun();
    }

    @Override
    protected void onPause() {
        gameView.pauseGame();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resumeGame();
        // Keep screen on instead of WAKE_LOCK permission
        gameView.setKeepScreenOn(true);
    }
}