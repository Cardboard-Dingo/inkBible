package app.unpolished.inkbible.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.unpolished.inkbible.domain.repository.BibleRepository
import app.unpolished.inkbible.ui.navigation.BibleNavigationViewModel
import app.unpolished.inkbible.ui.random.RandomVerseViewModel
import app.unpolished.inkbible.ui.reader.ScriptureReaderViewModel

class InkBibleViewModelFactory(
    private val bibleRepository: BibleRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ScriptureReaderViewModel::class.java) -> {
                ScriptureReaderViewModel(bibleRepository) as T
            }
            modelClass.isAssignableFrom(BibleNavigationViewModel::class.java) -> {
                BibleNavigationViewModel(bibleRepository) as T
            }
            modelClass.isAssignableFrom(RandomVerseViewModel::class.java) -> {
                RandomVerseViewModel(bibleRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
