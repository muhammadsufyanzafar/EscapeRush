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
                    gameView.revivePlayer();
                }
                @Override public void onClosedNoReward() {}
            });
        });
        btnDoubleCoins.setOnClickListener(v -> {
            AdsManager.getInstance().showRewarded(this, AdsManager.RewardPurpose.DOUBLE_COINS, new AdsManager.RewardCallback() {
                @Override public void onReward(AdsManager.RewardPurpose purpose) {
                    finalCoins *= 2;
                    txtFinalCoins.setText("Coins: " + finalCoins);
                }
                @Override public void onClosedNoReward() {}
            });
        });
        btnGameOverMenu.setOnClickListener(v -> finish());
        btnGameOverRestart.setOnClickListener(v -> restartRun());
    }

    private void restartRun() {
        // Hide overlays
        if (pauseOverlay.getVisibility() == View.VISIBLE) {
            pauseOverlay.setVisibility(View.GONE);
        }
        gameOverOverlay.setVisibility(View.GONE);

        // Reset flags
        reviveUsed = false;

        // Make sure the game loop is running
        gameView.resumeGame();

        // Reset world safely on the game thread
        gameView.startNewRun();
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

        // Persist score and coins; unlocks are handled here after coins added
        GameRepository.addScore(score, coins);

        txtFinalScore.setText("Score: " + score);
        txtFinalCoins.setText("Coins: " + finalCoins);
        txtNewArea.setVisibility(newAreaUnlocked ? View.VISIBLE : View.GONE);
        gameOverOverlay.setVisibility(View.VISIBLE);

        // Show an interstitial ad between runs (compliant with policies)
        AdsManager.getInstance().showInterstitialIfReady(this, null);
    }
}