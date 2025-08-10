package com.zafar.escaperush3d.ui.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zafar.escaperush3d.R;
import com.zafar.escaperush3d.util.ConsentManager;

/**
 * Splash screen with simple animation and consent flow.
 */
public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_MS = 3000;
    private boolean consentReady = false;
    private boolean timerElapsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logo);
        TextView title = findViewById(R.id.title);

        ObjectAnimator.ofFloat(logo, "scaleX", 0.6f, 1.0f).setDuration(1000).start();
        ObjectAnimator.ofFloat(logo, "scaleY", 0.6f, 1.0f).setDuration(1000).start();
        ObjectAnimator.ofFloat(title, "alpha", 0f, 1f).setDuration(1200).start();

        // Consent
        ConsentManager.requestConsent(this, () -> {
            consentReady = true;
            maybeGoNext();
        });

        // Timer
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            timerElapsed = true;
            maybeGoNext();
        }, SPLASH_MS);
    }

    private void maybeGoNext() {
        if (consentReady && timerElapsed) {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        }
    }
}