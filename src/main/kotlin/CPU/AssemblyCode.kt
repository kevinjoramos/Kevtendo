package CPU

@ExperimentalUnsignedTypes
interface AssemblyCode {
    val cpuReference: CPU6502
    val opcodeName: String
    val cycleCount: Map<AddressingMode, Int>

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
