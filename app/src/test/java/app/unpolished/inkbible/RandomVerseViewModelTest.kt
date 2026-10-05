package app.unpolished.inkbible

import app.unpolished.inkbible.data.local.BibleRepositoryImpl
import app.unpolished.inkbible.domain.model.Testament
import app.unpolished.inkbible.ui.random.RandomVerseUiState
import app.unpolished.inkbible.ui.random.RandomVerseViewModel
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
class RandomVerseViewModelTest {

    private lateinit var repository: BibleRepositoryImpl
    private lateinit var fakeReadingPositionRepository: FakeReadingPositionRepository
    private lateinit var viewModel: RandomVerseViewModel

    @Before
    fun setUp() {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        fakeReadingPositionRepository = FakeReadingPositionRepository()

        val assetOpener: (String) -> java.io.InputStream = { path ->
            File("src/main/assets/$path").inputStream()
        }

        repository = BibleRepositoryImpl(
            context = null,
            readingPositionRepository = fakeReadingPositionRepository,
            ioDispatcher = testDispatcher,
            assetOpener = assetOpener
        )

        viewModel = RandomVerseViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_fetchesRandomVerseSuccessfully() = runTest {
        val state = viewModel.uiState.value
        assertTrue("Expected Success state but was $state", state is RandomVerseUiState.Success)
        val success = state as RandomVerseUiState.Success
        assertNotNull(success.verse)
        assertTrue(success.verse.text.isNotBlank())
        assertTrue(success.bookName.isNotBlank())
    }

    @Test
    fun fetchRandomVerse_withTestamentFilter_returnsVerseFromCorrectTestament() = runTest {
        viewModel.fetchRandomVerse(Testament.NT)

        val state = viewModel.uiState.value
        assertTrue("Expected Success state but was $state", state is RandomVerseUiState.Success)
        val success = state as RandomVerseUiState.Success
        assertEquals(Testament.NT, success.testament)
    }
}
