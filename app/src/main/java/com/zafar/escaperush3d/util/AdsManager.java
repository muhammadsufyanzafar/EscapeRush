package com.zafar.escaperush3d.util;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.Nullable;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.mediation.MaxRewardedAdListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central ad manager for interstitial and rewarded ads.
 * Compatible with AppLovin SDK 12.x (uses onUserRewarded(MaxAd, MaxReward)).
 */
public class AdsManager {
    public enum RewardPurpose { REVIVE, DOUBLE_COINS }

    private static AdsManager instance;
    public static AdsManager getInstance() {
        if (instance == null) instance = new AdsManager();
        return instance;
    }

    private MaxInterstitialAd interstitialAd;
    private MaxRewardedAd rewardedAd;

    private final AtomicBoolean interstitialReady = new AtomicBoolean(false);
    private final AtomicBoolean rewardedReady = new AtomicBoolean(false);

    public void init(Context context) {
        // SDK initialization occurs in App.java
    }

    // -------- Interstitial --------

    public void preloadInterstitial(Activity activity) {
        if (interstitialAd == null) {
            interstitialAd = new MaxInterstitialAd(Constants.MAX_INTERSTITIAL_AD_UNIT_ID, activity);
            interstitialAd.setListener(new MaxAdListener() {
                @Override public void onAdLoaded(MaxAd ad) { interstitialReady.set(true); }
                @Override public void onAdDisplayed(MaxAd ad) { interstitialReady.set(false); }
                @Override public void onAdHidden(MaxAd ad) { interstitialAd.loadAd(); }
                @Override public void onAdClicked(MaxAd ad) {}
                @Override public void onAdLoadFailed(String adUnitId, MaxError error) { interstitialReady.set(false); }
                @Override public void onAdDisplayFailed(MaxAd ad, MaxError error) { interstitialReady.set(false); }
            });
            interstitialAd.loadAd();
        }
    }

    public void showInterstitialIfReady(Activity activity, @Nullable Runnable onClosed) {
        if (interstitialAd != null && interstitialReady.get()) {
            interstitialAd.setListener(new MaxAdListener() {
                @Override public void onAdLoaded(MaxAd ad) { interstitialReady.set(true); }
                @Override public void onAdDisplayed(MaxAd ad) { interstitialReady.set(false); }
                @Override public void onAdHidden(MaxAd ad) {
                    if (onClosed != null) onClosed.run();
                    interstitialAd.loadAd();
                }
                @Override public void onAdClicked(MaxAd ad) {}
                @Override public void onAdLoadFailed(String adUnitId, MaxError error) {
                    if (onClosed != null) onClosed.run();
                }
                @Override public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    if (onClosed != null) onClosed.run();
                }
            });
            interstitialAd.showAd();
        } else {
            if (onClosed != null) onClosed.run();
        }
    }

    // -------- Rewarded --------

    public void preloadRewarded(Activity activity) {
        if (rewardedAd == null) {
            rewardedAd = MaxRewardedAd.getInstance(Constants.MAX_REWARDED_AD_UNIT_ID, activity);
            rewardedAd.setListener(new MaxRewardedAdListener() {
                @Override public void onAdLoaded(MaxAd ad) { rewardedReady.set(true); }
                @Override public void onAdDisplayed(MaxAd ad) { rewardedReady.set(false); }
                @Override public void onAdHidden(MaxAd ad) { rewardedAd.loadAd(); }
                @Override public void onAdClicked(MaxAd ad) {}
                @Override public void onAdLoadFailed(String adUnitId, MaxError error) { rewardedReady.set(false); }
                @Override public void onAdDisplayFailed(MaxAd ad, MaxError error) { rewardedReady.set(false); }
                @Override public void onUserRewarded(MaxAd ad, MaxReward reward) { /* no-op for preload */ }
                @Override public void onRewardedVideoCompleted(MaxAd ad) {}
                @Override public void onRewardedVideoStarted(MaxAd ad) {}
            });
            rewardedAd.loadAd();
        }
    }

    public void showRewarded(Activity activity, RewardPurpose purpose, RewardCallback callback) {
        if (rewardedAd != null && rewardedReady.get()) {
            rewardedAd.setListener(new MaxRewardedAdListener() {
                private boolean rewarded = false;

                @Override public void onAdLoaded(MaxAd ad) { rewardedReady.set(true); }
                @Override public void onAdDisplayed(MaxAd ad) { rewardedReady.set(false); }
                @Override public void onAdHidden(MaxAd ad) {
                    if (rewarded) callback.onReward(purpose);
                    else callback.onClosedNoReward();
                    rewardedAd.loadAd();
                }
                @Override public void onAdClicked(MaxAd ad) {}
                @Override public void onAdLoadFailed(String adUnitId, MaxError error) { callback.onClosedNoReward(); }
                @Override public void onAdDisplayFailed(MaxAd ad, MaxError error) { callback.onClosedNoReward(); }
                @Override public void onUserRewarded(MaxAd ad, MaxReward reward) { rewarded = true; }
                @Override public void onRewardedVideoCompleted(MaxAd ad) {}
                @Override public void onRewardedVideoStarted(MaxAd ad) {}
            });
            rewardedAd.showAd();
        } else {
            callback.onClosedNoReward();
        }
    }

    public interface RewardCallback {
        void onReward(RewardPurpose purpose);
        void onClosedNoReward();
    }
}