package com.zafar.escaperush3d.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Game HUD VM (if you later move canvas HUD to Android views).
 */
public class GameViewModel extends ViewModel {
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> coins = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> lives = new MutableLiveData<>(1);

    public LiveData<Integer> score() { return score; }
    public LiveData<Integer> coins() { return coins; }
    public LiveData<Integer> lives() { return lives; }

    public void setScore(int s) { score.postValue(s); }
    public void setCoins(int c) { coins.postValue(c); }
    public void setLives(int l) { lives.postValue(l); }
}