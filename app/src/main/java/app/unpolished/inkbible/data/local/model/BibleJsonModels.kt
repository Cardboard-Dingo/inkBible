package app.unpolished.inkbible.data.local.model

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class BookJson(
    @field:Json(name = "id") @Json(name = "id") val id: String,
    @field:Json(name = "name") @Json(name = "name") val name: String,
    @field:Json(name = "testament") @Json(name = "testament") val testament: String,
    @field:Json(name = "chapterCount") @Json(name = "chapterCount") val chapterCount: Int
)

@Keep
@JsonClass(generateAdapter = true)
data class VerseJson(
    @field:Json(name = "verseNumber") @Json(name = "verseNumber") val verseNumber: Int,
    @field:Json(name = "text") @Json(name = "text") val text: String
)

@Keep
@JsonClass(generateAdapter = true)
data class ChapterJson(
    @field:Json(name = "chapterNumber") @Json(name = "chapterNumber") val chapterNumber: Int,
    @field:Json(name = "verses") @Json(name = "verses") val verses: List<VerseJson>
)

@Keep
@JsonClass(generateAdapter = true)
data class BookDataJson(
    @field:Json(name = "bookId") @Json(name = "bookId") val bookId: String,
    @field:Json(name = "chapters") @Json(name = "chapters") val chapters: List<ChapterJson>
)
