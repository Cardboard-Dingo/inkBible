package app.unpolished.inkbible.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data class ReaderRoute(
    val bookId: String? = null,
    val chapterNumber: Int? = null,
    val verseNumber: Int? = null
)

@Serializable
data class NavigationRoute(
    val initialBookId: String? = null
)

@Serializable
data object RandomVerseRoute
