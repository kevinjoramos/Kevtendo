package UI

data class CPUState(
    var programCounter: String,
    var stackPointer: String,
    var accumulator: String,
    var xRegister: String,
    var yRegister: String,
    var negativeFlag: Boolean,
    var overflowFlag: Boolean,
    var extraFlag: Boolean,
    var breakFlag: Boolean,
    var decimalFlag: Boolean,
    var interruptDisableFlag: Boolean,
    var zeroFlag: Boolean,
    var carryFlag: Boolean
)