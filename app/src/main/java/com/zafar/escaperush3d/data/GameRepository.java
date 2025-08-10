package com.zafar.escaperush3d.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zafar.escaperush3d.data.model.ScoreEntry;
import com.zafar.escaperush3d.util.Constants;
import com.zafar.escaperush3d.util.Prefs;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Handles leaderboard and persistent stats.
 */
public class GameRepository {
    private static final Gson gson = new Gson();
    private static final Type LIST_TYPE = new TypeToken<ArrayList<ScoreEntry>>() {}.getType();

    public static ArrayList<ScoreEntry> getLeaderboard() {
        String json = Prefs.getTopScoresJson();
        ArrayList<ScoreEntry> list = gson.fromJson(json, LIST_TYPE);
        if (list == null) list = new ArrayList<>();
        return list;
    }

    public static void addScore(int score, int coins) {
        ArrayList<ScoreEntry> list = getLeaderboard();
        list.add(new ScoreEntry(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), score, coins));
        Collections.sort(list, (a, b) -> Integer.compare(b.score, a.score));
        while (list.size() > 20) list.remove(list.size() - 1);
        Prefs.setTopScoresJson(gson.toJson(list));

        // Add coins to total and unlock areas if thresholds met
        Prefs.addTotalCoins(coins);
        int totalCoins = Prefs.getTotalCoins();
        for (int i = 0; i < Constants.AREA_COIN_THRESHOLDS.length; i++) {
            if (totalCoins >= Constants.AREA_COIN_THRESHOLDS[i]) {
                Prefs.unlockArea(i);
            }
        }
    }
}