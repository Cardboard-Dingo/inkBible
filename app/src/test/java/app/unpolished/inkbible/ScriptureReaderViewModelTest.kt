package app.unpolished.inkbible

import app.unpolished.inkbible.data.local.BibleRepositoryImpl
import app.unpolished.inkbible.domain.model.ReadingPosition
import app.unpolished.inkbible.ui.reader.ScriptureReaderUiState
import app.unpolished.inkbible.ui.reader.ScriptureReaderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ScriptureReaderViewModelTest {

    private lateinit var repository: BibleRepositoryImpl
    private lateinit var fakeReadingPositionRepository: FakeReadingPositionRepository
    private lateinit var viewModel: ScriptureReaderViewModel

    @Before
    fun setUp() {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        fakeReadingPositionRepository = FakeReadingPositionRepository(ReadingPosition("GEN", 1, 1))

        val assetOpener: (String) -> java.io.InputStream = { path ->
            File("src/main/assets/$path").inputStream()
        }

        repository = BibleRepositoryImpl(
            context = null,
            readingPositionRepository = fakeReadingPositionRepository,
            ioDispatcher = testDispatcher,
            assetOpener = assetOpener
        )

        viewModel = ScriptureReaderViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsLastReadingPosition() = runTest {
        val state = viewModel.uiState.value
        assertTrue("Expected Success state but was $state", state is ScriptureReaderUiState.Success)
        val success = state as ScriptureReaderUiState.Success
        assertEquals("GEN", success.currentBook.id)
        assertEquals(1, success.currentChapter.chapterNumber)
    }

    @Test
    fun loadChapter_updatesStateAndReadingPosition() = runTest {
        viewModel.loadChapter("JOHN", 3, 16)

        val state = viewModel.uiState.value
        assertTrue("Expected Success state but was $state", state is ScriptureReaderUiState.Success)
        val success = state as ScriptureReaderUiState.Success
        assertEquals("JOHN", success.currentBook.id)
        assertEquals(3, success.currentChapter.chapterNumber)
        assertEquals(16, success.selectedVerseNumber)
    }

    @Test
    fun navigateToNextChapter_advancesCorrectly() = runTest {
        viewModel.loadChapter("GEN", 1, 1)
        viewModel.navigateToNextChapter()

        val state = viewModel.uiState.value as ScriptureReaderUiState.Success
        assertEquals("GEN", state.currentBook.id)
        assertEquals(2, state.currentChapter.chapterNumber)
    }

    @Test
    fun fetchRandomVerse_populatesRandomVerseInState() = runTest {
        viewModel.fetchRandomVerse()

        val state = viewModel.uiState.value as ScriptureReaderUiState.Success
        assertNotNull(state.randomVerse)
        assertTrue(state.randomVerse!!.text.isNotBlank())
    }
}
