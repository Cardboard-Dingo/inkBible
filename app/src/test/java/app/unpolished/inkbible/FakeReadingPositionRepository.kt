package app.unpolished.inkbible

import app.unpolished.inkbible.domain.model.ReadingPosition
import app.unpolished.inkbible.domain.repository.ReadingPositionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeReadingPositionRepository(
    initialPosition: ReadingPosition = ReadingPosition("GEN", 1, 1)
) : ReadingPositionRepository {

    private val _position = MutableStateFlow(initialPosition)
    override val readingPosition: Flow<ReadingPosition> = _position.asStateFlow()

    override suspend fun saveReadingPosition(position: ReadingPosition) {
        _position.value = position
    }

    override suspend fun saveReadingPosition(bookId: String, chapterNumber: Int, verseNumber: Int) {
        _position.value = ReadingPosition(bookId, chapterNumber, verseNumber)
    }
}
