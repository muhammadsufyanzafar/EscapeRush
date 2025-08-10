package com.zafar.escaperush3d.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.zafar.escaperush3d.util.Prefs;

/**
 * Settings screen ViewModel.
 */
public class SettingsViewModel extends ViewModel {
    public String getControlScheme() { return Prefs.getControlScheme(); }
    public void setControlScheme(String s) { Prefs.setControlScheme(s); }

    public boolean isSoundEnabled() { return Prefs.isSoundEnabled(); }
    public void setSoundEnabled(boolean e) { Prefs.setSoundEnabled(e); }

    public int getSelectedArea() { return Prefs.getSelectedArea(); }
    public void setSelectedArea(int idx) { Prefs.setSelectedArea(idx); }
}