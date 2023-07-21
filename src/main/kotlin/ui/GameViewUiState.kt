package ui

import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class GameViewUiState(
    val pixelScreen: ImmutableList<ImmutableList<UByte>> =
        List(240) { List(256) { (0x0Fu).toUByte() }.toImmutableList() }.toImmutableList()
)