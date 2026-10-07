package br.com.cadernodereceitas.ui.tema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import br.com.cadernodereceitas.model.Categoria

private val Claro = lightColorScheme(
    primary = Color(0xFFA8482A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6DCD1),
    onPrimaryContainer = Color(0xFF3B1609),
    secondary = Color(0xFF6F625A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFE3D8),
    onSecondaryContainer = Color(0xFF2E2420),
    tertiary = Color(0xFF5E6E2A),
    onTertiary = Color.White,
    background = Color(0xFFFBF7F1),
    onBackground = Color(0xFF2E2420),
    surface = Color(0xFFFBF7F1),
    onSurface = Color(0xFF2E2420),
    surfaceVariant = Color(0xFFF1E9DF),
    onSurfaceVariant = Color(0xFF6A5D55),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F1E9),
    surfaceContainer = Color(0xFFF3ECE3),
    surfaceContainerHigh = Color(0xFFEEE6DC),
    surfaceContainerHighest = Color(0xFFE8DFD4),
    outline = Color(0xFF8A7C72),
    outlineVariant = Color(0xFFE2D7CA),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

private val Escuro = darkColorScheme(
    primary = Color(0xFFF0A487),
    onPrimary = Color(0xFF561F0C),
    primaryContainer = Color(0xFF7A3219),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFD7C3B6),
    onSecondary = Color(0xFF3A2E27),
    secondaryContainer = Color(0xFF4A3F38),
    onSecondaryContainer = Color(0xFFF1E0D5),
    tertiary = Color(0xFFBFD08A),
    onTertiary = Color(0xFF2C3600),
    background = Color(0xFF1C1714),
    onBackground = Color(0xFFEDE0D8),
    surface = Color(0xFF1C1714),
    onSurface = Color(0xFFEDE0D8),
    surfaceVariant = Color(0xFF3A312C),
    onSurfaceVariant = Color(0xFFCDBFB5),
    surfaceContainerLowest = Color(0xFF16110F),
    surfaceContainerLow = Color(0xFF241E1A),
    surfaceContainer = Color(0xFF29221E),
    surfaceContainerHigh = Color(0xFF332B27),
    surfaceContainerHighest = Color(0xFF3E3531),
    outline = Color(0xFF978A80),
    outlineVariant = Color(0xFF4D433D),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
)

/** Cores próprias do app que não existem no ColorScheme do Material. */
@Immutable
data class CoresCaderno(
    val salgados: Color,
    val doces: Color,
    val bebidas: Color,
    val dicaFundo: Color,
    val dicaTexto: Color,
) {
    fun daCategoria(categoria: Categoria) = when (categoria) {
        Categoria.SALGADOS -> salgados
        Categoria.DOCES -> doces
        Categoria.BEBIDAS -> bebidas
    }
}

private val CoresClaras = CoresCaderno(
    salgados = Color(0xFF5E6E2A),
    doces = Color(0xFF994A68),
    bebidas = Color(0xFF3B6F85),
    dicaFundo = Color(0xFFFFF1CF),
    dicaTexto = Color(0xFF4F3C0E),
)

private val CoresEscuras = CoresCaderno(
    salgados = Color(0xFF4F5D22),
    doces = Color(0xFF7E3D56),
    bebidas = Color(0xFF2F5A6C),
    dicaFundo = Color(0xFF3D3420),
    dicaTexto = Color(0xFFF3E2B8),
)

val LocalCoresCaderno = staticCompositionLocalOf { CoresClaras }

// Títulos com serifa lembram um caderno escrito; o resto segue a fonte do sistema
private val Tipografia = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontFamily = FontFamily.Serif),
        headlineLarge = headlineLarge.copy(fontFamily = FontFamily.Serif),
        headlineMedium = headlineMedium.copy(fontFamily = FontFamily.Serif),
        headlineSmall = headlineSmall.copy(fontFamily = FontFamily.Serif),
        titleLarge = titleLarge.copy(fontFamily = FontFamily.Serif),
    )
}

@Composable
fun CadernoTheme(escuro: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalCoresCaderno provides if (escuro) CoresEscuras else CoresClaras) {
        MaterialTheme(
            colorScheme = if (escuro) Escuro else Claro,
            typography = Tipografia,
            content = content,
        )
    }
}
