package ui

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class MainCpuViewState(
    val registers: ImmutableList<String> =
        listOf(
            "PC: 0000",
            "A: 00",
            "X: 00",
            "Y: 00",
            "SP: 0000"
        ).toImmutableList(),
    val flags: ImmutableList<Pair<String, Boolean>> =
        listOf(
            Pair("N", true),
            Pair("V", true),
            Pair("-", true),
            Pair("B", true),
            Pair("D", true),
            Pair("I", true),
            Pair("Z", true),
            Pair("C", true),
        ).toImmutableList()
)