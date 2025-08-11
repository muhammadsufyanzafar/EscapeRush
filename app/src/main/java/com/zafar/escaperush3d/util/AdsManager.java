package com.zafar.escaperush3d.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import java.util.concurrent.atomic.AtomicBoolean;

public class AdsManager {

    private static final String TAG = "AdsManager";

    public enum RewardPurpose { REVIVE, DOUBLE_COINS }

    private static AdsManager instance;
    public static AdsManager getInstance() {
        if (instance == null) instance = new AdsManager();
        return instance;
    }

    private final AtomicBoolean interstitialLoaded = new AtomicBoolean(false);
    private final AtomicBoolean rewardedLoaded = new AtomicBoolean(false);

    private RewardPurpose pendingRewardPurpose = null;
    private RewardCallback pendingRewardCallback = null;

    public interface RewardCallback {
        void onReward(RewardPurpose purpose);
        void onClosedNoReward();
    }

    public void init(Context context) {
        interstitialLoaded.set(false);
        rewardedLoaded.set(false);
    }

    // ------------------- Interstitial -------------------
    public void preloadInterstitial() {
        UnityAds.load(Constants.UNITY_INTERSTITIAL_PLACEMENT_ID, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                interstitialLoaded.set(true);
                Log.d(TAG, "Interstitial loaded: " + placementId);
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                interstitialLoaded.set(false);
                Log.w(TAG, "Failed to load interstitial: " + message);
            }
        });
    }

    public void showInterstitialIfReady(Activity activity, @Nullable Runnable onClosed) {
        if (!interstitialLoaded.get()) {
            preloadInterstitial();
            if (onClosed != null) onClosed.run();
            return;
        }

        UnityAds.show(activity, Constants.UNITY_INTERSTITIAL_PLACEMENT_ID, new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                Log.w(TAG, "Interstitial show failed: " + message);
                if (onClosed != null) onClosed.run();
                interstitialLoaded.set(false);
            }

            @Override public void onUnityAdsShowStart(String placementId) {}
            @Override public void onUnityAdsShowClick(String placementId) {}
            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                if (onClosed != null) onClosed.run();
                interstitialLoaded.set(false);
                preloadInterstitial();
            }
        });
    }

    // ------------------- Rewarded -------------------
    public void preloadRewarded() {
        UnityAds.load(Constants.UNITY_REWARDED_PLACEMENT_ID, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                rewardedLoaded.set(true);
                Log.d(TAG, "Rewarded loaded: " + placementId);
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                rewardedLoaded.set(false);
                Log.w(TAG, "Failed to load rewarded: " + message);
            }
        });
    }

    public void showRewarded(Activity activity, RewardPurpose purpose, RewardCallback callback) {
        if (!rewardedLoaded.get()) {
            preloadRewarded();
            callback.onClosedNoReward();
            return;
        }

        pendingRewardPurpose = purpose;
        pendingRewardCallback = callback;

        UnityAds.show(activity, Constants.UNITY_REWARDED_PLACEMENT_ID, new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                Log.w(TAG, "Rewarded show failed: " + message);
                if (pendingRewardCallback != null) pendingRewardCallback.onClosedNoReward();
                clearRewardState();
            }

            @Override public void onUnityAdsShowStart(String placementId) {}
            @Override public void onUnityAdsShowClick(String placementId) {}
            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    if (pendingRewardCallback != null && pendingRewardPurpose != null) {
                        pendingRewardCallback.onReward(pendingRewardPurpose);
                    }
                } else {
                    if (pendingRewardCallback != null) pendingRewardCallback.onClosedNoReward();
                }
                clearRewardState();
                preloadRewarded();
            }
        });
    }

    private void clearRewardState() {
        pendingRewardPurpose = null;
        pendingRewardCallback = null;
        rewardedLoaded.set(false);
    }
}
