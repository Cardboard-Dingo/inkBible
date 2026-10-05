package app.unpolished.inkbible.domain.model

data class Chapter(
    val bookId: String,
    val chapterNumber: Int,
    val verses: List<Verse>
)
