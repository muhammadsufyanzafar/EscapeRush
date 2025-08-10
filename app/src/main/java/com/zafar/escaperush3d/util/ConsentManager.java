package com.zafar.escaperush3d.util;

import android.app.Activity;

import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

/**
 * Handles user consent (GDPR/UMP).
 */
public class ConsentManager {
    public static void requestConsent(Activity activity, Runnable onReady) {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .build();
        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(activity);

        consentInformation.requestConsentInfoUpdate(
                activity, params,
                () -> {
                    if (consentInformation.isConsentFormAvailable()) {
                        UserMessagingPlatform.loadConsentForm(activity,
                                consentForm -> showIfRequired(activity, consentInformation, consentForm, onReady),
                                formError -> { if (onReady != null) onReady.run(); });
                    } else {
                        if (onReady != null) onReady.run();
                    }
                },
                requestConsentError -> { if (onReady != null) onReady.run(); }
        );
    }

    private static void showIfRequired(Activity activity, ConsentInformation ci, ConsentForm form, Runnable onReady) {
        if (ci.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
            form.show(activity, formError -> { if (onReady != null) onReady.run(); });
        } else {
            if (onReady != null) onReady.run();
        }
    }
}