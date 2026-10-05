package app.unpolished.inkbible.domain.model

data class ReadingPosition(
    val bookId: String,
    val chapterNumber: Int,
    val verseNumber: Int = 1
)
