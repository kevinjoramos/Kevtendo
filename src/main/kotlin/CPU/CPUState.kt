package CPU

data class CPUState(
    val programCounter: UShort,
    val stackPointer: UByte,
    val accumulator: UByte,
    val xRegister: UByte,
    val yRegister: UByte,
    val statusRegister: UByte
)