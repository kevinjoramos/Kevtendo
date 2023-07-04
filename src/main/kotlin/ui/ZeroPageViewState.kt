package ui

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class ZeroPageViewState(
    val zeroPage: ImmutableList<ImmutableList<String>> =
        List(16) { List(16) { "00" }.toImmutableList() }.toImmutableList()
)
