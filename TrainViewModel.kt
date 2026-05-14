package com.example.nammaplatform

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TrainUiState {
    object Loading : TrainUiState()
    data class Success(val trains: List<Train>) : TrainUiState()
    data class Error(val message: String) : TrainUiState()
}

class TrainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<TrainUiState>(TrainUiState.Loading)
    val uiState: StateFlow<TrainUiState> = _uiState

    fun loadTrains(context: Context) {
        viewModelScope.launch {
            _uiState.value = TrainUiState.Loading
            
            // Try Firebase first
            val firestoreTrains = fetchTrainsFromFirestore()
            
            if (firestoreTrains.isNotEmpty()) {
                _uiState.value = TrainUiState.Success(firestoreTrains)
            } else {
                // Fallback to local JSON
                val localTrains = loadTrainsFromAssets(context)
                if (localTrains.isNotEmpty()) {
                    _uiState.value = TrainUiState.Success(localTrains)
                } else {
                    _uiState.value = TrainUiState.Error("Failed to load train data")
                }
            }
        }
    }
}