package app.unpolished.inkbible

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import app.unpolished.inkbible.data.AppContainer
import app.unpolished.inkbible.ui.InkBibleViewModelFactory
import app.unpolished.inkbible.ui.navigation.BibleNavigationScreen
import app.unpolished.inkbible.ui.navigation.BibleNavigationViewModel
import app.unpolished.inkbible.ui.navigation.NavigationRoute
import app.unpolished.inkbible.ui.navigation.RandomVerseRoute
import app.unpolished.inkbible.ui.navigation.ReaderRoute
import app.unpolished.inkbible.ui.random.RandomVerseScreen
import app.unpolished.inkbible.ui.random.RandomVerseViewModel
import app.unpolished.inkbible.ui.reader.ScriptureReaderScreen
import app.unpolished.inkbible.ui.reader.ScriptureReaderViewModel
import app.unpolished.inkbible.ui.theme.EInkWhite
import app.unpolished.inkbible.ui.theme.InkBibleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as? InkBibleApplication)?.appContainer
            ?: AppContainer(applicationContext)
        val viewModelFactory = InkBibleViewModelFactory(appContainer.bibleRepository)

        setContent {
            InkBibleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = EInkWhite
                ) {
                    InkBibleApp(viewModelFactory = viewModelFactory)
                }
            }
        }
    }
}

@Composable
fun InkBibleApp(
    viewModelFactory: InkBibleViewModelFactory
) {
    val backStack = remember { mutableStateListOf<Any>(ReaderRoute()) }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.size - 1)
            }
        }
    ) { key ->
        NavEntry(key = key) {
            when (key) {
                is ReaderRoute -> {
                    val readerViewModel: ScriptureReaderViewModel = viewModel(factory = viewModelFactory)

                    LaunchedEffect(key.bookId, key.chapterNumber, key.verseNumber) {
                        if (key.bookId != null && key.chapterNumber != null) {
                            readerViewModel.loadChapter(
                                bookId = key.bookId,
                                chapterNumber = key.chapterNumber,
                                verseNumber = key.verseNumber ?: 1
                            )
                        }
                    }

                    ScriptureReaderScreen(
                        viewModel = readerViewModel,
                        onNavigateToBooks = {
                            val currentBookId = (readerViewModel.uiState.value as? app.unpolished.inkbible.ui.reader.ScriptureReaderUiState.Success)?.currentBook?.id
                            backStack.add(NavigationRoute(initialBookId = currentBookId))
                        },
                        onNavigateToRandomVerse = {
                            backStack.add(RandomVerseRoute)
                        }
                    )
                }

                is NavigationRoute -> {
                    val navViewModel: BibleNavigationViewModel = viewModel(factory = viewModelFactory)

                    LaunchedEffect(key.initialBookId) {
                        if (key.initialBookId != null) {
                            navViewModel.selectBook(key.initialBookId)
                        } else {
                            navViewModel.clearSelectedBook()
                        }
                    }

                    BibleNavigationScreen(
                        viewModel = navViewModel,
                        onNavigateToReader = { bookId, chapterNumber, verseNumber ->
                            backStack.add(
                                ReaderRoute(
                                    bookId = bookId,
                                    chapterNumber = chapterNumber,
                                    verseNumber = verseNumber
                                )
                            )
                        },
                        onNavigateToRandomVerse = {
                            backStack.add(RandomVerseRoute)
                        }
                    )
                }

                is RandomVerseRoute -> {
                    val randomViewModel: RandomVerseViewModel = viewModel(factory = viewModelFactory)

                    RandomVerseScreen(
                        viewModel = randomViewModel,
                        onJumpToScripture = { bookId, chapterNumber, verseNumber ->
                            backStack.add(
                                ReaderRoute(
                                    bookId = bookId,
                                    chapterNumber = chapterNumber,
                                    verseNumber = verseNumber
                                )
                            )
                        },
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        }
                    )
                }
            }
        }
    }
}
