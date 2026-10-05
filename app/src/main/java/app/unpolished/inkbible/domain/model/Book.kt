package app.unpolished.inkbible.domain.model

data class Book(
    val id: String,
    val name: String,
    val testament: Testament,
    val chapterCount: Int
)
