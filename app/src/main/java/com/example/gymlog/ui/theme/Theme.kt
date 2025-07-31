package com.example.gymlog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Enum para representar os temas disponíveis
enum class AppTheme {
    DEFAULT,
    FOREST,
    OCEAN
}

// 1. Tema Padrão
private val LightDefaultColorScheme = lightColorScheme(
    primary = LightPrimary, onPrimary = LightOnPrimary, primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer, secondary = LightSecondary, onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer, onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary, onTertiary = LightOnTertiary, tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer, background = LightBackground, onBackground = LightOnBackground,
    surface = LightSurface, onSurface = LightOnSurface, surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant, outline = LightOutline, error = LightError, onError = LightOnError,
    errorContainer = LightErrorContainer, onErrorContainer = LightOnErrorContainer
)

private val DarkDefaultColorScheme = darkColorScheme(
    primary = DarkPrimary, onPrimary = DarkOnPrimary, primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer, secondary = DarkSecondary, onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer, onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary, onTertiary = DarkOnTertiary, tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer, background = DarkBackground, onBackground = DarkOnBackground,
    surface = DarkSurface, onSurface = DarkOnSurface, surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant, outline = DarkOutline, error = DarkError, onError = DarkOnError,
    errorContainer = DarkErrorContainer, onErrorContainer = DarkOnErrorContainer
)

// 2. Tema Florestal
private val LightForestColorScheme = lightColorScheme(
    primary = ForestPrimary, secondary = ForestSecondary, tertiary = ForestTertiary, background = ForestBackground,
    surface = ForestSurface, onPrimary = LightOnPrimary, onSecondary = LightOnSecondary, onTertiary = LightOnTertiary,
    onBackground = DarkBackground, onSurface = DarkBackground
)
private val DarkForestColorScheme = darkColorScheme(
    primary = DarkForestPrimary, secondary = DarkForestSecondary, tertiary = DarkForestTertiary, background = DarkForestBackground,
    surface = DarkForestSurface, onPrimary = DarkOnPrimary, onSecondary = DarkOnSecondary, onTertiary = DarkOnTertiary,
    onBackground = LightBackground, onSurface = LightBackground
)

// 3. Tema Oceano
private val LightOceanColorScheme = lightColorScheme(
    primary = OceanPrimary, secondary = OceanSecondary, tertiary = OceanTertiary, background = OceanBackground,
    surface = OceanSurface, onPrimary = LightOnPrimary, onSecondary = LightOnPrimary, onTertiary = LightOnPrimary,
    onBackground = DarkBackground, onSurface = DarkBackground
)
private val DarkOceanColorScheme = darkColorScheme(
    primary = DarkOceanPrimary, secondary = DarkOceanSecondary, tertiary = DarkOceanTertiary, background = DarkOceanBackground,
    surface = DarkOceanSurface, onPrimary = DarkOnPrimary, onSecondary = DarkOnPrimary, onTertiary = DarkOnPrimary,
    onBackground = LightBackground, onSurface = LightBackground
)

@Composable
fun GymLogTheme(
    darkTheme: Boolean, // Removido valor padrão
    appTheme: AppTheme, // Removido valor padrão
    content: @Composable () -> Unit
) {
    // A lógica agora é 100% baseada nos parâmetros recebidos.
    // Ele escolhe o tema (DEFAULT, FOREST, etc.) e depois aplica a variante
    // clara ou escura com base no booleano 'darkTheme'.
    val colorScheme = when (appTheme) {
        AppTheme.DEFAULT -> if (darkTheme) DarkDefaultColorScheme else LightDefaultColorScheme
        AppTheme.FOREST -> if (darkTheme) DarkForestColorScheme else LightForestColorScheme
        AppTheme.OCEAN -> if (darkTheme) DarkOceanColorScheme else LightOceanColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}