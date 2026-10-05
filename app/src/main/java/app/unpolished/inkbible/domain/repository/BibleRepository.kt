package app.unpolished.inkbible.domain.repository

import app.unpolished.inkbible.domain.model.Book
import app.unpolished.inkbible.domain.model.Chapter
import app.unpolished.inkbible.domain.model.ReadingPosition
import app.unpolished.inkbible.domain.model.Testament
import app.unpolished.inkbible.domain.model.Verse
import kotlinx.coroutines.flow.Flow

interface BibleRepository {
    suspend fun getBooks(): List<Book>
    suspend fun getBooksByTestament(testament: Testament): List<Book>
    suspend fun getBook(bookId: String): Book?
    suspend fun getChapter(bookId: String, chapterNumber: Int): Chapter?
    suspend fun getVerse(bookId: String, chapterNumber: Int, verseNumber: Int): Verse?
    suspend fun getRandomVerse(testament: Testament? = null): Verse?
    suspend fun searchVerses(query: String, limit: Int = 50): List<Verse>

    val readingPosition: Flow<ReadingPosition>
    suspend fun saveReadingPosition(position: ReadingPosition)
    suspend fun saveReadingPosition(bookId: String, chapterNumber: Int, verseNumber: Int = 1)
}
