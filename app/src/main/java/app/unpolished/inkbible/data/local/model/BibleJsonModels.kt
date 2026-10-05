package app.unpolished.inkbible.data.local.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookJson(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "testament") val testament: String,
    @Json(name = "chapterCount") val chapterCount: Int
)

@JsonClass(generateAdapter = true)
data class VerseJson(
    @Json(name = "verseNumber") val verseNumber: Int,
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class ChapterJson(
    @Json(name = "chapterNumber") val chapterNumber: Int,
    @Json(name = "verses") val verses: List<VerseJson>
)

@JsonClass(generateAdapter = true)
data class BookDataJson(
    @Json(name = "bookId") val bookId: String,
    @Json(name = "chapters") val chapters: List<ChapterJson>
)
