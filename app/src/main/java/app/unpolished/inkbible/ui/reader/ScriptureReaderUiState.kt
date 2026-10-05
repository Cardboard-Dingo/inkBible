package app.unpolished.inkbible.ui.reader

import app.unpolished.inkbible.domain.model.Book
import app.unpolished.inkbible.domain.model.Chapter
import app.unpolished.inkbible.domain.model.ReadingPosition
import app.unpolished.inkbible.domain.model.Verse

sealed interface ScriptureReaderUiState {
    object Loading : ScriptureReaderUiState

    data class Success(
        val currentBook: Book,
        val currentChapter: Chapter,
        val selectedVerseNumber: Int? = null,
        val randomVerse: Verse? = null,
        val canGoToPreviousChapter: Boolean,
        val canGoToNextChapter: Boolean,
        val readingPosition: ReadingPosition
    ) : ScriptureReaderUiState

    data class Error(val message: String) : ScriptureReaderUiState
}
