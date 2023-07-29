package cpu

@ExperimentalUnsignedTypes
interface Operation {
    val opcodeName: String

    fun execute() {
        throw InvalidOpcodeException("This opcode cannot use this addressing mode.")
    }

    fun execute(operand: UByte) {
        throw InvalidOpcodeException("This opcode cannot use this addressing mode.")
    }

    fun execute(targetAddress: UShort) {
        throw InvalidOpcodeException("This opcode cannot use this addressing mode.")
    }
}
