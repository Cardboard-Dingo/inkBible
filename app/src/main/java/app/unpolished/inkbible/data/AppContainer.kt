package app.unpolished.inkbible.data

import android.content.Context
import app.unpolished.inkbible.data.local.BibleRepositoryImpl
import app.unpolished.inkbible.data.local.ReadingPositionRepositoryImpl
import app.unpolished.inkbible.data.local.readingPositionDataStore
import app.unpolished.inkbible.domain.repository.BibleRepository
import app.unpolished.inkbible.domain.repository.ReadingPositionRepository

class AppContainer(context: Context) {
    val readingPositionRepository: ReadingPositionRepository by lazy {
        ReadingPositionRepositoryImpl(context.applicationContext.readingPositionDataStore)
    }

    val bibleRepository: BibleRepository by lazy {
        BibleRepositoryImpl(
            context = context.applicationContext,
            readingPositionRepository = readingPositionRepository
        )
    }
}
