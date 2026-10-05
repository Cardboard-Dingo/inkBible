package app.unpolished.inkbible

import app.unpolished.inkbible.data.local.BibleRepositoryImpl
import app.unpolished.inkbible.domain.model.ReadingPosition
import app.unpolished.inkbible.domain.model.Testament
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class BibleRepositoryTest {

    private lateinit var repository: BibleRepositoryImpl
    private lateinit var fakeReadingPositionRepository: FakeReadingPositionRepository

    @Before
    fun setUp() {
        fakeReadingPositionRepository = FakeReadingPositionRepository()
    }

    private fun createRepository(dispatcher: kotlinx.coroutines.CoroutineDispatcher): BibleRepositoryImpl {
        val assetOpener: (String) -> java.io.InputStream = { path ->
            File("src/main/assets/$path").inputStream()
        }

        return BibleRepositoryImpl(
            context = null,
            readingPositionRepository = fakeReadingPositionRepository,
            ioDispatcher = dispatcher,
            assetOpener = assetOpener
        )
    }

    @Test
    fun getBooks_returnsAll66Books() = runTest {
        repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val books = repository.getBooks()
        assertEquals(66, books.size)
        assertEquals("GEN", books.first().id)
        assertEquals("Genesis", books.first().name)
        assertEquals(Testament.OT, books.first().testament)
        assertEquals(50, books.first().chapterCount)

        assertEquals("REV", books.last().id)
        assertEquals("Revelation", books.last().name)
        assertEquals(Testament.NT, books.last().testament)
        assertEquals(22, books.last().chapterCount)
    }

    @Test
    fun getBooksByTestament_returnsCorrectCounts() = runTest {
        repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val otBooks = repository.getBooksByTestament(Testament.OT)
        assertEquals(39, otBooks.size)
        assertTrue(otBooks.all { it.testament == Testament.OT })

        val ntBooks = repository.getBooksByTestament(Testament.NT)
        assertEquals(27, ntBooks.size)
        assertTrue(ntBooks.all { it.testament == Testament.NT })
    }

    @Test
    fun getChapter_returnsGenesisChapter1AndJohnChapter3() = runTest {
        repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val gen1 = repository.getChapter("GEN", 1)
        assertNotNull(gen1)
        assertEquals("GEN", gen1!!.bookId)
        assertEquals(1, gen1.chapterNumber)
        assertTrue(gen1.verses.isNotEmpty())
        assertEquals(1, gen1.verses.first().verseNumber)
        assertTrue(gen1.verses.first().text.contains("In the beginning"))

        val john3 = repository.getChapter("JOHN", 3)
        assertNotNull(john3)
        assertEquals("JOHN", john3!!.bookId)
        assertEquals(3, john3.chapterNumber)
        val john316 = john3.verses.firstOrNull { it.verseNumber == 16 }
        assertNotNull(john316)
        assertTrue(john316!!.text.contains("For God so loved"))
    }

    @Test
    fun getRandomVerse_returnsValidVerses() = runTest {
        repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val randomVerse = repository.getRandomVerse()
        assertNotNull(randomVerse)
        assertTrue(randomVerse!!.text.isNotBlank())
        assertTrue(randomVerse.chapterNumber > 0)
        assertTrue(randomVerse.verseNumber > 0)

        // OT random verse
        val otVerse = repository.getRandomVerse(Testament.OT)
        assertNotNull(otVerse)
        val otBook = repository.getBook(otVerse!!.bookId)
        assertEquals(Testament.OT, otBook?.testament)

        // NT random verse
        val ntVerse = repository.getRandomVerse(Testament.NT)
        assertNotNull(ntVerse)
        val ntBook = repository.getBook(ntVerse!!.bookId)
        assertEquals(Testament.NT, ntBook?.testament)
    }

    @Test
    fun readingPosition_savesAndRestores() = runTest {
        repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val newPosition = ReadingPosition("MATT", 5, 3)
        repository.saveReadingPosition(newPosition)

        val restoredPosition = repository.readingPosition.first()
        assertEquals("MATT", restoredPosition.bookId)
        assertEquals(5, restoredPosition.chapterNumber)
        assertEquals(3, restoredPosition.verseNumber)
    }
}
