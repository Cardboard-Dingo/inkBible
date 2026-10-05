package app.unpolished.inkbible.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PureEInkColorScheme = lightColorScheme(
    primary = EInkBlack,
    onPrimary = EInkWhite,
    primaryContainer = EInkWhite,
    onPrimaryContainer = EInkBlack,
    secondary = EInkBlack,
    onSecondary = EInkWhite,
    secondaryContainer = EInkWhite,
    onSecondaryContainer = EInkBlack,
    tertiary = EInkBlack,
    onTertiary = EInkWhite,
    background = EInkWhite,
    onBackground = EInkBlack,
    surface = EInkWhite,
    onSurface = EInkBlack,
    surfaceVariant = EInkWhite,
    onSurfaceVariant = EInkBlack,
    surfaceTint = EInkWhite,
    inverseSurface = EInkBlack,
    inverseOnSurface = EInkWhite,
    inversePrimary = EInkWhite,
    outline = EInkBlack,
    outlineVariant = EInkBlack,
    scrim = EInkBlack
)

private val InvertedEInkColorScheme = darkColorScheme(
    primary = EInkWhite,
    onPrimary = EInkBlack,
    primaryContainer = EInkBlack,
    onPrimaryContainer = EInkWhite,
    secondary = EInkWhite,
    onSecondary = EInkBlack,
    secondaryContainer = EInkBlack,
    onSecondaryContainer = EInkWhite,
    tertiary = EInkWhite,
    onTertiary = EInkBlack,
    background = EInkBlack,
    onBackground = EInkWhite,
    surface = EInkBlack,
    onSurface = EInkWhite,
    surfaceVariant = EInkBlack,
    onSurfaceVariant = EInkWhite,
    surfaceTint = EInkBlack,
    inverseSurface = EInkWhite,
    inverseOnSurface = EInkBlack,
    inversePrimary = EInkBlack,
    outline = EInkWhite,
    outlineVariant = EInkWhite,
    scrim = EInkWhite
)

@Composable
fun InkBibleTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) InvertedEInkColorScheme else PureEInkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EInkTypography,
        content = content
    )
}
