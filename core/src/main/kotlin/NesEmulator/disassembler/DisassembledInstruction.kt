package disassembler

data class DisassembledInstruction(
    val opcodeName: String,
    val addressingMode: String,
    val opcodeOperand: String?
)