package com.zafar.escaperush3d.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.zafar.escaperush3d.R;
import com.zafar.escaperush3d.ui.viewmodel.MainViewModel;
import com.zafar.escaperush3d.util.AdsManager;
import com.zafar.escaperush3d.util.Prefs;

/**
 * Main menu with navigation and stats.
 */
public class MainMenuActivity extends AppCompatActivity {
    private MainViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        vm = new ViewModelProvider(this).get(MainViewModel.class);

        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnLeaderboard = findViewById(R.id.btnLeaderboard);
        Button btnAbout = findViewById(R.id.btnAbout);
        Button btnPrivacy = findViewById(R.id.btnPrivacy);
        Button btnTerms = findViewById(R.id.btnTerms);
        Button btnExit = findViewById(R.id.btnExit);
        TextView txtCoins = findViewById(R.id.txtCoins);
        TextView txtArea = findViewById(R.id.txtArea);

        vm.getTotalCoins().observe(this, coins -> txtCoins.setText("Coins: " + coins));

        int area = Prefs.getSelectedArea();
        String[] names = getResources().getStringArray(R.array.area_names);
        txtArea.setText("Area: " + names[area]);

        btnPlay.setOnClickListener(v -> {
            AdsManager.getInstance().preloadInterstitial(this);
            AdsManager.getInstance().preloadRewarded(this);
            startActivity(new Intent(this, GameActivity.class));
        });
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        btnLeaderboard.setOnClickListener(v -> startActivity(new Intent(this, LeaderboardActivity.class)));
        btnAbout.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
        btnPrivacy.setOnClickListener(v -> startActivity(new Intent(this, PrivacyActivity.class)));
        btnTerms.setOnClickListener(v -> startActivity(new Intent(this, TermsActivity.class)));
        btnExit.setOnClickListener(v -> finishAffinity());
    }

    @Override
    protected void onResume() {
        super.onResume();
        vm.refreshCoins();
    }
}