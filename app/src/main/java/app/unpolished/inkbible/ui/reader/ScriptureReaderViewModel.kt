package app.unpolished.inkbible.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.unpolished.inkbible.domain.model.ReadingPosition
import app.unpolished.inkbible.domain.model.Testament
import app.unpolished.inkbible.domain.repository.BibleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ScriptureReaderViewModel(
    private val bibleRepository: BibleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScriptureReaderUiState>(ScriptureReaderUiState.Loading)
    val uiState: StateFlow<ScriptureReaderUiState> = _uiState.asStateFlow()

    init {
        loadLastReadingPosition()
    }

    fun loadLastReadingPosition() {
        viewModelScope.launch {
            _uiState.value = ScriptureReaderUiState.Loading
            try {
                val position = bibleRepository.readingPosition.first()
                loadChapter(position.bookId, position.chapterNumber, position.verseNumber)
            } catch (e: Throwable) {
                _uiState.value = ScriptureReaderUiState.Error(e.message ?: "Failed to load reading position")
            }
        }
    }

    fun loadChapter(bookId: String, chapterNumber: Int, verseNumber: Int = 1) {
        viewModelScope.launch {
            _uiState.value = ScriptureReaderUiState.Loading
            try {
                val books = bibleRepository.getBooks()
                val bookIndex = books.indexOfFirst { it.id.equals(bookId, ignoreCase = true) }
                if (bookIndex == -1) {
                    _uiState.value = ScriptureReaderUiState.Error("Book not found: $bookId")
                    return@launch
                }

                val book = books[bookIndex]
                val validChapterNumber = chapterNumber.coerceIn(1, book.chapterCount)
                val chapter = bibleRepository.getChapter(book.id, validChapterNumber)

                if (chapter == null) {
                    _uiState.value = ScriptureReaderUiState.Error("Chapter not found: ${book.name} $validChapterNumber")
                    return@launch
                }

                val validVerseNumber = if (chapter.verses.any { it.verseNumber == verseNumber }) verseNumber else 1
                val newPosition = ReadingPosition(book.id, validChapterNumber, validVerseNumber)

                // Save reading position in DataStore
                try {
                    bibleRepository.saveReadingPosition(newPosition)
                } catch (_: Throwable) {
                    // Ignore error saving preference so app continues
                }

                val canGoPrev = (validChapterNumber > 1) || (bookIndex > 0)
                val canGoNext = (validChapterNumber < book.chapterCount) || (bookIndex < books.size - 1)

                val existingRandomVerse = (_uiState.value as? ScriptureReaderUiState.Success)?.randomVerse

                _uiState.value = ScriptureReaderUiState.Success(
                    currentBook = book,
                    currentChapter = chapter,
                    selectedVerseNumber = validVerseNumber,
                    randomVerse = existingRandomVerse,
                    canGoToPreviousChapter = canGoPrev,
                    canGoToNextChapter = canGoNext,
                    readingPosition = newPosition
                )
            } catch (e: Throwable) {
                _uiState.value = ScriptureReaderUiState.Error(e.message ?: "Failed to load chapter")
            }
        }
    }

    fun navigateToNextChapter() {
        val currentState = _uiState.value as? ScriptureReaderUiState.Success ?: return
        viewModelScope.launch {
            val books = bibleRepository.getBooks()
            val bookIndex = books.indexOfFirst { it.id.equals(currentState.currentBook.id, ignoreCase = true) }
            if (bookIndex == -1) return@launch

            val currentBook = books[bookIndex]
            if (currentState.currentChapter.chapterNumber < currentBook.chapterCount) {
                loadChapter(currentBook.id, currentState.currentChapter.chapterNumber + 1, 1)
            } else if (bookIndex < books.size - 1) {
                val nextBook = books[bookIndex + 1]
                loadChapter(nextBook.id, 1, 1)
            }
        }
    }

    fun navigateToPreviousChapter() {
        val currentState = _uiState.value as? ScriptureReaderUiState.Success ?: return
        viewModelScope.launch {
            val books = bibleRepository.getBooks()
            val bookIndex = books.indexOfFirst { it.id.equals(currentState.currentBook.id, ignoreCase = true) }
            if (bookIndex == -1) return@launch

            if (currentState.currentChapter.chapterNumber > 1) {
                loadChapter(currentState.currentBook.id, currentState.currentChapter.chapterNumber - 1, 1)
            } else if (bookIndex > 0) {
                val prevBook = books[bookIndex - 1]
                loadChapter(prevBook.id, prevBook.chapterCount, 1)
            }
        }
    }

    fun selectVerse(verseNumber: Int) {
        val currentState = _uiState.value as? ScriptureReaderUiState.Success ?: return
        viewModelScope.launch {
            val newPosition = ReadingPosition(
                bookId = currentState.currentBook.id,
                chapterNumber = currentState.currentChapter.chapterNumber,
                verseNumber = verseNumber
            )
            bibleRepository.saveReadingPosition(newPosition)
            _uiState.value = currentState.copy(
                selectedVerseNumber = verseNumber,
                readingPosition = newPosition
            )
        }
    }

    fun fetchRandomVerse(testament: Testament? = null) {
        viewModelScope.launch {
            val verse = bibleRepository.getRandomVerse(testament)
            val currentState = _uiState.value
            if (currentState is ScriptureReaderUiState.Success && verse != null) {
                _uiState.value = currentState.copy(randomVerse = verse)
            }
        }
    }

    fun clearRandomVerse() {
        val currentState = _uiState.value as? ScriptureReaderUiState.Success ?: return
        _uiState.value = currentState.copy(randomVerse = null)
    }
}
