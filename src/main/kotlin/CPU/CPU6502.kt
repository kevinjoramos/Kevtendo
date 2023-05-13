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
class CPU6502 (val bus: Bus) {

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



    /**
     * Interrupt control signals.
     */
    fun irq() {}

    fun nmi() {}

    fun ready() {}

    fun run() {
        val opcode: UByte = bus.readAddress(programCounter)
        executeInstructions(opcode)
        programCounter++
    }

    /**
     * Three steps
     * 1. Decode byte to addressing mode
     * 2. fetch operands
     * 3. call the correct instruction.
     */
    private fun executeInstructions(opcode: UByte) {



    }

    val opcodeTable: Map<UByte, () -> Unit> = mapOf(
        (0x00u).toUByte() to { BRK().run() },
        (0x01u).toUByte() to { ORA().execute(xIndexedIndirectAddressing()) },
        (0x05u).toUByte() to { ORA().execute(zeroPageAddressing()) },
        (0x06u).toUByte() to { ASL().execute(zeroPageAddressing()) },
        (0x08u).toUByte() to { PHP().execute() },
        (0x09u).toUByte() to { ORA().execute(immediateAddressing()) },
        (0x0Au).toUByte() to { ASL().execute() },
        (0x0Du).toUByte() to { ORA().execute(absoluteAddressing()) },
        (0x0Eu).toUByte() to { ASL().execute(absoluteAddressing()) },

        (0x10u).toUByte() to { BPL().execute(relativeAddressing()) },
        (0x11u).toUByte() to { ORA().execute(indirectYIndexedAddressing()) },
        (0x15u).toUByte() to { ORA().execute(zeroPageXIndexedAddressing()) },
        (0x16u).toUByte() to { ASL().execute(zeroPageXIndexedAddressing()) },
        (0x18u).toUByte() to { CLC().execute() },
        (0x19u).toUByte() to { ORA().execute(absoluteYIndexedAddressing()) },
        (0x1Du).toUByte() to { ORA().execute(absoluteXIndexedAddressing()) },
        (0x1Eu).toUByte() to { ASL().execute(absoluteXIndexedAddressing()) },

        (0x20u).toUByte() to { JSR().execute(absoluteAddressing()) },
        (0x21u).toUByte() to { AND().execute(xIndexedIndirectAddressing()) },
        (0x24u).toUByte() to { BIT().execute(zeroPageAddressing()) },
        (0x25u).toUByte() to { AND().execute(zeroPageAddressing()) },
        (0x26u).toUByte() to { ROL().execute(zeroPageAddressing()) },
        (0x28u).toUByte() to { PLP().execute() },
        (0x29u).toUByte() to { AND().execute(immediateAddressing()) },
        (0x2Au).toUByte() to { ROL().execute() },
        (0x2Cu).toUByte() to { BIT().execute(absoluteAddressing()) },
        (0x2Du).toUByte() to { AND().execute(absoluteAddressing()) },
        (0x2Eu).toUByte() to { ROL().execute(absoluteAddressing()) },

        (0x30u).toUByte() to { BMI().execute(relativeAddressing()) },
        (0x31u).toUByte() to { AND().execute(indirectYIndexedAddressing()) },
        (0x35u).toUByte() to { AND().execute(zeroPageXIndexedAddressing()) },
        (0x36u).toUByte() to { ROL().execute(zeroPageXIndexedAddressing()) },
        (0x38u).toUByte() to { SEC().execute() },
        (0x39u).toUByte() to { AND().execute(absoluteYIndexedAddressing()) },
        (0x3Du).toUByte() to { AND().execute(absoluteXIndexedAddressing()) },
        (0x3Eu).toUByte() to { ROL().execute(absoluteXIndexedAddressing()) },

        (0x40u).toUByte() to { RTI().execute() },
        (0x41u).toUByte() to { EOR().execute(xIndexedIndirectAddressing()) },
        (0x45u).toUByte() to { EOR().execute(zeroPageAddressing()) },
        (0x46u).toUByte() to { LSR().execute(zeroPageAddressing()) },
        (0x48u).toUByte() to { PHA().execute() },
        (0x49u).toUByte() to { EOR().execute(immediateAddressing()) },
        (0x4Au).toUByte() to { LSR().execute() },
        (0x4Cu).toUByte() to { JMP().execute(absoluteAddressing()) },
        (0x4Du).toUByte() to { EOR().execute(absoluteAddressing()) },
        (0x4Eu).toUByte() to { LSR().execute(absoluteAddressing()) },

        (0x50u).toUByte() to { BVC().execute(relativeAddressing()) },
        (0x51u).toUByte() to { EOR().execute(indirectYIndexedAddressing()) },
        (0x55u).toUByte() to { EOR().execute(zeroPageXIndexedAddressing()) },
        (0x56u).toUByte() to { LSR().execute(zeroPageXIndexedAddressing()) },
        (0x58u).toUByte() to { CLI().execute() },
        (0x59u).toUByte() to { EOR().execute(absoluteYIndexedAddressing()) },
        (0x5Du).toUByte() to { EOR().execute(absoluteXIndexedAddressing()) },
        (0x5Eu).toUByte() to { LSR().execute(absoluteYIndexedAddressing()) },

        (0x60u).toUByte() to { RTS().execute() },


        (0x70u).toUByte() to { BVS().execute(relativeAddressing()) },


        (0x90u).toUByte() to { BCC().execute(relativeAddressing()) },


        (0xA0u).toUByte() to { LDY().execute(immediateAddressing()) },

        (0xB0u).toUByte() to { BCS().execute(relativeAddressing()) },

        (0xC0u).toUByte() to { CPY().execute(immediateAddressing()) },

        (0xD0u).toUByte() to { BNE().execute(relativeAddressing()) },

        (0xE0u).toUByte() to { CPX().execute(immediateAddressing()) },

        (0xF0u).toUByte() to { BEQ().execute(relativeAddressing()) },










    )

    fun impliedAddressing() {}

    fun immediateAddressing(): UByte { TODO("not_implemented") }

    fun absoluteAddressing(): UShort { TODO("not_implemented") }

    fun zeroPageAddressing(): UShort { TODO("not_implemented") } //zero page {}

    fun absoluteXIndexedAddressing(): UShort { TODO("not_implemented") }

    fun absoluteYIndexedAddressing(): UShort { TODO("not_implemented") }

    fun zeroPageXIndexedAddressing(): UShort { TODO("not_implemented") } //zero page {}

    fun zeroPageYIndexedAddressing(): UShort { TODO("not_implemented") } //zero page {}

    fun indirectAddressing(): UShort { TODO("not_implemented") }

    fun xIndexedIndirectAddressing(): UShort { TODO("not_implemented") } //zero page

    fun indirectYIndexedAddressing(): UShort { TODO("not_implemented") } //zero page

    fun relativeAddressing(): UShort {TODO("not_implemented")}



   /* *//**
     * Addressing modes
     * *implicit mode not included since its target it inferred from instruction.
     * *accumulator mode not included since a kotlin function would only return a copy
     * of the accumulators value.
     *//*
    fun implicitAddressingMode(): UShort {
        return 0u
    }

    fun accumulatorAddressingMode(): UShort {
        return 0u
    }

    *//**
     * "Immediate addressing allows the programmer to directly specify an 8 bit constant within the instruction."
     * returns byte from next instruction address.
     *//*
    fun immediateAddressingMode(): UByte {
        programCounter++
        return bus.readAddress(programCounter)
    }

    *//**
     * Zero page addressing allows for efficient access to the first 256 bytes of memory.
     * referenced by one byte.
     * Returns address of specified zero-page address.
     *//*
    fun zeroPageAddressingMode(): UShort {
        programCounter++
        return bus.readAddress(programCounter).toUShort()
    }

    *//**
     * Zero page X addressing allows for the xRegister offset to be added to the operand.
     * Returns address of specified zero-page address.
     *//*
    fun zeroPageXAddressingMode(): UShort {
        programCounter++
        val operand: UByte = bus.readAddress(programCounter)
        val targetAddress: UByte = (operand + xRegister).toUByte()
        return targetAddress.toUShort()
    }

    *//**
     * Zero page Y addressing allows for the yRegister offset to be added to the operand.
     * Returns address of specified zero-page address.
     *//*
    fun zeroPageYAddressingMode(): UShort {
        programCounter++
        val operand: UByte = bus.readAddress(programCounter)
        val targetAddress: UByte = (operand + yRegister).toUByte()
        return targetAddress.toUShort()
    }

    *//**
     * Relative addressing adds the operand(offset) to the program counter to return the target address
     * in memory.
     *//*
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

    *//**
     * In inddireactIndexed addressing the operand is a zero page address whose contents are added with carry (C) to the Y register
     * $aa + Y (C), the LSB result contains the LSB of the EA.
     * The contents of address (operand + $01 + C) contain the MSB of the EA.
     *//*
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

*/
    /**
     * Op codes
     * I chose to implement the opcodes as
     */

    inner class ADC(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }

        fun run(operand: UByte) {
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
        fun execute(operand: UByte) {
            TODO("Not implemented")
        }

        fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.bus.readAddress(targetAddress)
            val result: UByte = this@CPU6502.accumulator and operand
            this@CPU6502.accumulator = result

            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
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
        fun execute() {
            val data: UInt = this@CPU6502.accumulator.toUInt()
            val result: UByte = (data shl 1).toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (data shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
        }
        fun execute(targetAddress: UShort) {
            val data: UInt = this@CPU6502.bus.readAddress(targetAddress).toUInt()
            val result: UByte = (data shl 1).toUByte()
            this@CPU6502.bus.writeToAddress(targetAddress, result)

            this@CPU6502.carryFlag = (data shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
        }
    }

    inner class BCC(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class BCS(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class BEQ(): Instruction() {
        fun execute(targetAddress: UShort) {
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
        fun execute(targetAddress: UShort) {
            val operand: UInt = bus.readAddress(targetAddress).toUInt()
            val result: UByte = this@CPU6502.accumulator and operand.toUByte()

            this@CPU6502.negativeFlag = (operand shr 7) == 1u
            this@CPU6502.overflowFlag = (operand and 0x40u) != 0u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
        }
    }

    inner class BMI(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class BNE(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class BPL(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class BRK(): Instruction() {
        fun run() {
            TODO("Not yet implemented")
        }
    }

    inner class BVC(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class BVS(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    /**
     * Clear Carry Flag
     * This instruction initializes the carry flag to a 0.
     * This instruction affects no registers in the microprocessor and no flags other than the carry flag which is reset.
     */
    inner class CLC(): Instruction() {
        fun execute() {
            this@CPU6502.carryFlag = false
        }
    }

    /**
     * Clear Decimal Mode
     * This instruction sets the decimal mode flag to a 0.
     * CLD affects no registers in the microprocessor and no flags other than the decimal mode flag which is set to a 0.
     */
    inner class CLD(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.decimalFlag = false
        }
    }

    /**
     * Clear Interrupt Disable
     * This instruction initializes the interrupt disable to a 0. This allows the microprocessor to receive interrupts.
     * It affects no registers in the microprocessor and no flags other than the interrupt disable which is cleared.
     */
    inner class CLI(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.interruptDisableFlag = false
        }
    }


    /**
     * Clear Overflow Flag
     * This instruction clears the overflow flag to a 0.
     * CLV affects no registers in the microprocessor and no flags other than the overflow flag which is set to a 0.
     */
    inner class CLV(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.overflowFlag = false
        }
    }

    inner class CMP(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class CPX(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class CPY(): Instruction() {
        fun execute(operand: UByte) {

        }
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class DEC(): Instruction() {
        fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.bus.readAddress(targetAddress)
            val result = operand.dec()
            this@CPU6502.bus.writeToAddress(targetAddress, result)

            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
        }
    }

    inner class DEX(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.xRegister--

            this@CPU6502.zeroFlag = this@CPU6502.xRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.xRegister.toUInt() shr 7) == 1u
        }
    }

    inner class DEY(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.yRegister--

            this@CPU6502.zeroFlag = this@CPU6502.yRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.yRegister.toUInt() shr 7) == 1u
        }
    }

    /**
     * Exclusive OR Memory with Accumulator
     * performs a binary "EXCLUSIVE OR" on a bit-by-bit basis and stores the result in the accumulator.
     * Negative flag toggled by bit 7 of result.
     * Zero flag toggled by result.
     */
    inner class EOR(): Instruction() {
        fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.bus.readAddress(targetAddress)
            val result: UByte = this@CPU6502.accumulator xor operand
            this@CPU6502.accumulator = result

            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
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
        fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.bus.readAddress(targetAddress)
            val result = operand.inc()
            this@CPU6502.bus.writeToAddress(targetAddress, result)

            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
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
        fun execute(targetAddress: UShort) {
            this@CPU6502.xRegister++

            this@CPU6502.zeroFlag = this@CPU6502.xRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.xRegister.toUInt() shr 7) == 1u
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
        fun execute(targetAddress: UShort) {
            this@CPU6502.yRegister++

            this@CPU6502.zeroFlag = this@CPU6502.yRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.yRegister.toUInt() shr 7) == 1u
        }
    }

    inner class JMP(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    inner class JSR(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    /**
     * Load Accumulator
     * Load the accumulator from memory
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class LDA(): Instruction() {
        fun execute(targetAddress: UShort) {
            val data: UByte = bus.readAddress(targetAddress)
            this@CPU6502.accumulator = data

            this@CPU6502.zeroFlag = data == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (data.toUInt() shr 7) == 1u
        }
    }

    /**
     * Load X Register
     * Load the index register X from memory.
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class LDX(): Instruction() {
        fun execute(targetAddress: UShort) {
            val data: UByte = bus.readAddress(targetAddress)
            this@CPU6502.xRegister = data

            this@CPU6502.zeroFlag = data == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (data.toUInt() shr 7) == 1u
        }
    }

    /**
     * Load Y Register
     * Load the index register Y from memory.
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class LDY(): Instruction() {
        fun execute(operand: UByte) {

        }

        fun execute(targetAddress: UShort) {
            val data: UByte = bus.readAddress(targetAddress)
            this@CPU6502.yRegister = data

            this@CPU6502.zeroFlag = data == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (data.toUInt() shr 7) == 1u
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
        fun execute(targetAddress: UShort){
            val data: UInt = this@CPU6502.accumulator.toUInt()
            val result: UByte = (data shr 1).toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = false
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
        fun execute(targetAddress: UShort){
            val data: UInt = this@CPU6502.bus.readAddress(targetAddress).toUInt()
            val result: UByte = (data shr 1).toUByte()
            this@CPU6502.bus.writeToAddress(targetAddress, result)

            this@CPU6502.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = false
        }
    }

    /**
     * No Operation
     */
    inner class NOP(): Instruction() {
        fun execute(targetAddress: UShort) {
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
        fun execute(operand: UByte) {

        }

        fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.bus.readAddress(targetAddress)
            val result: UByte = this@CPU6502.accumulator or operand
            this@CPU6502.accumulator = result

            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
        }
    }

    /**
     * Push Accumulator On Stack
     * copies the current value of the accumulator into the memory location the stack register points to.
     * decrements the stack pointer value
     * does not affect any flags or registers.
     */
    inner class PHA(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.bus.writeToAddress(stackPointer.toUShort(), this@CPU6502.accumulator)
            this@CPU6502.stackPointer--
        }
    }

    /**
     * Push Processor Status on Stack
     * copies the current value of the status register into the memory location the stack register points to.
     * decrements the stack pointer value
     * does not affect any flags or registers.
     * NOTE: I implemented the stack register as booleans for the individual bits. I used bit masking here
     * to create the register value.
     */
    inner class PHP(): Instruction() {
        fun execute() {
            var result: UByte = 0u
            val negativeBitMask: UByte = 0x80u
            val overflowBitMask: UByte = 0x40u
            val extraBitMask: UByte = 0x20u
            val breakBitMask: UByte = 0x10u
            val decimalBitMask: UByte = 0x08u
            val interruptDisableBitMask: UByte = 0x04u
            val zeroBitMask: UByte = 0x02u
            val carryBitMask: UByte = 0x01u

            if (this@CPU6502.negativeFlag) {
                result = result or negativeBitMask
            }

            if (this@CPU6502.overflowFlag) {
                result = result or overflowBitMask
            }

            if (this@CPU6502.extraFlag) {
                result = result or extraBitMask
            }

            if (this@CPU6502.breakFlag) {
                result = result or breakBitMask
            }

            if (this@CPU6502.decimalFlag) {
                result = result or decimalBitMask
            }

            if (this@CPU6502.interruptDisableFlag) {
                result = result or interruptDisableBitMask
            }

            if (this@CPU6502.zeroFlag) {
                result = result or zeroBitMask
            }

            if (this@CPU6502.carryFlag) {
                result = result or carryBitMask
            }

            this@CPU6502.bus.writeToAddress(this@CPU6502.stackPointer.toUShort(), result)
            this@CPU6502.stackPointer--
        }
    }

    /**
     * Pull Accumulator from Stack
     * increments the stack pointer, and copies the value in that location to accumulator.
     * -bit 7 of the result toggles negative flag.
     * -toggles zero flag if result is 0.
     */
    inner class PLA(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.stackPointer++
            val data: UByte = this@CPU6502.bus.readAddress(stackPointer.toUShort())
            this@CPU6502.accumulator = data

            this@CPU6502.negativeFlag = (data.toUInt() shr 7) == 1u
            this@CPU6502.zeroFlag = data == (0x00u).toUByte()
        }
    }

    /**
     * Pull Processor Status from Stack
     * increments the stack pointer, and copies the value in that address to the status register.
     */
    inner class PLP(): Instruction() {
        fun execute() {
            this@CPU6502.stackPointer++
            val data: UByte = this@CPU6502.bus.readAddress(stackPointer.toUShort())

            val negativeBitMask: UByte = 0x80u
            val overflowBitMask: UByte = 0x40u
            val extraBitMask: UByte = 0x20u
            val breakBitMask: UByte = 0x10u
            val decimalBitMask: UByte = 0x08u
            val interruptDisableBitMask: UByte = 0x04u
            val zeroBitMask: UByte = 0x02u
            val carryBitMask: UByte = 0x01u

            this@CPU6502.negativeFlag = data and negativeBitMask == negativeBitMask
            this@CPU6502.overflowFlag = data and overflowBitMask == overflowBitMask
            this@CPU6502.extraFlag = data and extraBitMask == extraBitMask
            this@CPU6502.breakFlag = data and breakBitMask == breakBitMask
            this@CPU6502.decimalFlag = data and decimalBitMask == decimalBitMask
            this@CPU6502.interruptDisableFlag = data and interruptDisableBitMask == interruptDisableBitMask
            this@CPU6502.zeroFlag = data and zeroBitMask == zeroBitMask
            this@CPU6502.carryFlag = data and carryBitMask == carryBitMask
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
        fun execute() {
            val data: UInt = this@CPU6502.accumulator.toUInt()
            val result: UByte = if (carryFlag) ((data shl 1) or (1u)).toUByte() else (data shl 1).toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (data shr 7).toUByte() == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()
        }

        fun execute(targetAddress: UShort) {
            val data: UInt = this@CPU6502.bus.readAddress(targetAddress).toUInt()
            val result: UByte = if (carryFlag) ((data shl 1) or (1u)).toUByte() else (data shl 1).toUByte()
            this@CPU6502.bus.writeToAddress(targetAddress, result)

            this@CPU6502.carryFlag = (data shr 7).toUByte() == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()
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
        fun execute(targetAddress: UShort) {
            val data: UInt = this@CPU6502.accumulator.toUInt()
            val result: UByte = if (carryFlag) ((data shr 1) or (0x80u)).toUByte() else (data shr 1).toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()
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
        fun execute(targetAddress: UShort) {
            val data: UInt = this@CPU6502.bus.readAddress(targetAddress).toUInt()
            val result: UByte = if (carryFlag) ((data shr 1) or (0x80u)).toUByte() else (data shr 1).toUByte()
            this@CPU6502.bus.writeToAddress(targetAddress, result)

            this@CPU6502.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()
        }
    }

    inner class RTI(): Instruction() {
        fun execute() {
            TODO("Not yet implemented")
        }
    }

    inner class RTS(): Instruction() {
        fun execute() {
            TODO("Not yet implemented")
        }
    }

    inner class SBC(): Instruction() {
        fun execute(targetAddress: UShort) {
            TODO("Not yet implemented")
        }
    }

    /**
     * Set Carry Flag
     * This instruction initializes the carry flag to a 1.
     * This instruction affects no registers in the microprocessor and no flags other than the carry flag which is set.
     */
    inner class SEC(): Instruction() {
        fun execute() {
            this@CPU6502.carryFlag = true
        }
    }

    /**
     * Set Decimal Flag
     * This instruction sets the decimal mode flag D to a 1.
     * SED affects no registers in the microprocessor and no flags other than the decimal mode which is set to a 1.
     */
    inner class SED(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.decimalFlag = true
        }
    }

    /**
     * Set Interrupt Disable
     * This instruction initializes the interrupt disable to a 1.
     * It is used to mask interrupt requests during system reset operations and during interrupt commands.
     * It affects no registers in the microprocessor and no flags other than the interrupt disable which is set.
     */
    inner class SEI(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.interruptDisableFlag = true
        }
    }

    /**
     * Store Accumulator In Memory
     * This instruction transfers the contents of the accumulator to memory.
     * This instruction affects none of the flags in the processor status register and does not affect the accumulator.
     */
    inner class STA(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.bus.writeToAddress(targetAddress, this@CPU6502.accumulator)
        }
    }

    /**
     * Store Register X in Memory
     * Transfers value of X register to addressed memory location.
     * No flags or registers in the microprocessor are affected by the store operation.
     */
    inner class STX(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.bus.writeToAddress(targetAddress, this@CPU6502.xRegister)
        }
    }

    /**
     * Store Register Y in Memory
     * Transfer the value of the Y register to the addressed memory location.
     * STY does not affect any flags or registers in the microprocessor.
     */
    inner class STY(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.bus.writeToAddress(targetAddress, this@CPU6502.yRegister)
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
        fun execute(targetAddress: UShort) {
            this@CPU6502.xRegister = this@CPU6502.accumulator
            this@CPU6502.zeroFlag = this@CPU6502.xRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.xRegister.toUInt() shr 7) == 1u
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
        fun execute(targetAddress: UShort) {
            this@CPU6502.yRegister = this@CPU6502.accumulator
            this@CPU6502.zeroFlag = this@CPU6502.yRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.yRegister.toUInt() shr 7) == 1u
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
        fun execute(targetAddress: UShort) {
            this@CPU6502.xRegister = this@CPU6502.stackPointer
            this@CPU6502.zeroFlag = this@CPU6502.xRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.xRegister.toUInt() shr 7) == 1u
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
        fun execute(targetAddress: UShort) {
            this@CPU6502.accumulator = this@CPU6502.xRegister
            this@CPU6502.zeroFlag = this@CPU6502.accumulator == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.accumulator.toUInt() shr 7) == 1u
        }
    }

    /**
     * Transfer X register to Stack Pointer
     * This instruction transfers the value in the index register X to the stack pointer.
     * TXS changes only the stack pointer, making it equal to the content of the index
     * register X. It does not affect any of the flags.
     */
    inner class TXS(): Instruction() {
        fun execute(targetAddress: UShort) {
            this@CPU6502.stackPointer = this@CPU6502.xRegister
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
        fun execute(targetAddress: UShort) {
            this@CPU6502.accumulator = this@CPU6502.yRegister
            this@CPU6502.zeroFlag = this@CPU6502.accumulator == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.accumulator.toUInt() shr 7) == 1u
        }
    }




}