package com.zafar.escaperush3d.data.model;

/**
 * Leaderboard score entry (local).
 */
public class ScoreEntry {
    public String date; // yyyy-MM-dd
    public int score;
    public int coins;

    public ScoreEntry() {}

    public ScoreEntry(String date, int score, int coins) {
        this.date = date;
        this.score = score;
        this.coins = coins;
    }
}