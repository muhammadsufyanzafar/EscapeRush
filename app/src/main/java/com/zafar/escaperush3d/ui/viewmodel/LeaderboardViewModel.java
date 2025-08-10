package com.zafar.escaperush3d.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zafar.escaperush3d.data.GameRepository;
import com.zafar.escaperush3d.data.model.ScoreEntry;

import java.util.ArrayList;

/**
 * ViewModel for local leaderboard screen.
 */
public class LeaderboardViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<ScoreEntry>> data = new MutableLiveData<>(GameRepository.getLeaderboard());
    public LiveData<ArrayList<ScoreEntry>> getLeaderboard() { return data; }
    public void refresh() { data.postValue(GameRepository.getLeaderboard()); }
}