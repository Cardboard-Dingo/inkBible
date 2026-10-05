package app.unpolished.inkbible.domain.repository

import app.unpolished.inkbible.domain.model.ReadingPosition
import kotlinx.coroutines.flow.Flow

interface ReadingPositionRepository {
    val readingPosition: Flow<ReadingPosition>
    suspend fun saveReadingPosition(position: ReadingPosition)
    suspend fun saveReadingPosition(bookId: String, chapterNumber: Int, verseNumber: Int = 1)
}
