package com.zafar.escaperush3d.util;

public final class Constants {
    private Constants() {}

    // AppLovin MAX (replace with your IDs)
    public static final String MAX_INTERSTITIAL_AD_UNIT_ID = "YOUR_MAX_INTERSTITIAL_AD_UNIT_ID";
    public static final String MAX_REWARDED_AD_UNIT_ID = "YOUR_MAX_REWARDED_AD_UNIT_ID";

    // Game constants
    public static final int LANE_COUNT = 3;
    public static final float BASE_SPEED = 450f; // px/sec at 1080p, scaled by device
    public static final float SPEED_INCREASE_PER_SEC = 2.0f;
    public static final float PLAYER_JUMP_VELOCITY = -1150f;
    public static final float GRAVITY = 2600f;
    public static final float SLIDE_DURATION = 0.75f; // seconds
    public static final float POWERUP_DURATION = 8f; // seconds
    public static final float MAGNET_RADIUS = 240f;

    // Level unlock thresholds (total coins)
    public static final int[] AREA_COIN_THRESHOLDS = { 0, 300, 900 }; // Forest, Desert, City
}