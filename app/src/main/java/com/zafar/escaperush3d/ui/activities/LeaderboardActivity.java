package com.zafar.escaperush3d.ui.activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.zafar.escaperush3d.R;
import com.zafar.escaperush3d.data.model.ScoreEntry;
import com.zafar.escaperush3d.ui.viewmodel.LeaderboardViewModel;

import java.util.ArrayList;

/**
 * Displays top scores saved locally.
 */
public class LeaderboardActivity extends AppCompatActivity {
    private LeaderboardViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        vm = new ViewModelProvider(this).get(LeaderboardViewModel.class);
        LinearLayout container = findViewById(R.id.containerScores);

        vm.getLeaderboard().observe(this, list -> {
            container.removeAllViews();
            int rank = 1;
            for (ScoreEntry e : list) {
                TextView tv = (TextView) getLayoutInflater().inflate(R.layout.item_score, container, false);
                tv.setText(rank + ". " + e.score + " pts  •  " + e.coins + " coins  •  " + e.date);
                container.addView(tv);
                rank++;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        vm.refresh();
    }
}