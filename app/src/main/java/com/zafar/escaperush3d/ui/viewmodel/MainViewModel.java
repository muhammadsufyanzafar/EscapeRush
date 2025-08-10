package com.zafar.escaperush3d.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zafar.escaperush3d.util.Prefs;

/**
 * ViewModel for main menu stats (coins).
 */
public class MainViewModel extends ViewModel {
    private final MutableLiveData<Integer> totalCoins = new MutableLiveData<>(Prefs.getTotalCoins());
    public LiveData<Integer> getTotalCoins() { return totalCoins; }
    public void refreshCoins() { totalCoins.postValue(Prefs.getTotalCoins()); }
}