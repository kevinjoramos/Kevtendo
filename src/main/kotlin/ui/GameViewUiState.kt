package ui

import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class GameViewUiState(
    val pixelScreen: ImmutableList<ImmutableList<Color>> =
        List(240) { List(256) { Color.Transparent }.toImmutableList() }.toImmutableList()
)