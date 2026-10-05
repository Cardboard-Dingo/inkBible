package app.unpolished.inkbible.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unpolished.inkbible.domain.model.Book
import app.unpolished.inkbible.domain.model.Testament
import app.unpolished.inkbible.domain.model.Verse
import app.unpolished.inkbible.ui.theme.EInkBlack
import app.unpolished.inkbible.ui.theme.EInkButton
import app.unpolished.inkbible.ui.theme.EInkCard
import app.unpolished.inkbible.ui.theme.EInkChip
import app.unpolished.inkbible.ui.theme.EInkDivider
import app.unpolished.inkbible.ui.theme.EInkOutlinedButton
import app.unpolished.inkbible.ui.theme.EInkTextField
import app.unpolished.inkbible.ui.theme.EInkWhite
import app.unpolished.inkbible.ui.theme.InkBibleTheme

@Composable
fun BibleNavigationScreen(
    viewModel: BibleNavigationViewModel,
    onNavigateToReader: (bookId: String, chapterNumber: Int, verseNumber: Int) -> Unit,
    onNavigateToRandomVerse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    BibleNavigationContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onFilterTestament = viewModel::filterByTestament,
        onSelectBook = viewModel::selectBook,
        onSelectChapter = viewModel::selectChapter,
        onSelectVerse = { bookId, chapterNumber, verseNumber ->
            onNavigateToReader(bookId, chapterNumber, verseNumber)
        },
        onClearChapter = viewModel::clearSelectedChapter,
        onClearBook = viewModel::clearSelectedBook,
        onNavigateToRandomVerse = onNavigateToRandomVerse,
        onRetry = viewModel::loadBooks,
        modifier = modifier
    )
}

@Composable
fun BibleNavigationContent(
    uiState: BibleNavigationUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterTestament: (Testament?) -> Unit,
    onSelectBook: (String) -> Unit,
    onSelectChapter: (Int) -> Unit,
    onSelectVerse: (bookId: String, chapterNumber: Int, verseNumber: Int) -> Unit,
    onClearChapter: () -> Unit,
    onClearBook: () -> Unit,
    onNavigateToRandomVerse: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EInkWhite,
        contentColor = EInkBlack
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(EInkWhite)
        ) {
            when (uiState) {
                is BibleNavigationUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = EInkBlack)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading Bible Books...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                is BibleNavigationUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error Loading Books",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.message,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        EInkButton(onClick = onRetry) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is BibleNavigationUiState.Success -> {
                    when {
                        uiState.selectedBook != null && uiState.selectedChapterNumber != null -> {
                            // Step 3: Verse Selection
                            VerseSelectionView(
                                book = uiState.selectedBook,
                                chapterNumber = uiState.selectedChapterNumber,
                                verses = uiState.chapterVerses,
                                onSelectVerse = { verseNumber ->
                                    onSelectVerse(uiState.selectedBook.id, uiState.selectedChapterNumber, verseNumber)
                                },
                                onBackToChapters = onClearChapter,
                                onBackToBooks = onClearBook
                            )
                        }
                        uiState.selectedBook != null -> {
                            // Step 2: Chapter Selection
                            ChapterSelectionView(
                                book = uiState.selectedBook,
                                onSelectChapter = onSelectChapter,
                                onBackToBooks = onClearBook,
                                onReadChapterOne = {
                                    onSelectVerse(uiState.selectedBook.id, 1, 1)
                                }
                            )
                        }
                        else -> {
                            // Step 1: Book Selection
                            BookSelectionView(
                                filteredBooks = uiState.filteredBooks,
                                searchQuery = uiState.searchQuery,
                                selectedTestamentFilter = uiState.selectedTestamentFilter,
                                onSearchQueryChange = onSearchQueryChange,
                                onFilterTestament = onFilterTestament,
                                onSelectBook = onSelectBook,
                                onNavigateToRandomVerse = onNavigateToRandomVerse
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookSelectionView(
    filteredBooks: List<Book>,
    searchQuery: String,
    selectedTestamentFilter: Testament?,
    onSearchQueryChange: (String) -> Unit,
    onFilterTestament: (Testament?) -> Unit,
    onSelectBook: (String) -> Unit,
    onNavigateToRandomVerse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bible Books",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            EInkOutlinedButton(
                onClick = onNavigateToRandomVerse,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("🎲 Random Verse", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        EInkTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = "Search books (e.g., Genesis, John)..."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EInkChip(
                text = "All (66)",
                isSelected = selectedTestamentFilter == null,
                onClick = { onFilterTestament(null) }
            )
            EInkChip(
                text = "Old Testament (39)",
                isSelected = selectedTestamentFilter == Testament.OT,
                onClick = { onFilterTestament(Testament.OT) }
            )
            EInkChip(
                text = "New Testament (27)",
                isSelected = selectedTestamentFilter == Testament.NT,
                onClick = { onFilterTestament(Testament.NT) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        EInkDivider()
        Spacer(modifier = Modifier.height(8.dp))

        // Books Grid / List
        if (filteredBooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No books found matching \"$searchQuery\"",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(filteredBooks, key = { it.id }) { book ->
                    EInkCard(
                        onClick = { onSelectBook(book.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = book.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (book.testament == Testament.OT) "OT" else "NT",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${book.chapterCount} ch",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterSelectionView(
    book: Book,
    onSelectChapter: (Int) -> Unit,
    onBackToBooks: () -> Unit,
    onReadChapterOne: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EInkOutlinedButton(
                onClick = onBackToBooks,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("← Books", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            }

            Text(
                text = book.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            EInkButton(
                onClick = onReadChapterOne,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("Read Ch 1", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        EInkDivider()
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Select Chapter (1 – ${book.chapterCount}):",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        val chapterNumbers = (1..book.chapterCount).toList()

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(chapterNumbers) { chapterNum ->
                EInkCard(
                    onClick = { onSelectChapter(chapterNum) },
                    modifier = Modifier.height(52.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chapterNum.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerseSelectionView(
    book: Book,
    chapterNumber: Int,
    verses: List<Verse>,
    onSelectVerse: (Int) -> Unit,
    onBackToChapters: () -> Unit,
    onBackToBooks: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EInkOutlinedButton(
                onClick = onBackToChapters,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("← Ch $chapterNumber", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            }

            Text(
                text = "${book.name} $chapterNumber",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            EInkOutlinedButton(
                onClick = onBackToBooks,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("Books", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        EInkDivider()
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Select Verse:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (verses.isEmpty()) {
            // Fallback if verses list not preloaded: allow picking numeric verse or read chapter
            EInkButton(
                onClick = { onSelectVerse(1) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Read Chapter $chapterNumber from Verse 1", fontWeight = FontWeight.Bold)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(verses, key = { it.verseNumber }) { verse ->
                    EInkCard(
                        onClick = { onSelectVerse(verse.verseNumber) },
                        modifier = Modifier.height(52.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${verse.verseNumber}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
fun BibleNavigationScreenEInkPreview() {
    val sampleBooks = listOf(
        Book("GEN", "Genesis", Testament.OT, 50),
        Book("EXO", "Exodus", Testament.OT, 40),
        Book("LEV", "Leviticus", Testament.OT, 27),
        Book("MATT", "Matthew", Testament.NT, 28),
        Book("MARK", "Mark", Testament.NT, 16),
        Book("JOHN", "John", Testament.NT, 21)
    )

    InkBibleTheme {
        BibleNavigationContent(
            uiState = BibleNavigationUiState.Success(
                books = sampleBooks,
                filteredBooks = sampleBooks
            ),
            onSearchQueryChange = {},
            onFilterTestament = {},
            onSelectBook = {},
            onSelectChapter = {},
            onSelectVerse = { _, _, _ -> },
            onClearChapter = {},
            onClearBook = {},
            onNavigateToRandomVerse = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
fun ChapterSelectionEInkPreview() {
    InkBibleTheme {
        BibleNavigationContent(
            uiState = BibleNavigationUiState.Success(
                books = emptyList(),
                filteredBooks = emptyList(),
                selectedBook = Book("GEN", "Genesis", Testament.OT, 50)
            ),
            onSearchQueryChange = {},
            onFilterTestament = {},
            onSelectBook = {},
            onSelectChapter = {},
            onSelectVerse = { _, _, _ -> },
            onClearChapter = {},
            onClearBook = {},
            onNavigateToRandomVerse = {},
            onRetry = {}
        )
    }
}
