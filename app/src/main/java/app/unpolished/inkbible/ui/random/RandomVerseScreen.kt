package app.unpolished.inkbible.ui.random

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unpolished.inkbible.domain.model.Testament
import app.unpolished.inkbible.domain.model.Verse
import app.unpolished.inkbible.ui.theme.EInkBlack
import app.unpolished.inkbible.ui.theme.EInkButton
import app.unpolished.inkbible.ui.theme.EInkCard
import app.unpolished.inkbible.ui.theme.EInkChip
import app.unpolished.inkbible.ui.theme.EInkDivider
import app.unpolished.inkbible.ui.theme.EInkOutlinedButton
import app.unpolished.inkbible.ui.theme.EInkWhite
import app.unpolished.inkbible.ui.theme.InkBibleTheme

@Composable
fun RandomVerseScreen(
    viewModel: RandomVerseViewModel,
    onJumpToScripture: (bookId: String, chapterNumber: Int, verseNumber: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    RandomVerseContent(
        uiState = uiState,
        onNewRandomVerse = { viewModel.fetchRandomVerse() },
        onFilterByTestament = { testament -> viewModel.fetchRandomVerse(testament) },
        onJumpToScripture = onJumpToScripture,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun RandomVerseContent(
    uiState: RandomVerseUiState,
    onNewRandomVerse: () -> Unit,
    onFilterByTestament: (Testament?) -> Unit,
    onJumpToScripture: (bookId: String, chapterNumber: Int, verseNumber: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<Testament?>(null) }

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
                is RandomVerseUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = EInkBlack)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Finding Random Scripture...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                is RandomVerseUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error",
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
                        EInkButton(onClick = onNewRandomVerse) {
                            Text("Try Again", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is RandomVerseUiState.Success -> {
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
                                onClick = onBack,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("← Back", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                            }

                            Text(
                                text = "Random Scripture",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            EInkButton(
                                onClick = onNewRandomVerse,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("🎲 Another", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EInkChip(
                                text = "All",
                                isSelected = selectedFilter == null,
                                onClick = {
                                    selectedFilter = null
                                    onFilterByTestament(null)
                                }
                            )
                            EInkChip(
                                text = "Old Testament",
                                isSelected = selectedFilter == Testament.OT,
                                onClick = {
                                    selectedFilter = Testament.OT
                                    onFilterByTestament(Testament.OT)
                                }
                            )
                            EInkChip(
                                text = "New Testament",
                                isSelected = selectedFilter == Testament.NT,
                                onClick = {
                                    selectedFilter = Testament.NT
                                    onFilterByTestament(Testament.NT)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        EInkDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // Random Verse Card
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            EInkCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${uiState.bookName} ${uiState.verse.chapterNumber}:${uiState.verse.verseNumber}",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        EInkChip(
                                            text = if (uiState.testament == Testament.OT) "OT" else "NT",
                                            isSelected = false,
                                            onClick = {}
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    EInkDivider()
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = uiState.verse.text,
                                        fontSize = 20.sp,
                                        fontFamily = FontFamily.Serif,
                                        lineHeight = 28.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Jump to Scripture Button
                            EInkButton(
                                onClick = {
                                    onJumpToScripture(
                                        uiState.verse.bookId,
                                        uiState.verse.chapterNumber,
                                        uiState.verse.verseNumber
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "📖 Jump to ${uiState.bookName} ${uiState.verse.chapterNumber}:${uiState.verse.verseNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
fun RandomVerseScreenEInkPreview() {
    val sampleVerse = Verse("JOHN", 3, 16, "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.")

    InkBibleTheme {
        RandomVerseContent(
            uiState = RandomVerseUiState.Success(
                verse = sampleVerse,
                bookName = "John",
                testament = Testament.NT
            ),
            onNewRandomVerse = {},
            onFilterByTestament = {},
            onJumpToScripture = { _, _, _ -> },
            onBack = {}
        )
    }
}
