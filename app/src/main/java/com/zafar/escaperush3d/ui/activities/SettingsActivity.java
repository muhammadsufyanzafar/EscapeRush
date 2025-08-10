package com.zafar.escaperush3d.ui.activities;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.zafar.escaperush3d.R;
import com.zafar.escaperush3d.ui.viewmodel.SettingsViewModel;
import com.zafar.escaperush3d.util.Constants;
import com.zafar.escaperush3d.util.Prefs;

import java.util.Set;

/**
 * Settings: control scheme, sound, area selection (unlocked only).
 */
public class SettingsActivity extends AppCompatActivity {
    private SettingsViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        vm = new ViewModelProvider(this).get(SettingsViewModel.class);

        RadioButton rbSwipe = findViewById(R.id.rbSwipe);
        RadioButton rbTilt = findViewById(R.id.rbTilt);
        Switch swSound = findViewById(R.id.swSound);
        TextView txtArea = findViewById(R.id.txtSelectedArea);
        TextView btnPrev = findViewById(R.id.btnPrevArea);
        TextView btnNext = findViewById(R.id.btnNextArea);

        rbSwipe.setChecked("swipe".equals(vm.getControlScheme()));
        rbTilt.setChecked("tilt".equals(vm.getControlScheme()));
        rbSwipe.setOnCheckedChangeListener((b, v) -> { if (v) vm.setControlScheme("swipe"); });
        rbTilt.setOnCheckedChangeListener((b, v) -> { if (v) vm.setControlScheme("tilt"); });

        swSound.setChecked(vm.isSoundEnabled());
        swSound.setOnCheckedChangeListener((b, v) -> vm.setSoundEnabled(v));

        String[] areas = getResources().getStringArray(R.array.area_names);
        Set<String> unlocked = Prefs.getUnlockedAreas();

        Runnable updateAreaText = () -> {
            int idx = vm.getSelectedArea();
            String next = getNextThresholdText();
            txtArea.setText("Area: " + areas[idx] + (next.isEmpty() ? "" : "  (Next unlock at " + next + " coins)"));
        };
        updateAreaText.run();

        btnPrev.setOnClickListener(v -> {
            int idx = vm.getSelectedArea();
            int next = Math.max(0, idx - 1);
            if (unlocked.contains(String.valueOf(next))) {
                vm.setSelectedArea(next);
                updateAreaText.run();
            } else {
                Toast.makeText(this, "Area locked!", Toast.LENGTH_SHORT).show();
            }
        });
        btnNext.setOnClickListener(v -> {
            int idx = vm.getSelectedArea();
            int next = Math.min(areas.length - 1, idx + 1);
            if (unlocked.contains(String.valueOf(next))) {
                vm.setSelectedArea(next);
                updateAreaText.run();
            } else {
                Toast.makeText(this, "Area locked!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getNextThresholdText() {
        int[] t = Constants.AREA_COIN_THRESHOLDS;
        int sel = Prefs.getSelectedArea();
        if (sel + 1 < t.length) return String.valueOf(t[sel + 1]);
        return "";
    }
}