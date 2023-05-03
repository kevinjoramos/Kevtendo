package CPU

import Bus

/**
 * Emulation of the 6502 processor.
 * TODO("implement addressing modes")
 * TODO("implement all opcodes")
 * TODO("implement main fetch loop")
 * TODO("implement control signals")
 * TODO("implement clock cycles")
 * TODO("implement bootup sequence.")
 *
 *
 */
@ExperimentalUnsignedTypes
class CPU (val bus: Bus) {

    /**
     * 6502 Architecture components
     */
    var programCounter: UShort = 0x0000u
    var stackPointer: UByte = 0xFDu
    var accumulator: UByte = 0x00u
    var xRegister: UByte = 0x00u
    var yRegister: UByte = 0x00u

    /**
     * FLAGS
     *
     * "The zero flag (Z) indicates a value of all zero bits and the negative flag (N)
     * indicates the presence of a set sign bit in bit-position 7. These flags are always
     * updated, whenever a value is transferred to a CPU register (A,X,Y) and as a result
     * of any logical ALU operations. The Z and N flags are also updated by increment and
     * decrement operations acting on a memory location."
     */
    var negativeFlag = false
    var overflowFlag = false
    var extraFlag = true
    var breakFlag = true
    var decimalFlag = false
    var interruptDisableFlag = true
    var zeroFlag = false
    var carryFlag = false

    private val addressingModeMap: Map<UByte, () -> UShort> = mapOf(
        (0x00u).toUByte() to ::implicitAddressingMode
    )



    /**
     * Interrupt control signals.
     */
    fun irq() {}

    fun nmi() {}

    fun ready() {}

    fun run() {
        val opcode: UByte = bus.readAddress(programCounter)
        val addressingMode = decodeByteToAddressingMode(opcode)
        val instruction = decodeByteToInstruction(opcode)

        val targetAddress = addressingMode.invoke()


    }

    private fun decodeByteToAddressingMode(opcode: UByte): () -> UShort {
        return addressingModeMap[opcode] ?: { (0u).toUShort() }
    }

    private fun decodeByteToInstruction(opcode:UByte): Instruction {
        TODO("Not implemented.")
    }


    /**
     * Addressing modes
     * *implicit mode not included since its target it inferred from instruction.
     * *accumulator mode not included since a kotlin function would only return a copy
     * of the accumulators value.
     */
    fun implicitAddressingMode(): UShort {
        return 0u
    }

    fun accumulatorAddressingMode(): UShort {
        return 0u
    }

    /**
     * "Immediate addressing allows the programmer to directly specify an 8 bit constant within the instruction."
     * returns byte from next instruction address.
     */
    fun immediateAddressingMode(): UByte {
        programCounter++
        return bus.readAddress(programCounter)
    }

    /**
     * Zero page addressing allows for efficient access to the first 256 bytes of memory.
     * referenced by one byte.
     * Returns address of specified zero-page address.
     */
    fun zeroPageAddressingMode(): UShort {
        programCounter++
        return bus.readAddress(programCounter).toUShort()
    }

    /**
     * Zero page X addressing allows for the xRegister offset to be added to the operand.
     * Returns address of specified zero-page address.
     */
    fun zeroPageXAddressingMode(): UShort {
        programCounter++
        val operand: UByte = bus.readAddress(programCounter)
        val targetAddress: UByte = (operand + xRegister).toUByte()
        return targetAddress.toUShort()
    }

    /**
     * Zero page Y addressing allows for the yRegister offset to be added to the operand.
     * Returns address of specified zero-page address.
     */
    fun zeroPageYAddressingMode(): UShort {
        programCounter++
        val operand: UByte = bus.readAddress(programCounter)
        val targetAddress: UByte = (operand + yRegister).toUByte()
        return targetAddress.toUShort()
    }

    /**
     * Relative addressing adds the operand(offset) to the program counter to return the target address
     * in memory.
     */
    fun relativeAddressingMode(): UShort {
        programCounter++
        val addressOffset = bus.readAddress(programCounter)
        return (programCounter + addressOffset).toUShort()
    }

    fun absoluteAddressingMode(): UShort {
        programCounter++
        val leastSignificantByte = bus.readAddress(programCounter).toUShort()
        programCounter++
        val mostSignificantByte = bus.readAddress((programCounter)).toUInt()
        return ((mostSignificantByte shl 8) + leastSignificantByte).toUShort()
    }

    fun absoluteXAddressingMode(): UShort {
        programCounter++
        val leastSignificantByte = bus.readAddress(programCounter).toUShort()
        programCounter++
        val mostSignificantByte = bus.readAddress((programCounter)).toUInt()
        return ((mostSignificantByte shl 8) + leastSignificantByte + xRegister).toUShort()
    }

    fun absoluteYAddressingMode(): UShort {
        programCounter++
        val leastSignificantByte = bus.readAddress(programCounter).toUShort()
        programCounter++
        val mostSignificantByte = bus.readAddress((programCounter)).toUInt()
        return ((mostSignificantByte shl 8) + leastSignificantByte + yRegister).toUShort()
    }

    fun indirectAddressingMode(): UShort {
        programCounter++
        val leastSignificantByte: UByte = bus.readAddress(programCounter)
        programCounter++
        val mostSignificantByte = bus.readAddress(programCounter).toUInt()
        val indirectAddress: UShort = ((mostSignificantByte shl 8) + leastSignificantByte).toUShort()

        programCounter = indirectAddress

        val indirectLeastSignificantByte: UByte = bus.readAddress(programCounter)
        programCounter++
        val indirectMostSignificantByte = bus.readAddress(programCounter).toUInt()
        return ((indirectMostSignificantByte shl 8) + indirectLeastSignificantByte).toUShort()
    }

    fun indexedIndirectAddressingMode(): UShort {
        programCounter++
        val operand: UByte = bus.readAddress(programCounter)
        val zeroPageAddress: UShort = (operand + xRegister).toUByte().toUShort()
        programCounter = zeroPageAddress
        val targetLeastSignificantByte: UByte = bus.readAddress(programCounter)
        programCounter++
        val targetMostSignificantByte = bus.readAddress(programCounter).toUInt()
        return ((targetMostSignificantByte shl 8) + targetLeastSignificantByte).toUShort()
    }

    /**
     * In inddireactIndexed addressing the operand is a zero page address whose contents are added with carry (C) to the Y register
     * $aa + Y (C), the LSB result contains the LSB of the EA.
     * The contents of address (operand + $01 + C) contain the MSB of the EA.
     */
    fun indirectIndexedAddressingMode(): UShort {
        programCounter++
        val zeroPageOperand: UByte = bus.readAddress(programCounter)
        val targetLSBWithCarry: UShort = (zeroPageOperand + yRegister).toUShort()

        programCounter++
        val finalOperand: UByte = bus.readAddress(programCounter)
        val leastSignificantByte: UByte = targetLSBWithCarry.toUByte()
        val mostSignificantByte: UByte = (finalOperand + (targetLSBWithCarry.toUInt() shr 8)).toUByte()

        return ((mostSignificantByte.toUInt() shl 8) + leastSignificantByte).toUShort()
    }


    /**
     * Op codes
     * I chose to implement the opcodes as
     */

    class ADC(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class AND(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class ASL(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class BCC(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class BCS(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class BEQ(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class BIT(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class BMI(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class BNE(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class BPL(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class BRK(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class BVC(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class BVS(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    /**
     * Clear Carry Flag
     * This instruction initializes the carry flag to a 0.
     * This instruction affects no registers in the microprocessor and no flags other than the carry flag which is reset.
     */
    inner class CLC(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.carryFlag = false
        }
    }

    /**
     * Clear Decimal Mode
     * This instruction sets the decimal mode flag to a 0.
     * CLD affects no registers in the microprocessor and no flags other than the decimal mode flag which is set to a 0.
     */
    inner class CLD(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.decimalFlag = false
        }
    }

    /**
     * Clear Interrupt Disable
     * This instruction initializes the interrupt disable to a 0. This allows the microprocessor to receive interrupts.
     * It affects no registers in the microprocessor and no flags other than the interrupt disable which is cleared.
     */
    inner class CLI(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.interruptDisableFlag = false
        }
    }


    /**
     * Clear Overflow Flag
     * This instruction clears the overflow flag to a 0.
     * CLV affects no registers in the microprocessor and no flags other than the overflow flag which is set to a 0.
     */
    inner class CLV(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.overflowFlag = false
        }
    }

    class CMP(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class CPX(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class CPY(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class DEC(): Instruction() {
        override fun run(targetAddress: UShort) {
            val operand: UByte = this@CPU.bus.readAddress(targetAddress)
            val result = operand.dec()
            this@CPU.bus.writeToAddress(targetAddress, result)

            this@CPU.zeroFlag = result == (0x00u).toUByte()
            this@CPU.negativeFlag = (result.toUInt() shr 7) == 1u
        }
    }

    inner class DEX(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.xRegister--

            this@CPU.zeroFlag = this@CPU.xRegister == (0x00u).toUByte()
            this@CPU.negativeFlag = (this@CPU.xRegister.toUInt() shr 7) == 1u
        }
    }

    inner class DEY(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.yRegister--

            this@CPU.zeroFlag = this@CPU.yRegister == (0x00u).toUByte()
            this@CPU.negativeFlag = (this@CPU.yRegister.toUInt() shr 7) == 1u
        }
    }

    class EOR(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    /**
     * INCREMENT at MEMORY
     * This instruction adds 1 to the contents of the addressed memory loca­tion.
     *
     * The increment memory instruction does not affect any internal registers and does not affect the
     * carry or overflow flags. If bit 7 is on as the result of the increment,N is set, otherwise it is reset;
     * if the increment causes the result to become 0, the Z flag is set on, otherwise it is reset.
     */
    inner class INC(): Instruction() {
        override fun run(targetAddress: UShort) {
            val operand: UByte = this@CPU.bus.readAddress(targetAddress)
            val result = operand.inc()
            this@CPU.bus.writeToAddress(targetAddress, result)

            this@CPU.zeroFlag = result == (0x00u).toUByte()
            this@CPU.negativeFlag = (result.toUInt() shr 7) == 1u
        }
    }

    /**
     * INCREMENT the X register
     * Increment X adds 1 to the current value of the X register. This is an 8-bit increment which does not affect
     * the carry operation, therefore, if the value of X before the increment was FF, the resulting value is 00.
     *
     * INX does not affect the carry or overflow flags; it sets the N flag if the result of the increment has a one
     * in bit 7, otherwise resets N; sets the Z flag if the result of the increment is 0, otherwise it resets the Z flag.
     *
     * INX does not affect any other register other than the X register.
     */
    inner class INX(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.xRegister++

            this@CPU.zeroFlag = this@CPU.xRegister == (0x00u).toUByte()
            this@CPU.negativeFlag = (this@CPU.xRegister.toUInt() shr 7) == 1u
        }
    }

    /**
     * INCREMENT the Y register.
     * Increment Y increments or adds one to the current value in the Y register, storing the result in the Y register.
     * As in the case of INX the primary application is to step thru a set of values using the Y register.
     *
     * The INY does not affect the carry or overflow flags, sets the N flag if the result of the increment has a one
     * in bit 7, otherwise resets N, sets Z if as a result of the increment the Y register is zero otherwise resets
     * the Z flag.
     */
    inner class INY(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.yRegister++

            this@CPU.zeroFlag = this@CPU.yRegister == (0x00u).toUByte()
            this@CPU.negativeFlag = (this@CPU.yRegister.toUInt() shr 7) == 1u
        }
    }

    class JMP(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class JSR(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    /**
     * Load Accumulator
     * Load the accumulator from memory
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class LDA(): Instruction() {
        override fun run(targetAddress: UShort) {
            val data: UByte = bus.readAddress(targetAddress)
            this@CPU.accumulator = data

            this@CPU.zeroFlag = data == (0x00u).toUByte()
            this@CPU.negativeFlag = (data.toUInt() shr 7) == 1u
        }
    }

    /**
     * Load X Register
     * Load the index register X from memory.
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class LDX(): Instruction() {
        override fun run(targetAddress: UShort) {
            val data: UByte = bus.readAddress(targetAddress)
            this@CPU.xRegister = data

            this@CPU.zeroFlag = data == (0x00u).toUByte()
            this@CPU.negativeFlag = (data.toUInt() shr 7) == 1u
        }
    }

    /**
     * Load Y Register
     * Load the index register Y from memory.
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class LDY(): Instruction() {
        override fun run(targetAddress: UShort) {
            val data: UByte = bus.readAddress(targetAddress)
            this@CPU.yRegister = data

            this@CPU.zeroFlag = data == (0x00u).toUByte()
            this@CPU.negativeFlag = (data.toUInt() shr 7) == 1u
        }
    }

    class LSR(): Instruction() {
        override fun run(targetAddress: UShort){
            TODO("Not yet implemented")
        }
    }

    /**
     * No Operation
     */
    class NOP(): Instruction() {
        override fun run(targetAddress: UShort) {
            return
        }
    }

    class ORA(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class PHA(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class PHP(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class PLA(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class PLP(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class ROL(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class ROR(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class RTI(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class RTS(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    class SBC(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    /**
     * Set Carry Flag
     * This instruction initializes the carry flag to a 1.
     * This instruction affects no registers in the microprocessor and no flags other than the carry flag which is set.
     */
    inner class SEC(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.carryFlag = true
        }
    }

    /**
     * Set Decimal Flag
     * This instruction sets the decimal mode flag D to a 1.
     * SED affects no registers in the microprocessor and no flags other than the decimal mode which is set to a 1.
     */
    inner class SED(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.decimalFlag = true
        }
    }

    /**
     * Set Interrupt Disable
     * This instruction initializes the interrupt disable to a 1.
     * It is used to mask interrupt requests during system reset operations and during interrupt commands.
     * It affects no registers in the microprocessor and no flags other than the interrupt disable which is set.
     */
    inner class SEI(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.interruptDisableFlag = true
        }
    }

    /**
     * Store Accumulator In Memory
     * This instruction transfers the contents of the accumulator to memory.
     * This instruction affects none of the flags in the processor status register and does not affect the accumulator.
     */
    inner class STA(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.bus.writeToAddress(targetAddress, this@CPU.accumulator)
        }
    }

    /**
     * Store Register X in Memory
     * Transfers value of X register to addressed memory location.
     * No flags or registers in the microprocessor are affected by the store operation.
     */
    inner class STX(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.bus.writeToAddress(targetAddress, this@CPU.xRegister)
        }
    }

    /**
     * Store Register Y in Memory
     * Transfer the value of the Y register to the addressed memory location.
     * STY does not affect any flags or registers in the microprocessor.
     */
    inner class STY(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.bus.writeToAddress(targetAddress, this@CPU.yRegister)
        }
    }

    inner class TAX(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class TAY(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class TSX(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class TXA(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class TXS(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class TYA(): Instruction() {
        override fun run(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }




}