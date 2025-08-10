package com.zafar.escaperush3d;

import android.app.Application;

import com.applovin.sdk.AppLovinSdk;
import com.zafar.escaperush3d.util.AdsManager;
import com.zafar.escaperush3d.util.Prefs;

/**
 * Application class: initializes preferences and AppLovin MAX.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Prefs.init(this);

        // Initialize AppLovin MAX
        AppLovinSdk.getInstance(this).setMediationProvider("max");
        AppLovinSdk.initializeSdk(this, config -> {
            AdsManager.getInstance().init(this);
        });
        // Consent prompt is shown in SplashActivity to ensure we have an Activity context
    }
}