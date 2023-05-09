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

    /**
     * And Memory with Accumulator
     * The AND instruction transfer the accumulator and memory to the adder which performs a bit-by-bit AND operation
     * and stores the result back in the accumulator.
     *
     * This instruction affects the accumulator; sets the zero flag if the result in the accumulator is 0,
     * otherwise resets the zero flag; sets the negative flag if the result in the accumulator has bit 7 on,
     * otherwise resets the negative flag.
     */
    inner class AND(): Instruction() {
        override fun run(targetAddress: UShort) {
            val operand: UByte = this@CPU.bus.readAddress(targetAddress)
            val result: UByte = this@CPU.accumulator and operand
            this@CPU.accumulator = result

            this@CPU.zeroFlag = result == (0x00u).toUByte()
            this@CPU.negativeFlag = (result.toUInt() shr 7) == 1u
        }
    }

    /**
     * Arithmetic Shift Left on Accumulator
     * ASL either shifts the accumulator left 1 bit or is a read/modify/write instruction that affects only memory.
     * The instruction does not affect the overflow bit, sets N equal to the result bit 7 (bit 6 in the input), sets
     * Z flag if the result is equal to 0, otherwise resets Z and stores the input bit 7 in the carry flag.
     *
     * Note: I had to separate the ASL command into two implementations because passing the accumulator creates a copy,
     * and does not change the accumulator.
     */
    inner class ASLA(): Instruction() {
        override fun run(targetAddress: UShort) {
            val data: UInt = this@CPU.accumulator.toUInt()
            val result: UByte = (data shl 1).toUByte()
            this@CPU.accumulator = result

            this@CPU.carryFlag = (data shr 7) == 1u
            this@CPU.zeroFlag = result == (0x00u).toUByte()
            this@CPU.negativeFlag = (result.toUInt() shr 7) == 1u
        }
    }

    /**
     * Arithmetic Shift
     * ASL either shifts the accumulator left 1 bit or is a read/modify/write instruction that affects only memory.
     * The instruction does not affect the overflow bit, sets N equal to the result bit 7 (bit 6 in the input), sets
     * Z flag if the result is equal to 0, otherwise resets Z and stores the input bit 7 in the carry flag.
     *
     * Note: I had to separate the ASL command into two implementations because passing the accumulator creates a copy,
     * and does not change the accumulator.
     */
    inner class ASL(): Instruction() {
        override fun run(targetAddress: UShort) {
            val data: UInt = this@CPU.bus.readAddress(targetAddress).toUInt()
            val result: UByte = (data shl 1).toUByte()
            this@CPU.bus.writeToAddress(targetAddress, result)

            this@CPU.carryFlag = (data shr 7) == 1u
            this@CPU.zeroFlag = result == (0x00u).toUByte()
            this@CPU.negativeFlag = (result.toUInt() shr 7) == 1u
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

    /**
     * Test Bits in Memory with Accumulator
     * Bit 7 of operand toggles the negative flag;
     * Bit 6 of perand toggles the overflow flag
     * the zero-flag is set to the result of operand AND accumulator.
     */
    inner class BIT(): Instruction() {
        override fun run(targetAddress: UShort) {
            val operand: UInt = bus.readAddress(targetAddress).toUInt()
            val result: UByte = this@CPU.accumulator and operand.toUByte()

            this@CPU.negativeFlag = (operand shr 7) == 1u
            this@CPU.overflowFlag = (operand and 0x40u) != 0u
            this@CPU.zeroFlag = result == (0x00u).toUByte()
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

    /**
     * Exclusive OR Memory with Accumulator
     * performs a binary "EXCLUSIVE OR" on a bit-by-bit basis and stores the result in the accumulator.
     * Negative flag toggled by bit 7 of result.
     * Zero flag toggled by result.
     */
    inner class EOR(): Instruction() {
        override fun run(targetAddress: UShort) {
            val operand: UByte = this@CPU.bus.readAddress(targetAddress)
            val result: UByte = this@CPU.accumulator xor operand
            this@CPU.accumulator = result

            this@CPU.negativeFlag = (result.toUInt() shr 7) == 1u
            this@CPU.zeroFlag = result == (0x00u).toUByte()
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

    /**
     * Logical Shift Right on Accumulator
     * This instruction shifts either the accumulator or a specified memory location 1 bit to the right, with the
     * higher bit of the result always being set to 0, and the low bit which is shifted out of the field being
     * stored in the carry flag.
     *
     * The shift right does not affect the overflow flag.
     * The N flag is always reset.
     * The Z flag is set if the result of the shift is 0 and reset otherwise.
     * The carry is set equal to bit 0 of the input.
     *
     * Note: I had to separate the LSR command into two implementations because passing the accumulator creates a copy,
     * and does not change the accumulator.
     */
    inner class LSRA(): Instruction() {
        override fun run(targetAddress: UShort){
            val data: UInt = this@CPU.accumulator.toUInt()
            val result: UByte = (data shr 1).toUByte()
            this@CPU.accumulator = result

            this@CPU.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU.zeroFlag = result == (0x00u).toUByte()
            this@CPU.negativeFlag = false
        }
    }

    /**
     * Logical Shift Right
     * This instruction shifts either the accumulator or a specified memory location 1 bit to the right, with the
     * higher bit of the result always being set to 0, and the low bit which is shifted out of the field being
     * stored in the carry flag.
     *
     * The shift right does not affect the overflow flag.
     * The N flag is always reset.
     * The Z flag is set if the result of the shift is 0 and reset otherwise.
     * The carry is set equal to bit 0 of the input.
     */
    inner class LSR(): Instruction() {
        override fun run(targetAddress: UShort){
            val data: UInt = this@CPU.bus.readAddress(targetAddress).toUInt()
            val result: UByte = (data shr 1).toUByte()
            this@CPU.bus.writeToAddress(targetAddress, result)

            this@CPU.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU.zeroFlag = result == (0x00u).toUByte()
            this@CPU.negativeFlag = false
        }
    }

    /**
     * No Operation
     */
    inner class NOP(): Instruction() {
        override fun run(targetAddress: UShort) {
            return
        }
    }

    /**
     * OR Memory with Accumulator
     * performs a binary OR on a bit-by-bit basis and stores the result in the accumulator.
     * Negative flag toggled by bit 7 of result.
     * Zero flag toggled by result.
     */
    inner class ORA(): Instruction() {
        override fun run(targetAddress: UShort) {
            val operand: UByte = this@CPU.bus.readAddress(targetAddress)
            val result: UByte = this@CPU.accumulator or operand
            this@CPU.accumulator = result

            this@CPU.negativeFlag = (result.toUInt() shr 7) == 1u
            this@CPU.zeroFlag = result == (0x00u).toUByte()
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

    /**
     * Rotate Left on Accumulator
     * The rotate left instruction shifts either the accumulator or addressed memory left 1 bit, with the input carry
     * being stored in bit 0 and with the input bit 7 being stored in the carry flags.
     *
     * The ROL instruction sets carry equal to the input bit 7,
     * sets N equal to the input bit 6 ,
     * sets the Z flag if the result is 0.
     * Note: I had to separate the ROL command into two implementations because passing the accumulator creates a copy,
     * and does not change the accumulator.
     */
    inner class ROLA(): Instruction() {
        override fun run(targetAddress: UShort) {
            val data: UInt = this@CPU.accumulator.toUInt()
            val result: UByte = if (carryFlag) ((data shl 1) or (1u)).toUByte() else (data shl 1).toUByte()
            this@CPU.accumulator = result

            this@CPU.carryFlag = (data shr 7).toUByte() == (1u).toUByte()
            this@CPU.zeroFlag = result == (0x00u).toUByte()
            this@CPU.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()
        }
    }

    /**
     * Rotate Left on Accumulator
     * The rotate left instruction shifts either the accumulator or addressed memory left 1 bit, with the input carry
     * being stored in bit 0 and with the input bit 7 being stored in the carry flags.
     *
     * The ROL instruction sets carry equal to the input bit 7,
     * sets N equal to the input bit 6 ,
     * sets the Z flag if the result is 0.
     */
    inner class ROL(): Instruction() {
        override fun run(targetAddress: UShort) {
            val data: UInt = this@CPU.bus.readAddress(targetAddress).toUInt()
            val result: UByte = if (carryFlag) ((data shl 1) or (1u)).toUByte() else (data shl 1).toUByte()
            this@CPU.bus.writeToAddress(targetAddress, result)

            this@CPU.carryFlag = (data shr 7).toUByte() == (1u).toUByte()
            this@CPU.zeroFlag = result == (0x00u).toUByte()
            this@CPU.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()
        }
    }

    /**
     * Rotate Right on Accumulator
     * The rotate right instruction shifts either the accumulator or addressed memory right 1 bit with bit 0 shifted
     * into the carry and carry shifted into bit 7.
     * The ROR instruction sets carry equal to input bit 0,
     * sets N equal to the input carry
     * sets the Z flag if the result of the rotate is 0;
     * otherwise it resets Z and
     * does not affect the overflow flag at all.
     * Note: I had to separate the ROL command into two implementations because passing the accumulator creates a copy,
     * and does not change the accumulator.
     */
    inner class RORA(): Instruction() {
        override fun run(targetAddress: UShort) {
            val data: UInt = this@CPU.accumulator.toUInt()
            val result: UByte = if (carryFlag) ((data shr 1) or (0x80u)).toUByte() else (data shr 1).toUByte()
            this@CPU.accumulator = result

            this@CPU.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU.zeroFlag = result == (0x00u).toUByte()
            this@CPU.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()
        }
    }

    /**
     * Rotate Right
     * The rotate right instruction shifts either the accumulator or addressed memory right 1 bit with bit 0 shifted
     * into the carry and carry shifted into bit 7.
     * The ROR instruction sets carry equal to input bit 0,
     * sets N equal to the input carry
     * sets the Z flag if the result of the rotate is 0;
     * otherwise it resets Z and
     * does not affect the overflow flag at all.
     */
    inner class ROR(): Instruction() {
        override fun run(targetAddress: UShort) {
            val data: UInt = this@CPU.bus.readAddress(targetAddress).toUInt()
            val result: UByte = if (carryFlag) ((data shr 1) or (0x80u)).toUByte() else (data shr 1).toUByte()
            this@CPU.bus.writeToAddress(targetAddress, result)

            this@CPU.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU.zeroFlag = result == (0x00u).toUByte()
            this@CPU.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()
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

    /**
     * Transfer Accumulator to X register.
     * This instruction takes the value from the accumulator and loads it to register X
     * without disturbing the content of the accumulator A.
     *
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class TAX(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.xRegister = this@CPU.accumulator
            this@CPU.zeroFlag = this@CPU.xRegister == (0x00u).toUByte()
            this@CPU.negativeFlag = (this@CPU.xRegister.toUInt() shr 7) == 1u
        }
    }

    /**
     * Transfer Accumulator to Y register.
     * This instruction takes the value from the accumulator and loads it to register Y
     * without disturbing the content of the accumulator A.
     *
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class TAY(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.yRegister = this@CPU.accumulator
            this@CPU.zeroFlag = this@CPU.yRegister == (0x00u).toUByte()
            this@CPU.negativeFlag = (this@CPU.yRegister.toUInt() shr 7) == 1u
        }
    }

    /**
     * Transfer Stack Pointer to X register.
     * This instruction takes the value from the stack pointer and loads it to register X
     * without disturbing the content of the stack pointer.
     *
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class TSX(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.xRegister = this@CPU.stackPointer
            this@CPU.zeroFlag = this@CPU.xRegister == (0x00u).toUByte()
            this@CPU.negativeFlag = (this@CPU.xRegister.toUInt() shr 7) == 1u
        }
    }


    /**
     * Transfer X register to Accumulator.
     * This instruction takes the value from the x register and loads it into the accumulator
     * without disturbing the content of the x register.
     *
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class TXA(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.accumulator = this@CPU.xRegister
            this@CPU.zeroFlag = this@CPU.accumulator == (0x00u).toUByte()
            this@CPU.negativeFlag = (this@CPU.accumulator.toUInt() shr 7) == 1u
        }
    }

    /**
     * Transfer X register to Stack Pointer
     * This instruction transfers the value in the index register X to the stack pointer.
     * TXS changes only the stack pointer, making it equal to the content of the index
     * register X. It does not affect any of the flags.
     */
    inner class TXS(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.stackPointer = this@CPU.xRegister
        }
    }

    /**
     * Transfer Y register to Accumulator.
     * This instruction takes the value from the y register and loads it into the accumulator
     * without disturbing the content of the y register.
     *
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class TYA(): Instruction() {
        override fun run(targetAddress: UShort) {
            this@CPU.accumulator = this@CPU.yRegister
            this@CPU.zeroFlag = this@CPU.accumulator == (0x00u).toUByte()
            this@CPU.negativeFlag = (this@CPU.accumulator.toUInt() shr 7) == 1u
        }
    }




}