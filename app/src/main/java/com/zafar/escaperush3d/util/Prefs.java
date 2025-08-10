package com.zafar.escaperush3d.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple shared preferences wrapper.
 */
public class Prefs {
    private static SharedPreferences sp;
    private static final String NAME = "escaperush_prefs";

    private static final String KEY_TOTAL_COINS = "total_coins";
    private static final String KEY_TOP_SCORES = "top_scores_json";
    private static final String KEY_CONTROL_SCHEME = "control_scheme"; // "swipe" or "tilt"
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_UNLOCKED_AREAS = "unlocked_areas";
    private static final String KEY_SELECTED_AREA = "selected_area"; // 0..n

    public static void init(Context ctx) {
        sp = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        if (!sp.contains(KEY_CONTROL_SCHEME)) {
            sp.edit().putString(KEY_CONTROL_SCHEME, "swipe").apply();
        }
        if (!sp.contains(KEY_SOUND_ENABLED)) {
            sp.edit().putBoolean(KEY_SOUND_ENABLED, true).apply();
        }
        if (!sp.contains(KEY_UNLOCKED_AREAS)) {
            Set<String> set = new HashSet<>();
            set.add("0"); // Forest unlocked by default
            sp.edit().putStringSet(KEY_UNLOCKED_AREAS, set).apply();
        }
    }

    public static int getTotalCoins() { return sp.getInt(KEY_TOTAL_COINS, 0); }
    public static void addTotalCoins(int delta) { sp.edit().putInt(KEY_TOTAL_COINS, Math.max(0, getTotalCoins() + delta)).apply(); }

    public static String getTopScoresJson() { return sp.getString(KEY_TOP_SCORES, "[]"); }
    public static void setTopScoresJson(String json) { sp.edit().putString(KEY_TOP_SCORES, json).apply(); }

    public static String getControlScheme() { return sp.getString(KEY_CONTROL_SCHEME, "swipe"); }
    public static void setControlScheme(String scheme) { sp.edit().putString(KEY_CONTROL_SCHEME, scheme).apply(); }

    public static boolean isSoundEnabled() { return sp.getBoolean(KEY_SOUND_ENABLED, true); }
    public static void setSoundEnabled(boolean enabled) { sp.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply(); }

    public static Set<String> getUnlockedAreas() { return new HashSet<>(sp.getStringSet(KEY_UNLOCKED_AREAS, new HashSet<>())); }
    public static void unlockArea(int areaIndex) {
        Set<String> s = getUnlockedAreas();
        s.add(String.valueOf(areaIndex));
        sp.edit().putStringSet(KEY_UNLOCKED_AREAS, s).apply();
    }

    public static int getSelectedArea() { return sp.getInt(KEY_SELECTED_AREA, 0); }
    public static void setSelectedArea(int areaIndex) { sp.edit().putInt(KEY_SELECTED_AREA, areaIndex).apply(); }
}