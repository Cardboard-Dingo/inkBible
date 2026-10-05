package app.unpolished.inkbible.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.unpolished.inkbible.domain.model.ReadingPosition
import app.unpolished.inkbible.domain.repository.ReadingPositionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.readingPositionDataStore: DataStore<Preferences> by preferencesDataStore(name = "reading_position_prefs")

class ReadingPositionRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : ReadingPositionRepository {

    private object Keys {
        val BOOK_ID = stringPreferencesKey("last_book_id")
        val CHAPTER_NUMBER = intPreferencesKey("last_chapter_number")
        val VERSE_NUMBER = intPreferencesKey("last_verse_number")
    }

    override val readingPosition: Flow<ReadingPosition> = dataStore.data.map { preferences ->
        val bookId = preferences[Keys.BOOK_ID] ?: DEFAULT_BOOK_ID
        val chapterNumber = preferences[Keys.CHAPTER_NUMBER] ?: DEFAULT_CHAPTER_NUMBER
        val verseNumber = preferences[Keys.VERSE_NUMBER] ?: DEFAULT_VERSE_NUMBER
        ReadingPosition(
            bookId = bookId,
            chapterNumber = chapterNumber,
            verseNumber = verseNumber
        )
    }

    override suspend fun saveReadingPosition(position: ReadingPosition) {
        saveReadingPosition(position.bookId, position.chapterNumber, position.verseNumber)
    }

    override suspend fun saveReadingPosition(bookId: String, chapterNumber: Int, verseNumber: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.BOOK_ID] = bookId
            preferences[Keys.CHAPTER_NUMBER] = chapterNumber
            preferences[Keys.VERSE_NUMBER] = verseNumber
        }
    }

    companion object {
        const val DEFAULT_BOOK_ID = "GEN"
        const val DEFAULT_CHAPTER_NUMBER = 1
        const val DEFAULT_VERSE_NUMBER = 1
    }
}
