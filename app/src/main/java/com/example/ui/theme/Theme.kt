package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppColorTheme

@Composable
fun VetDosisTheme(
    theme: AppColorTheme = AppColorTheme.EMERALD_VET,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppColorTheme.EMERALD_VET -> if (darkTheme) {
            darkColorScheme(
                primary = EmeraldDarkPrimary,
                onPrimary = EmeraldDarkOnPrimary,
                secondary = EmeraldSecondary,
                background = EmeraldDarkBackground,
                surface = EmeraldDarkSurface,
                surfaceVariant = EmeraldDarkSurfaceVariant
            )
        } else {
            lightColorScheme(
                primary = EmeraldPrimary,
                onPrimary = EmeraldOnPrimary,
                primaryContainer = EmeraldPrimaryContainer,
                onPrimaryContainer = EmeraldOnPrimaryContainer,
                secondary = EmeraldSecondary,
                background = EmeraldBackground,
                surface = EmeraldSurface,
                surfaceVariant = EmeraldSurfaceVariant
            )
        }
        AppColorTheme.OCEAN_TEAL -> if (darkTheme) {
            darkColorScheme(
                primary = OceanDarkPrimary,
                secondary = OceanSecondary,
                background = OceanDarkBackground,
                surface = OceanDarkSurface
            )
        } else {
            lightColorScheme(
                primary = OceanPrimary,
                secondary = OceanSecondary,
                background = OceanBackground,
                surface = OceanSurface
            )
        }
        AppColorTheme.ROYAL_INDIGO -> if (darkTheme) {
            darkColorScheme(
                primary = IndigoDarkPrimary,
                secondary = IndigoSecondary,
                background = IndigoDarkBackground,
                surface = IndigoDarkSurface
            )
        } else {
            lightColorScheme(
                primary = IndigoPrimary,
                secondary = IndigoSecondary,
                background = IndigoBackground,
                surface = IndigoSurface
            )
        }
        AppColorTheme.SUNSET_AMBER -> if (darkTheme) {
            darkColorScheme(
                primary = AmberDarkPrimary,
                secondary = AmberSecondary,
                background = AmberDarkBackground,
                surface = AmberDarkSurface
            )
        } else {
            lightColorScheme(
                primary = AmberPrimary,
                secondary = AmberSecondary,
                background = AmberBackground,
                surface = AmberSurface
            )
        }
        AppColorTheme.SURGICAL_DARK -> {
            darkColorScheme(
                primary = OnyxPrimary,
                secondary = OnyxSecondary,
                background = OnyxDarkBackground,
                surface = OnyxDarkSurface
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
