package app.unpolished.inkbible.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.unpolished.inkbible.domain.model.Book
import app.unpolished.inkbible.domain.model.Testament
import app.unpolished.inkbible.domain.repository.BibleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BibleNavigationViewModel(
    private val bibleRepository: BibleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BibleNavigationUiState>(BibleNavigationUiState.Loading)
    val uiState: StateFlow<BibleNavigationUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = BibleNavigationUiState.Loading
            try {
                val books = bibleRepository.getBooks()
                val currentState = _uiState.value as? BibleNavigationUiState.Success
                val filter = currentState?.selectedTestamentFilter
                val query = currentState?.searchQuery.orEmpty()
                val selectedBook = currentState?.selectedBook

                val filtered = applyFilter(books, filter, query)

                _uiState.value = BibleNavigationUiState.Success(
                    books = books,
                    filteredBooks = filtered,
                    selectedTestamentFilter = filter,
                    selectedBook = selectedBook,
                    searchQuery = query
                )
            } catch (e: Throwable) {
                _uiState.value = BibleNavigationUiState.Error(e.message ?: "Failed to load Bible books")
            }
        }
    }

    fun filterByTestament(testament: Testament?) {
        val currentState = _uiState.value as? BibleNavigationUiState.Success ?: return
        val filtered = applyFilter(currentState.books, testament, currentState.searchQuery)
        _uiState.value = currentState.copy(
            selectedTestamentFilter = testament,
            filteredBooks = filtered
        )
    }

    fun updateSearchQuery(query: String) {
        val currentState = _uiState.value as? BibleNavigationUiState.Success ?: return
        val filtered = applyFilter(currentState.books, currentState.selectedTestamentFilter, query)
        _uiState.value = currentState.copy(
            searchQuery = query,
            filteredBooks = filtered
        )
    }

    fun selectBook(bookId: String?) {
        val currentState = _uiState.value as? BibleNavigationUiState.Success ?: return
        val book = if (bookId != null) {
            currentState.books.firstOrNull { it.id.equals(bookId, ignoreCase = true) }
        } else {
            null
        }
        _uiState.value = currentState.copy(
            selectedBook = book,
            selectedChapterNumber = null,
            chapterVerses = emptyList()
        )
    }

    fun selectChapter(chapterNumber: Int) {
        val currentState = _uiState.value as? BibleNavigationUiState.Success ?: return
        val book = currentState.selectedBook ?: return
        viewModelScope.launch {
            try {
                val chapter = bibleRepository.getChapter(book.id, chapterNumber)
                _uiState.value = currentState.copy(
                    selectedChapterNumber = chapterNumber,
                    chapterVerses = chapter?.verses ?: emptyList()
                )
            } catch (_: Exception) {
                _uiState.value = currentState.copy(
                    selectedChapterNumber = chapterNumber,
                    chapterVerses = emptyList()
                )
            }
        }
    }

    fun clearSelectedChapter() {
        val currentState = _uiState.value as? BibleNavigationUiState.Success ?: return
        _uiState.value = currentState.copy(
            selectedChapterNumber = null,
            chapterVerses = emptyList()
        )
    }

    fun clearSelectedBook() {
        val currentState = _uiState.value as? BibleNavigationUiState.Success ?: return
        _uiState.value = currentState.copy(
            selectedBook = null,
            selectedChapterNumber = null,
            chapterVerses = emptyList()
        )
    }

    private fun applyFilter(books: List<Book>, testament: Testament?, query: String): List<Book> {
        val trimmedQuery = query.trim()
        return books.filter { book ->
            val matchesTestament = testament == null || book.testament == testament
            val matchesQuery = trimmedQuery.isEmpty() ||
                    book.name.contains(trimmedQuery, ignoreCase = true) ||
                    book.id.contains(trimmedQuery, ignoreCase = true)
            matchesTestament && matchesQuery
        }
    }
}
