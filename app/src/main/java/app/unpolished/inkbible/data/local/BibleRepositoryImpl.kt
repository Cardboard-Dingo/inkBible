package app.unpolished.inkbible.data.local

import android.content.Context
import app.unpolished.inkbible.data.local.model.BookDataJson
import app.unpolished.inkbible.data.local.model.BookJson
import app.unpolished.inkbible.domain.model.Book
import app.unpolished.inkbible.domain.model.Chapter
import app.unpolished.inkbible.domain.model.ReadingPosition
import app.unpolished.inkbible.domain.model.Testament
import app.unpolished.inkbible.domain.model.Verse
import app.unpolished.inkbible.domain.repository.BibleRepository
import app.unpolished.inkbible.domain.repository.ReadingPositionRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class BibleRepositoryImpl(
    private val context: Context? = null,
    private val readingPositionRepository: ReadingPositionRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val random: Random = Random.Default,
    private val assetOpener: (String) -> InputStream = { path ->
        requireNotNull(context) { "Context must not be null when default assetOpener is used" }.assets.open(path)
    }
) : BibleRepository {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val bookListType = Types.newParameterizedType(List::class.java, BookJson::class.java)
    private val bookListAdapter = moshi.adapter<List<BookJson>>(bookListType)
    private val bookDataAdapter = moshi.adapter(BookDataJson::class.java)

    private var cachedBooks: List<Book>? = null
    private val booksMutex = Mutex()
    private val loadedBookContent = ConcurrentHashMap<String, BookDataJson>()

    override val readingPosition: Flow<ReadingPosition> = readingPositionRepository.readingPosition

    override suspend fun saveReadingPosition(position: ReadingPosition) {
        readingPositionRepository.saveReadingPosition(position)
    }

    override suspend fun saveReadingPosition(bookId: String, chapterNumber: Int, verseNumber: Int) {
        readingPositionRepository.saveReadingPosition(bookId, chapterNumber, verseNumber)
    }

    override suspend fun getBooks(): List<Book> = withContext(ioDispatcher) {
        cachedBooks?.let { return@withContext it }
        booksMutex.withLock {
            cachedBooks?.let { return@withLock it }
            val books = loadBooksFromAssets()
            cachedBooks = books
            books
        }
    }

    override suspend fun getBooksByTestament(testament: Testament): List<Book> {
        return getBooks().filter { it.testament == testament }
    }

    override suspend fun getBook(bookId: String): Book? {
        return getBooks().firstOrNull { it.id.equals(bookId, ignoreCase = true) }
    }

    override suspend fun getChapter(bookId: String, chapterNumber: Int): Chapter? = withContext(ioDispatcher) {
        val bookData = getOrLoadBookData(bookId) ?: return@withContext null
        val chapterJson = bookData.chapters.firstOrNull { it.chapterNumber == chapterNumber }
            ?: return@withContext null

        val verses = chapterJson.verses.map { verseJson ->
            Verse(
                bookId = bookData.bookId,
                chapterNumber = chapterJson.chapterNumber,
                verseNumber = verseJson.verseNumber,
                text = verseJson.text
            )
        }

        Chapter(
            bookId = bookData.bookId,
            chapterNumber = chapterJson.chapterNumber,
            verses = verses
        )
    }

    override suspend fun getVerse(
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int
    ): Verse? {
        val chapter = getChapter(bookId, chapterNumber) ?: return null
        return chapter.verses.firstOrNull { it.verseNumber == verseNumber }
    }

    override suspend fun getRandomVerse(testament: Testament?): Verse? = withContext(ioDispatcher) {
        val availableBooks = if (testament != null) {
            getBooksByTestament(testament)
        } else {
            getBooks()
        }

        if (availableBooks.isEmpty()) return@withContext null

        val randomBook = availableBooks[random.nextInt(availableBooks.size)]
        val randomChapterNumber = random.nextInt(randomBook.chapterCount) + 1
        val chapter = getChapter(randomBook.id, randomChapterNumber) ?: return@withContext null

        if (chapter.verses.isEmpty()) return@withContext null

        chapter.verses[random.nextInt(chapter.verses.size)]
    }

    override suspend fun searchVerses(query: String, limit: Int): List<Verse> = withContext(ioDispatcher) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return@withContext emptyList()

        val results = mutableListOf<Verse>()
        val books = getBooks()

        for (book in books) {
            val bookData = getOrLoadBookData(book.id) ?: continue
            for (chapterJson in bookData.chapters) {
                for (verseJson in chapterJson.verses) {
                    if (verseJson.text.contains(trimmedQuery, ignoreCase = true)) {
                        results.add(
                            Verse(
                                bookId = book.id,
                                chapterNumber = chapterJson.chapterNumber,
                                verseNumber = verseJson.verseNumber,
                                text = verseJson.text
                            )
                        )
                        if (results.size >= limit) {
                            return@withContext results
                        }
                    }
                }
            }
        }

        results
    }

    private fun loadBooksFromAssets(): List<Book> {
        val jsonString = assetOpener("bible/books.json").use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        }.removePrefix("\uFEFF")
        val bookJsons = bookListAdapter.fromJson(jsonString) ?: emptyList()
        return bookJsons.map { json ->
            Book(
                id = json.id,
                name = json.name,
                testament = Testament.valueOf(json.testament),
                chapterCount = json.chapterCount
            )
        }
    }

    private fun getOrLoadBookData(bookId: String): BookDataJson? {
        val uppercaseId = bookId.uppercase()
        loadedBookContent[uppercaseId]?.let { return it }

        return try {
            val jsonString = assetOpener("bible/books/$uppercaseId.json").use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }.removePrefix("\uFEFF")
            val bookData = bookDataAdapter.fromJson(jsonString)
            if (bookData != null) {
                loadedBookContent[uppercaseId] = bookData
            }
            bookData
        } catch (_: Exception) {
            null
        }
    }
}
