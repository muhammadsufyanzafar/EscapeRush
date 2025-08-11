package com.zafar.escaperush3d.util;

public final class Constants {
    private Constants() {}

    // -------------------- Unity Ads --------------------
    public static final String UNITY_GAME_ID = "5920721";
    public static final String UNITY_INTERSTITIAL_PLACEMENT_ID = "Interstitial_Android";
    public static final String UNITY_REWARDED_PLACEMENT_ID = "Rewarded_Android";
    boolean testMode = true; // keep true until ads are working


    // -------------------- Game Settings --------------------
    public static final int LANE_COUNT = 3;
    public static final float BASE_SPEED = 450f;
    public static final float SPEED_INCREASE_PER_SEC = 2.0f;
    public static final float PLAYER_JUMP_VELOCITY = -1150f;
    public static final float GRAVITY = 2600f;
    public static final float SLIDE_DURATION = 0.75f;
    public static final float POWERUP_DURATION = 8f;
    public static final float MAGNET_RADIUS = 240f;

    // Level unlock thresholds (total coins)
    public static final int[] AREA_COIN_THRESHOLDS = { 0, 300, 900 };
}



