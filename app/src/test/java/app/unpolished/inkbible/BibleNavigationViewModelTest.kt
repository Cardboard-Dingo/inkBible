package app.unpolished.inkbible

import app.unpolished.inkbible.data.local.BibleRepositoryImpl
import app.unpolished.inkbible.domain.model.Testament
import app.unpolished.inkbible.ui.navigation.BibleNavigationUiState
import app.unpolished.inkbible.ui.navigation.BibleNavigationViewModel
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
class BibleNavigationViewModelTest {

    private lateinit var repository: BibleRepositoryImpl
    private lateinit var fakeReadingPositionRepository: FakeReadingPositionRepository
    private lateinit var viewModel: BibleNavigationViewModel

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

        viewModel = BibleNavigationViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadBooks_populatesAll66Books() = runTest {
        val state = viewModel.uiState.value
        assertTrue("Expected Success state but was $state", state is BibleNavigationUiState.Success)
        val success = state as BibleNavigationUiState.Success
        assertEquals(66, success.books.size)
        assertEquals(66, success.filteredBooks.size)
    }

    @Test
    fun filterByTestament_filtersBooksCorrectly() = runTest {
        viewModel.filterByTestament(Testament.OT)

        var state = viewModel.uiState.value as BibleNavigationUiState.Success
        assertEquals(39, state.filteredBooks.size)
        assertTrue(state.filteredBooks.all { it.testament == Testament.OT })

        viewModel.filterByTestament(Testament.NT)

        state = viewModel.uiState.value as BibleNavigationUiState.Success
        assertEquals(27, state.filteredBooks.size)
        assertTrue(state.filteredBooks.all { it.testament == Testament.NT })
    }

    @Test
    fun updateSearchQuery_filtersBooksByName() = runTest {
        viewModel.updateSearchQuery("John")

        val state = viewModel.uiState.value as BibleNavigationUiState.Success
        assertTrue(state.filteredBooks.any { it.name == "John" })
        assertTrue(state.filteredBooks.any { it.name == "1 John" })
    }

    @Test
    fun selectBook_setsSelectedBookInState() = runTest {
        viewModel.selectBook("MATT")

        val state = viewModel.uiState.value as BibleNavigationUiState.Success
        val selected = state.selectedBook
        assertNotNull(selected)
        assertEquals("MATT", selected!!.id)
        assertEquals("Matthew", selected.name)
    }
}
