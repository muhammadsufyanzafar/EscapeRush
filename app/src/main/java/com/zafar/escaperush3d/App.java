package com.zafar.escaperush3d;

import android.app.Application;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.zafar.escaperush3d.util.AdsManager;
import com.zafar.escaperush3d.util.Constants;
import com.zafar.escaperush3d.util.Prefs;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Prefs.init(this);

        boolean testMode = true; // true for testing, false for release
        UnityAds.setDebugMode(true);

        // Initialize Unity Ads
        UnityAds.initialize(
                this,
                Constants.UNITY_GAME_ID,
                testMode,
                new IUnityAdsInitializationListener() {
                    @Override
                    public void onInitializationComplete() {
                        // Unity Ads ready
                    }

                    @Override
                    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                        // Initialization failed
                    }
                }
        );

        // Initialize AdsManager
        AdsManager.getInstance().init(this);
    }
}
