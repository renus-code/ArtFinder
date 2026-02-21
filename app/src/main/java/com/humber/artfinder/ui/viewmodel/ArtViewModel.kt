package com.humber.artfinder.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.humber.artfinder.data.model.Artwork
import com.humber.artfinder.data.repository.ArtRepository
import kotlinx.coroutines.launch

sealed class ArtState {
    object Loading : ArtState()
    data class Success(val artworks: List<Artwork>) : ArtState()
    data class Error(val message: String) : ArtState()
}

class ArtViewModel(private val repository: ArtRepository) : ViewModel() {

    private val _artState = mutableStateOf<ArtState>(ArtState.Loading)
    val artState: State<ArtState> = _artState

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _currentPage = mutableIntStateOf(1)
    val currentPage: State<Int> = _currentPage

    init {
        fetchArtworks()
    }

    fun fetchArtworks() {
        _artState.value = ArtState.Loading
        viewModelScope.launch {
            val result = if (_searchQuery.value.isEmpty()) {
                repository.getArtworks(_currentPage.intValue)
            } else {
                repository.searchArtworks(_searchQuery.value, _currentPage.intValue)
            }
            
            result.fold(
                onSuccess = { artworks ->
                    _artState.value = ArtState.Success(artworks)
                },
                onFailure = { e ->
                    _artState.value = ArtState.Error(e.message ?: "Failed to fetch artworks")
                }
            )
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        _currentPage.intValue = 1 // Reset pagination for new search
        fetchArtworks()
    }

    fun loadNextPage() {
        _currentPage.intValue++
        fetchArtworks()
    }

    fun loadPreviousPage() {
        if (_currentPage.intValue > 1) {
            _currentPage.intValue--
            fetchArtworks()
        }
    }
}

class ArtVMFactory(private val repository: ArtRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArtViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
