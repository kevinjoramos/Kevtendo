package CPU

data class InstructionWrapper(
    val executionFunction: () -> Unit,
    val opcodeName: String,
    val addressingMode: String,
    val operand: String? = null
)