package com.zafar.escaperush3d.game.systems;

import com.zafar.escaperush3d.util.Constants;
import com.zafar.escaperush3d.util.Prefs;

/**
 * Tracks area index and helper unlock logic.
 */
public class LevelManager {
    private static LevelManager instance;
    public static LevelManager getInstance() { if (instance == null) instance = new LevelManager(); return instance; }

    private int currentArea = 0;
    private int coinsThisRun = 0;

    public void setCurrentArea(int idx) { this.currentArea = idx; }
    public int getCurrentArea() { return currentArea; }

    public void tickForUnlock(int coins) { coinsThisRun = coins; }

    public int computeUnlockedCount() {
        int total = Prefs.getTotalCoins();
        int unlocked = 0;
        for (int threshold : Constants.AREA_COIN_THRESHOLDS) {
            if (total >= threshold) unlocked++;
        }
        return unlocked;
    }
}