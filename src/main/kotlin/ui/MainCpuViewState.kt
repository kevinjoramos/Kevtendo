package ui

data class MainCpuViewState(
    var registers: List<String>,
    var flags: List<Pair<String, Boolean>>
)