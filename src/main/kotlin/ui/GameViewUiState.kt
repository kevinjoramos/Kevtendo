package ui

import androidx.compose.ui.graphics.Color

data class GameViewUiState(
    var pixelScreen: List<List<Color>> = List(240) { List(256) { Color.Red } }
)