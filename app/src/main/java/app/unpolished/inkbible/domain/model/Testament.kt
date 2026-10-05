package app.unpolished.inkbible.domain.model

enum class Testament {
    OT,
    NT;

    val displayName: String
        get() = when (this) {
            OT -> "Old Testament"
            NT -> "New Testament"
        }
}
