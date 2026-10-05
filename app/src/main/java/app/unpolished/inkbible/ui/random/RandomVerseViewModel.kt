package app.unpolished.inkbible.ui.random

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.unpolished.inkbible.domain.model.Testament
import app.unpolished.inkbible.domain.model.Verse
import app.unpolished.inkbible.domain.repository.BibleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RandomVerseUiState {
    object Loading : RandomVerseUiState

    data class Success(
        val verse: Verse,
        val bookName: String,
        val testament: Testament
    ) : RandomVerseUiState

    data class Error(val message: String) : RandomVerseUiState
}

class RandomVerseViewModel(
    private val bibleRepository: BibleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RandomVerseUiState>(RandomVerseUiState.Loading)
    val uiState: StateFlow<RandomVerseUiState> = _uiState.asStateFlow()

    init {
        fetchRandomVerse()
    }

    fun fetchRandomVerse(testament: Testament? = null) {
        viewModelScope.launch {
            _uiState.value = RandomVerseUiState.Loading
            try {
                val verse = bibleRepository.getRandomVerse(testament)
                if (verse == null) {
                    _uiState.value = RandomVerseUiState.Error("Could not retrieve random verse")
                    return@launch
                }

                val book = bibleRepository.getBook(verse.bookId)
                val bookName = book?.name ?: verse.bookId
                val bookTestament = book?.testament ?: Testament.OT

                _uiState.value = RandomVerseUiState.Success(
                    verse = verse,
                    bookName = bookName,
                    testament = bookTestament
                )
            } catch (e: Exception) {
                _uiState.value = RandomVerseUiState.Error(e.message ?: "Failed to fetch random verse")
            }
        }
    }
}
