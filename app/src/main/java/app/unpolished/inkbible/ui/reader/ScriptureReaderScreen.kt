package app.unpolished.inkbible.ui.reader

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unpolished.inkbible.domain.model.Book
import app.unpolished.inkbible.domain.model.Chapter
import app.unpolished.inkbible.domain.model.ReadingPosition
import app.unpolished.inkbible.domain.model.Testament
import app.unpolished.inkbible.domain.model.Verse
import app.unpolished.inkbible.ui.theme.EInkBlack
import app.unpolished.inkbible.ui.theme.EInkButton
import app.unpolished.inkbible.ui.theme.EInkCard
import app.unpolished.inkbible.ui.theme.EInkDivider
import app.unpolished.inkbible.ui.theme.EInkOutlinedButton
import app.unpolished.inkbible.ui.theme.EInkWhite
import app.unpolished.inkbible.ui.theme.InkBibleTheme

@Composable
fun ScriptureReaderScreen(
    viewModel: ScriptureReaderViewModel,
    onNavigateToBooks: () -> Unit,
    onNavigateToRandomVerse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    ScriptureReaderContent(
        uiState = uiState,
        onPrevChapter = viewModel::navigateToPreviousChapter,
        onNextChapter = viewModel::navigateToNextChapter,
        onSelectVerse = viewModel::selectVerse,
        onNavigateToBooks = onNavigateToBooks,
        onNavigateToRandomVerse = onNavigateToRandomVerse,
        onRetry = viewModel::loadLastReadingPosition,
        modifier = modifier
    )
}

@Composable
fun ScriptureReaderContent(
    uiState: ScriptureReaderUiState,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onSelectVerse: (Int) -> Unit,
    onNavigateToBooks: () -> Unit,
    onNavigateToRandomVerse: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fontSizeSp by remember { mutableIntStateOf(18) }

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
                is ScriptureReaderUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = EInkBlack)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading Scripture...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                is ScriptureReaderUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error Loading Scripture",
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
                is ScriptureReaderUiState.Success -> {
                    val listState = rememberLazyListState()

                    // Automatically scroll to selected verse
                    LaunchedEffect(uiState.selectedVerseNumber, uiState.currentChapter.chapterNumber) {
                        val targetVerse = uiState.selectedVerseNumber ?: 1
                        val index = uiState.currentChapter.verses.indexOfFirst { it.verseNumber == targetVerse }
                        if (index >= 0) {
                            listState.scrollToItem(index)
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top Navigation Bar
                        ReaderTopBar(
                            bookName = uiState.currentBook.name,
                            chapterNumber = uiState.currentChapter.chapterNumber,
                            onNavigateToBooks = onNavigateToBooks,
                            onNavigateToRandomVerse = onNavigateToRandomVerse
                        )

                        EInkDivider()

                        // Scripture Reading List
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = uiState.currentChapter.verses,
                                key = { it.verseNumber }
                            ) { verse ->
                                val isSelected = verse.verseNumber == uiState.selectedVerseNumber

                                VerseItem(
                                    verse = verse,
                                    isSelected = isSelected,
                                    fontSizeSp = fontSizeSp,
                                    onClick = { onSelectVerse(verse.verseNumber) }
                                )
                            }
                        }

                        EInkDivider()

                        // Bottom Action Bar
                        ReaderBottomBar(
                            canGoPrev = uiState.canGoToPreviousChapter,
                            canGoNext = uiState.canGoToNextChapter,
                            fontSizeSp = fontSizeSp,
                            onPrevChapter = onPrevChapter,
                            onNextChapter = onNextChapter,
                            onIncreaseFontSize = { if (fontSizeSp < 32) fontSizeSp += 2 },
                            onDecreaseFontSize = { if (fontSizeSp > 12) fontSizeSp -= 2 }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderTopBar(
    bookName: String,
    chapterNumber: Int,
    onNavigateToBooks: () -> Unit,
    onNavigateToRandomVerse: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        EInkOutlinedButton(onClick = onNavigateToBooks) {
            Text("📖 Books", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Text(
            text = "$bookName $chapterNumber",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = EInkBlack
        )

        EInkOutlinedButton(onClick = onNavigateToRandomVerse) {
            Text("🎲 Random", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun VerseItem(
    verse: Verse,
    isSelected: Boolean,
    fontSizeSp: Int,
    onClick: () -> Unit
) {
    EInkCard(
        isSelected = isSelected,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            val annotatedText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = (fontSizeSp + 1).sp,
                        color = if (isSelected) EInkWhite else EInkBlack
                    )
                ) {
                    append("${verse.verseNumber}  ")
                }
                withStyle(
                    style = SpanStyle(
                        fontSize = fontSizeSp.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) EInkWhite else EInkBlack
                    )
                ) {
                    append(verse.text)
                }
            }

            Text(
                text = annotatedText,
                lineHeight = (fontSizeSp + 8).sp
            )
        }
    }
}

@Composable
private fun ReaderBottomBar(
    canGoPrev: Boolean,
    canGoNext: Boolean,
    fontSizeSp: Int,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev Chapter Button
        EInkOutlinedButton(
            onClick = onPrevChapter,
            enabled = canGoPrev
        ) {
            Text("← Prev", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Font Size Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            EInkCard(
                onClick = onDecreaseFontSize,
                modifier = Modifier
            ) {
                Text(
                    text = " A- ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }

            Text(
                text = "${fontSizeSp}pt",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            EInkCard(
                onClick = onIncreaseFontSize,
                modifier = Modifier
            ) {
                Text(
                    text = " A+ ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }

        // Next Chapter Button
        EInkOutlinedButton(
            onClick = onNextChapter,
            enabled = canGoNext
        ) {
            Text("Next →", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
fun ScriptureReaderScreenEInkPreview() {
    val sampleBook = Book("GEN", "Genesis", Testament.OT, 50)
    val sampleChapter = Chapter(
        bookId = "GEN",
        chapterNumber = 1,
        verses = listOf(
            Verse("GEN", 1, 1, "In the beginning God created the heaven and the earth."),
            Verse("GEN", 1, 2, "And the earth was without form, and void; and darkness was upon the face of the deep."),
            Verse("GEN", 1, 3, "And God said, Let there be light: and there was light.")
        )
    )

    InkBibleTheme {
        ScriptureReaderContent(
            uiState = ScriptureReaderUiState.Success(
                currentBook = sampleBook,
                currentChapter = sampleChapter,
                selectedVerseNumber = 1,
                canGoToPreviousChapter = false,
                canGoToNextChapter = true,
                readingPosition = ReadingPosition("GEN", 1, 1)
            ),
            onPrevChapter = {},
            onNextChapter = {},
            onSelectVerse = {},
            onNavigateToBooks = {},
            onNavigateToRandomVerse = {},
            onRetry = {}
        )
    }
}
