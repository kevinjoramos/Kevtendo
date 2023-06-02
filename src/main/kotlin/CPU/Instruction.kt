package CPU

import util.to2DigitHexString
import util.to4DigitHexString

@ExperimentalUnsignedTypes
class Instruction(
    private val addressingMode: AddressingMode,
    private val operation: AssemblyCode,
) {

    fun runOperation() {
        when (addressingMode) {
            AddressingMode.IMM -> {
                val targetValue = operation.cpuReference.immediateAddressing()
                operation.execute(targetValue)
            }
            AddressingMode.ABS -> {
                val targetAddress = operation.cpuReference.absoluteAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.ABS_X -> {
                val targetAddress = operation.cpuReference.absoluteXIndexedAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.ABS_Y -> {
            val targetAddress = operation.cpuReference.absoluteYIndexedAddressing()
            operation.execute(targetAddress)
            }
            AddressingMode.ZPG -> {
                val targetAddress = operation.cpuReference.zeroPageAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.ZPG_X -> {
                val targetAddress = operation.cpuReference.zeroPageXIndexedAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.ZPG_Y -> {
                val targetAddress = operation.cpuReference.zeroPageYIndexedAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.IND -> {
                val targetAddress = operation.cpuReference.indirectAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.X_IND -> {
                val targetAddress = operation.cpuReference.xIndexedIndirectAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.IND_Y -> {
                val targetAddress = operation.cpuReference.indirectYIndexedAddressing()
                operation.execute(targetAddress)
            }
            AddressingMode.REL -> {
                val targetAddress = operation.cpuReference.relativeAddressing()
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

    }

    fun log(
        programCounter: UShort,
        opcode: UByte,
        operandLowByte: UByte?,
        operandHighByte: UByte?,
        opcodeName: String,
        targetAddress


    ) {
        "C000  4C F5 C5  JMP $C5F5                       A:00 X:00 Y:00 P:24 SP:FD PPU:  0, 21 CYC:7\n"
        val logState = programCounter.to4DigitHexString() + "  " + opcode.to2DigitHexString() + "  " +
                (operandLowByte?.to2DigitHexString() ?: "  ") + "  " + (operandHighByte?.to2DigitHexString() ?: "  ") +
                "  " + opcodeName +
    }

}