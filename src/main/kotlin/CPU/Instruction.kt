package CPU

@ExperimentalUnsignedTypes
class Instruction(
    private val addressingMode: AddressingMode,
    private val operation: AssemblyCode,
    val cpu: CPU6502
) {

    fun runOperation() {
        when (addressingMode) {
            AddressingMode.IMM -> {
                val targetValue = cpu.immediateAddressing()
                operation.execute(targetValue)
            }
            AddressingMode.ABS -> {
                val targetAddress = cpu.absoluteAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.ABS_X -> {
                val targetAddress = cpu.absoluteXIndexedAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.ABS_Y -> {
            val targetAddress = cpu.absoluteYIndexedAddressing()
            operation.execute(targetAddress)
            }
            AddressingMode.ZPG -> {
                val targetAddress = cpu.zeroPageAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.ZPG_X -> {
                val targetAddress = cpu.zeroPageXIndexedAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.ZPG_Y -> {
                val targetAddress = cpu.zeroPageYIndexedAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.IND -> {
                val targetAddress = cpu.indirectAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.X_IND -> {
                val targetAddress = cpu.xIndexedIndirectAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.IND_Y -> {
                val targetAddress = cpu.indirectYIndexedAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.REL -> {
                val targetAddress = cpu.relativeAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.A -> {
                operation.execute()
            }
            AddressingMode.IMP->
                operation.execute()
        }
    }

    fun disassemble() {
        val
    }

}