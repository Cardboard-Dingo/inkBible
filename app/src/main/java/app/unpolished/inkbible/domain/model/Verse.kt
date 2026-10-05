package app.unpolished.inkbible.domain.model

data class Verse(
    val bookId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val text: String
)
