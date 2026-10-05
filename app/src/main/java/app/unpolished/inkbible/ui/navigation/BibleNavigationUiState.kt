package app.unpolished.inkbible.ui.navigation

import app.unpolished.inkbible.domain.model.Book
import app.unpolished.inkbible.domain.model.Testament
import app.unpolished.inkbible.domain.model.Verse

sealed interface BibleNavigationUiState {
    object Loading : BibleNavigationUiState

    data class Success(
        val books: List<Book>,
        val filteredBooks: List<Book>,
        val selectedTestamentFilter: Testament? = null,
        val selectedBook: Book? = null,
        val selectedChapterNumber: Int? = null,
        val chapterVerses: List<Verse> = emptyList(),
        val searchQuery: String = ""
    ) : BibleNavigationUiState

    data class Error(val message: String) : BibleNavigationUiState
}

