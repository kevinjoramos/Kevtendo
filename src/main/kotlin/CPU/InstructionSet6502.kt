import CPU.AddressingMode
import CPU.AssemblyCode
import CPU.CPU6502

/**
 * Op codes
 * I chose to implement the opcodes as
 */

/**
 * Add to Accumulator With Carry
 * adds value to accumulator
 * sets the carry flag if value exceed 255
 * sets overflow flag if adding two positive numbers is negative.
 * sets overflow flag if adding two negative numbers is positive.
 * sets zero flag is result is 0.
 * sets negative flag if result has signed bit high.
 */
@ExperimentalUnsignedTypes
class ADC(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "ADC"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(operand: UByte) {
        val signBitMask: UByte = 0x80u
        val accumulatorSignedBit = cpuReference.accumulator and signBitMask == signBitMask
        val operandSignedBit = operand and signBitMask == signBitMask

        val rawResult = cpuReference.accumulator + operand + (if (cpuReference.carryFlag) 1u else 0u)
        val result = rawResult.toUByte()
        cpuReference.accumulator = result

        cpuReference.carryFlag = (rawResult shr 8) == 1u
        cpuReference.negativeFlag = result and signBitMask == signBitMask
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        if (accumulatorSignedBit != operandSignedBit) {
            cpuReference.overflowFlag = false
            cpuReference.programCounter++
            return
        }

        if (accumulatorSignedBit == cpuReference.negativeFlag) {
            cpuReference.overflowFlag = false
            cpuReference.programCounter++
            return
        }

        cpuReference.overflowFlag = true
        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val signBitMask: UByte = 0x80u
        val operand: UByte = cpuReference.readAddress(targetAddress)

        val accumulatorSignedBit = cpuReference.accumulator and signBitMask == signBitMask
        val operandSignedBit = operand and signBitMask == signBitMask

        val rawResult = cpuReference.accumulator + operand + (if (cpuReference.carryFlag) 1u else 0u)
        val result = rawResult.toUByte()
        cpuReference.accumulator = result

        cpuReference.carryFlag = (rawResult shr 8) == 1u
        cpuReference.negativeFlag = result and signBitMask == signBitMask
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        if (accumulatorSignedBit != operandSignedBit) {
            cpuReference.overflowFlag = false
            cpuReference.programCounter++
            return
        }

        if (accumulatorSignedBit == cpuReference.negativeFlag) {
            cpuReference.overflowFlag = false
            cpuReference.programCounter++
            return
        }

        cpuReference.overflowFlag = true

        cpuReference.programCounter++
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
@ExperimentalUnsignedTypes
class AND(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "AND"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(operand: UByte) {
        val result: UByte = cpuReference.accumulator and operand
        cpuReference.accumulator = result

        cpuReference.zeroFlag = result == (0x00u).toUByte()
        cpuReference.negativeFlag = (result.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val operand: UByte = cpuReference.readAddress(targetAddress)
        val result: UByte = cpuReference.accumulator and operand
        cpuReference.accumulator = result

        cpuReference.zeroFlag = result == (0x00u).toUByte()
        cpuReference.negativeFlag = (result.toUInt() shr 7) == 1u

        cpuReference.programCounter++
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
@ExperimentalUnsignedTypes
class ASL(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "ASL"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        val data: UInt = cpuReference.accumulator.toUInt()
        val result: UByte = (data shl 1).toUByte()
        cpuReference.accumulator = result

        cpuReference.carryFlag = (data shr 7) == 1u
        cpuReference.zeroFlag = result == (0x00u).toUByte()
        cpuReference.negativeFlag = (result.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val data: UInt = cpuReference.readAddress(targetAddress).toUInt()
        val result: UByte = (data shl 1).toUByte()
        cpuReference.writeToAddress(targetAddress, result)

        cpuReference.carryFlag = (data shr 7) == 1u
        cpuReference.zeroFlag = result == (0x00u).toUByte()
        cpuReference.negativeFlag = (result.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }
}

/**
 * Branch on Clear Carry
 * Sets program counter to target address when carryFlag = 0
 */
@ExperimentalUnsignedTypes
class BCC(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "BCC"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        if (!cpuReference.carryFlag) cpuReference.programCounter = targetAddress
        else cpuReference.programCounter++
    }
}

/**
 * Branch on Set Carry
 * Sets program counter to target address when carryFlag = 1
 */
@ExperimentalUnsignedTypes
class BCS(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "BCS"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        if (cpuReference.carryFlag) cpuReference.programCounter = targetAddress
        else cpuReference.programCounter++
    }
}

/**
 * Branch on Result Zero
 * Sets program counter to target address when zeroFlag = 1
 */
@ExperimentalUnsignedTypes
class BEQ(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "BEQ"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        if (cpuReference.zeroFlag) cpuReference.programCounter = targetAddress
        else cpuReference.programCounter++
    }
}

/**
 * Test Bits in Memory with Accumulator
 * Bit 7 of operand toggles the negative flag;
 * Bit 6 of perand toggles the overflow flag
 * the zero-flag is set to the result of operand AND accumulator.
 */
@ExperimentalUnsignedTypes
class BIT(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "BIT"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        val operand: UInt = cpuReference.readAddress(targetAddress).toUInt()
        val result: UByte = cpuReference.accumulator and operand.toUByte()

        cpuReference.negativeFlag = (operand shr 7) == 1u
        cpuReference.overflowFlag = (operand and 0x40u) != 0u
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        cpuReference.programCounter++
    }
}

/**
 * Branch on Result Minus
 * Sets program counter to target address when negativeFlag = 1
 */
@ExperimentalUnsignedTypes
class BMI(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "BMI"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        if (cpuReference.negativeFlag) cpuReference.programCounter = targetAddress
        else cpuReference.programCounter++
    }
}

/**
 * Branch on Result Not Zero
 * Sets program counter to target address when zeroFlag = 0
 */
@ExperimentalUnsignedTypes
class BNE(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "BNE"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        if (!cpuReference.zeroFlag) cpuReference.programCounter = targetAddress
        else cpuReference.programCounter++
    }
}

/**
 * Branch on Result Plus
 * Sets program counter to target address when negativeFlag = 0
 */
@ExperimentalUnsignedTypes
class BPL(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "BPL"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        if (!cpuReference.negativeFlag) cpuReference.programCounter = targetAddress
        else cpuReference.programCounter++
    }
}

/**
 * Break Command
 * performs a programed interrupt similar to IRQ
 */
@ExperimentalUnsignedTypes
class BRK(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "BRK"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        val vectorLeastSignificantByte = cpuReference.readAddress(0xFFFEu)
        val vectorMostSignificantByte = cpuReference.readAddress(0xFFFFu)

        cpuReference.programCounter++
        cpuReference.programCounter++

        cpuReference.writeToAddress(cpuReference.stackPointer.toUShort(), (cpuReference.programCounter.toInt() shr 8).toUByte())
        cpuReference.stackPointer--
        cpuReference.writeToAddress(cpuReference.stackPointer.toUShort(), cpuReference.programCounter.toUByte())
        cpuReference.stackPointer--

        var statusRegisterValue: UByte = 0u
        val negativeBitMask: UByte = 0x80u
        val overflowBitMask: UByte = 0x40u
        val extraBitMask: UByte = 0x20u
        val breakBitMask: UByte = 0x10u
        val decimalBitMask: UByte = 0x08u
        val interruptDisableBitMask: UByte = 0x04u
        val zeroBitMask: UByte = 0x02u
        val carryBitMask: UByte = 0x01u

        if (cpuReference.negativeFlag) statusRegisterValue = statusRegisterValue or negativeBitMask
        if (cpuReference.overflowFlag) statusRegisterValue = statusRegisterValue or overflowBitMask
        if (cpuReference.extraFlag) statusRegisterValue = statusRegisterValue or extraBitMask
        if (cpuReference.breakFlag) statusRegisterValue = statusRegisterValue or breakBitMask
        if (cpuReference.decimalFlag) statusRegisterValue = statusRegisterValue or decimalBitMask
        if (cpuReference.interruptDisableFlag) statusRegisterValue = statusRegisterValue or interruptDisableBitMask
        if (cpuReference.zeroFlag) statusRegisterValue = statusRegisterValue or zeroBitMask
        if (cpuReference.carryFlag) statusRegisterValue = statusRegisterValue or carryBitMask
        cpuReference.writeToAddress(cpuReference.stackPointer.toUShort(), statusRegisterValue)
        cpuReference.stackPointer--

        cpuReference.interruptDisableFlag = true

        cpuReference.programCounter = (((vectorMostSignificantByte.toUInt() shl 8) + vectorLeastSignificantByte).toUShort())
    }
}

/**
 * Branch on Overflow Clear
 * Sets program counter to target address when overFlow = 0
 */
@ExperimentalUnsignedTypes
class BVC(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "BVC"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        if (!cpuReference.overflowFlag) cpuReference.programCounter = targetAddress
        else cpuReference.programCounter++
    }
}

/**
 * Branch on Overflow Set
 * Sets program counter to target address when overFlow = 1
 */
@ExperimentalUnsignedTypes
class BVS(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "BVS"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        if (cpuReference.overflowFlag) cpuReference.programCounter = targetAddress
        else cpuReference.programCounter++
    }
}

/**
 * Clear Carry Flag
 * This instruction initializes the carry flag to a 0.
 * This instruction affects no registers in the microprocessor and no flags other than the carry flag which is reset.
 */
@ExperimentalUnsignedTypes
class CLC(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "CLC"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.carryFlag = false
        cpuReference.programCounter++
    }
}

/**
 * Clear Decimal Mode
 * This instruction sets the decimal mode flag to a 0.
 * CLD affects no registers in the microprocessor and no flags other than the decimal mode flag which is set to a 0.
 */
@ExperimentalUnsignedTypes
class CLD(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "CLD"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.decimalFlag = false
        cpuReference.programCounter++
    }
}

/**
 * Clear Interrupt Disable
 * This instruction initializes the interrupt disable to a 0. This allows the microprocessor to receive interrupts.
 * It affects no registers in the microprocessor and no flags other than the interrupt disable which is cleared.
 */
@ExperimentalUnsignedTypes
class CLI(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "CLI"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.interruptDisableFlag = false
        cpuReference.programCounter++
    }
}


/**
 * Clear Overflow Flag
 * This instruction clears the overflow flag to a 0.
 * CLV affects no registers in the microprocessor and no flags other than the overflow flag which is set to a 0.
 */
@ExperimentalUnsignedTypes
class CLV(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "CLV"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.overflowFlag = false
        cpuReference.programCounter++
    }
}

/**
 * Compare Memory and Accumulator
 * subtracts the value in memory from the accumulator without storing the result.
 * zero flag is set when operands are equal.
 * negative flag set if result is negative.
 * carry flag set if register >= operand
 */
@ExperimentalUnsignedTypes
class CMP(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "CMP"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(operand: UByte) {
        val signBitMask: UByte = 0x80u
        val rawResult = (cpuReference.accumulator.toByte() - operand.toByte()).toUInt()
        val result = rawResult.toUByte()

        cpuReference.carryFlag = cpuReference.accumulator >= operand
        cpuReference.negativeFlag = result and signBitMask == signBitMask
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val signBitMask: UByte = 0x80u
        val operand: UByte = cpuReference.readAddress(targetAddress)
        val rawResult = (cpuReference.accumulator.toByte() - operand.toByte()).toUInt()
        val result = rawResult.toUByte()

        cpuReference.carryFlag = cpuReference.accumulator >= operand
        cpuReference.negativeFlag = result and signBitMask == signBitMask
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        cpuReference.programCounter++
    }
}

/**
 * Compare Memory and X Register
 * subtracts the value in memory from the register without storing the result.
 * zero flag is set when operands are equal.
 * negative flag set if result is negative.
 * carry flag set if register >= operand
 */
@ExperimentalUnsignedTypes
class CPX(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "CPX"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(operand: UByte) {
        val signBitMask: UByte = 0x80u
        val rawResult = (cpuReference.xRegister.toByte() - operand.toByte()).toUInt()
        val result = rawResult.toUByte()

        cpuReference.carryFlag = cpuReference.xRegister >= operand
        cpuReference.negativeFlag = result and signBitMask == signBitMask
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val signBitMask: UByte = 0x80u
        val operand: UByte = cpuReference.readAddress(targetAddress)
        val rawResult = (cpuReference.xRegister.toByte() - operand.toByte()).toUInt()
        val result = rawResult.toUByte()

        cpuReference.carryFlag = cpuReference.xRegister >= operand
        cpuReference.negativeFlag = result and signBitMask == signBitMask
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        cpuReference.programCounter++
    }
}

/**
 * Compare Memory and Y Register
 * subtracts the value in memory from the register without storing the result.
 * zero flag is set when operands are equal.
 * negative flag set if result is negative.
 * carry flag set if register >= operand
 */
@ExperimentalUnsignedTypes
class CPY(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "CPY"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(operand: UByte) {
        val signBitMask: UByte = 0x80u
        val rawResult = (cpuReference.yRegister.toByte() - operand.toByte()).toUInt()
        val result = rawResult.toUByte()

        cpuReference.carryFlag = cpuReference.yRegister >= operand
        cpuReference.negativeFlag = result and signBitMask == signBitMask
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val signBitMask: UByte = 0x80u
        val operand: UByte = cpuReference.readAddress(targetAddress)
        val rawResult = (cpuReference.yRegister.toByte() - operand.toByte()).toUInt()
        val result = rawResult.toUByte()

        cpuReference.carryFlag = cpuReference.yRegister >= operand
        cpuReference.negativeFlag = result and signBitMask == signBitMask
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        cpuReference.programCounter++
    }
}

@ExperimentalUnsignedTypes
class DEC(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "DEC"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        val operand: UByte = cpuReference.readAddress(targetAddress)
        val result = operand.dec()
        cpuReference.writeToAddress(targetAddress, result)

        cpuReference.zeroFlag = result == (0x00u).toUByte()
        cpuReference.negativeFlag = (result.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }
}

@ExperimentalUnsignedTypes
class DEX(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "DEX"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.xRegister--

        cpuReference.zeroFlag = cpuReference.xRegister == (0x00u).toUByte()
        cpuReference.negativeFlag = (cpuReference.xRegister.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }
}

@ExperimentalUnsignedTypes
class DEY(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "DEY"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.yRegister--

        cpuReference.zeroFlag = cpuReference.yRegister == (0x00u).toUByte()
        cpuReference.negativeFlag = (cpuReference.yRegister.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }
}

/**
 * Exclusive OR Memory with Accumulator
 * performs a binary "EXCLUSIVE OR" on a bit-by-bit basis and stores the result in the accumulator.
 * Negative flag toggled by bit 7 of result.
 * Zero flag toggled by result.
 */
@ExperimentalUnsignedTypes
class EOR(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "EOR"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(operand: UByte) {
        val result: UByte = cpuReference.accumulator xor operand
        cpuReference.accumulator = result

        cpuReference.negativeFlag = (result.toUInt() shr 7) == 1u
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val operand: UByte = cpuReference.readAddress(targetAddress)
        val result: UByte = cpuReference.accumulator xor operand
        cpuReference.accumulator = result

        cpuReference.negativeFlag = (result.toUInt() shr 7) == 1u
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        cpuReference.programCounter++
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
@ExperimentalUnsignedTypes
class INC(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "INC"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        val operand: UByte = cpuReference.readAddress(targetAddress)
        val result = operand.inc()
        cpuReference.writeToAddress(targetAddress, result)

        cpuReference.zeroFlag = result == (0x00u).toUByte()
        cpuReference.negativeFlag = (result.toUInt() shr 7) == 1u

        cpuReference.programCounter++
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
@ExperimentalUnsignedTypes
class INX(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "INX"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.xRegister++

        cpuReference.zeroFlag = cpuReference.xRegister == (0x00u).toUByte()
        cpuReference.negativeFlag = (cpuReference.xRegister.toUInt() shr 7) == 1u

        cpuReference.programCounter++
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
@ExperimentalUnsignedTypes
class INY(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "INY"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.yRegister++

        cpuReference.zeroFlag = cpuReference.yRegister == (0x00u).toUByte()
        cpuReference.negativeFlag = (cpuReference.yRegister.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }
}

@ExperimentalUnsignedTypes
class JMP(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "JMP"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        cpuReference.programCounter = targetAddress
    }
}


/**
 * Jump to Subroutine
 * jumps program counter to target address, but first saves last address of current instruction to stack.
 * Decrements the stack twice in the process.
 */
@ExperimentalUnsignedTypes
class JSR(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "JSR"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        val currentAddressMostSignificantByte: UByte = (cpuReference.programCounter.toUInt() shr 8).toUByte()
        val currentAddressLeastSignificantByte: UByte = cpuReference.programCounter.toUByte()

        cpuReference.writeToAddress(cpuReference.stackPointer.toUShort(), currentAddressMostSignificantByte)
        cpuReference.stackPointer--
        cpuReference.writeToAddress(cpuReference.stackPointer.toUShort(), currentAddressLeastSignificantByte)
        cpuReference.stackPointer--

        cpuReference.programCounter = targetAddress
    }
}

/**
 * Load Accumulator
 * Load the accumulator from memory
 * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
 */
@ExperimentalUnsignedTypes
class LDA(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "LDA"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(operand: UByte) {
        cpuReference.accumulator = operand

        cpuReference.zeroFlag = operand == (0x00u).toUByte()
        cpuReference.negativeFlag = (operand.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val data: UByte = cpuReference.readAddress(targetAddress)
        cpuReference.accumulator = data

        cpuReference.zeroFlag = data == (0x00u).toUByte()
        cpuReference.negativeFlag = (data.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }
}

/**
 * Load X Register
 * Load the index register X from memory.
 * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
 */
@ExperimentalUnsignedTypes
class LDX(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "LDX"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(operand: UByte) {
        cpuReference.xRegister = operand

        cpuReference.zeroFlag = operand == (0x00u).toUByte()
        cpuReference.negativeFlag = (operand.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val data: UByte = cpuReference.readAddress(targetAddress)
        cpuReference.xRegister = data

        cpuReference.zeroFlag = data == (0x00u).toUByte()
        cpuReference.negativeFlag = (data.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }
}

/**
 * Load Y Register
 * Load the index register Y from memory.
 * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
 */
@ExperimentalUnsignedTypes
class LDY(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "LDY"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(operand: UByte) {
        cpuReference.yRegister = operand

        cpuReference.zeroFlag = operand == (0x00u).toUByte()
        cpuReference.negativeFlag = (operand.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val data: UByte = cpuReference.readAddress(targetAddress)
        cpuReference.yRegister = data

        cpuReference.zeroFlag = data == (0x00u).toUByte()
        cpuReference.negativeFlag = (data.toUInt() shr 7) == 1u

        cpuReference.programCounter++
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
@ExperimentalUnsignedTypes
class LSR(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "LSR"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        val data: UInt = cpuReference.accumulator.toUInt()
        val result: UByte = (data shr 1).toUByte()
        cpuReference.accumulator = result

        cpuReference.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
        cpuReference.zeroFlag = result == (0x00u).toUByte()
        cpuReference.negativeFlag = false

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort){
        val data: UInt = cpuReference.readAddress(targetAddress).toUInt()
        val result: UByte = (data shr 1).toUByte()
        cpuReference.writeToAddress(targetAddress, result)

        cpuReference.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
        cpuReference.zeroFlag = result == (0x00u).toUByte()
        cpuReference.negativeFlag = false

        cpuReference.programCounter++
    }
}

/**
 * No Operation
 */
@ExperimentalUnsignedTypes
class NOP(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "NOP"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.programCounter++
    }
}

/**
 * OR Memory with Accumulator
 * performs a binary OR on a bit-by-bit basis and stores the result in the accumulator.
 * Negative flag toggled by bit 7 of result.
 * Zero flag toggled by result.
 */
@ExperimentalUnsignedTypes
class ORA(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "ORA"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(operand: UByte) {
        val result: UByte = cpuReference.accumulator or operand
        cpuReference.accumulator = result

        cpuReference.negativeFlag = (result.toUInt() shr 7) == 1u
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val operand: UByte = cpuReference.readAddress(targetAddress)
        val result: UByte = cpuReference.accumulator or operand
        cpuReference.accumulator = result

        cpuReference.negativeFlag = (result.toUInt() shr 7) == 1u
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        cpuReference.programCounter++
    }
}

/**
 * Push Accumulator On Stack
 * copies the current value of the accumulator into the memory location the stack register points to.
 * decrements the stack pointer value
 * does not affect any flags or registers.
 */
@ExperimentalUnsignedTypes
class PHA(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "PHA"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.writeToAddress(cpuReference.stackPointer.toUShort(), cpuReference.accumulator)
        cpuReference.stackPointer--

        cpuReference.programCounter++
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
@ExperimentalUnsignedTypes
class PHP(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "PHP"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        var result: UByte = 0u
        val negativeBitMask: UByte = 0x80u
        val overflowBitMask: UByte = 0x40u
        val extraBitMask: UByte = 0x20u
        val breakBitMask: UByte = 0x10u
        val decimalBitMask: UByte = 0x08u
        val interruptDisableBitMask: UByte = 0x04u
        val zeroBitMask: UByte = 0x02u
        val carryBitMask: UByte = 0x01u

        if (cpuReference.negativeFlag) {
            result = result or negativeBitMask
        }

        if (cpuReference.overflowFlag) {
            result = result or overflowBitMask
        }

        if (cpuReference.extraFlag) {
            result = result or extraBitMask
        }

        if (cpuReference.breakFlag) {
            result = result or breakBitMask
        }

        if (cpuReference.decimalFlag) {
            result = result or decimalBitMask
        }

        if (cpuReference.interruptDisableFlag) {
            result = result or interruptDisableBitMask
        }

        if (cpuReference.zeroFlag) {
            result = result or zeroBitMask
        }

        if (cpuReference.carryFlag) {
            result = result or carryBitMask
        }

        cpuReference.writeToAddress(cpuReference.stackPointer.toUShort(), result)
        cpuReference.stackPointer--

        cpuReference.programCounter++
    }
}

/**
 * Pull Accumulator from Stack
 * increments the stack pointer, and copies the value in that location to accumulator.
 * -bit 7 of the result toggles negative flag.
 * -toggles zero flag if result is 0.
 */
@ExperimentalUnsignedTypes
class PLA(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "PLA"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.stackPointer++
        val data: UByte = cpuReference.readAddress(cpuReference.stackPointer.toUShort())
        cpuReference.accumulator = data

        cpuReference.negativeFlag = (data.toUInt() shr 7) == 1u
        cpuReference.zeroFlag = data == (0x00u).toUByte()

        cpuReference.programCounter++
    }
}

/**
 * Pull Processor Status from Stack
 * increments the stack pointer, and copies the value in that address to the status register.
 */
@ExperimentalUnsignedTypes
class PLP(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "PLP"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.stackPointer++
        val data: UByte = cpuReference.readAddress(cpuReference.stackPointer.toUShort())

        val negativeBitMask: UByte = 0x80u
        val overflowBitMask: UByte = 0x40u
        val extraBitMask: UByte = 0x20u
        val breakBitMask: UByte = 0x10u
        val decimalBitMask: UByte = 0x08u
        val interruptDisableBitMask: UByte = 0x04u
        val zeroBitMask: UByte = 0x02u
        val carryBitMask: UByte = 0x01u

        cpuReference.negativeFlag = data and negativeBitMask == negativeBitMask
        cpuReference.overflowFlag = data and overflowBitMask == overflowBitMask
        cpuReference.extraFlag = data and extraBitMask == extraBitMask
        cpuReference.breakFlag = data and breakBitMask == breakBitMask
        cpuReference.decimalFlag = data and decimalBitMask == decimalBitMask
        cpuReference.interruptDisableFlag = data and interruptDisableBitMask == interruptDisableBitMask
        cpuReference.zeroFlag = data and zeroBitMask == zeroBitMask
        cpuReference.carryFlag = data and carryBitMask == carryBitMask

        cpuReference.programCounter++
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
@ExperimentalUnsignedTypes
class ROL(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "ROL"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        val data: UInt = cpuReference.accumulator.toUInt()
        val result: UByte = if (cpuReference.carryFlag) ((data shl 1) or (1u)).toUByte() else (data shl 1).toUByte()
        cpuReference.accumulator = result

        cpuReference.carryFlag = (data shr 7).toUByte() == (1u).toUByte()
        cpuReference.zeroFlag = result == (0x00u).toUByte()
        cpuReference.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val data: UInt = cpuReference.readAddress(targetAddress).toUInt()
        val result: UByte = if (cpuReference.carryFlag) ((data shl 1) or (1u)).toUByte() else (data shl 1).toUByte()
        cpuReference.writeToAddress(targetAddress, result)

        cpuReference.carryFlag = (data shr 7).toUByte() == (1u).toUByte()
        cpuReference.zeroFlag = result == (0x00u).toUByte()
        cpuReference.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()

        cpuReference.programCounter++
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
@ExperimentalUnsignedTypes
class ROR(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "ROR"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        val data: UInt = cpuReference.accumulator.toUInt()
        val result: UByte = if (cpuReference.carryFlag) ((data shr 1) or (0x80u)).toUByte() else (data shr 1).toUByte()
        cpuReference.accumulator = result

        cpuReference.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
        cpuReference.zeroFlag = result == (0x00u).toUByte()
        cpuReference.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val data: UInt = cpuReference.readAddress(targetAddress).toUInt()
        val result: UByte = if (cpuReference.carryFlag) ((data shr 1) or (0x80u)).toUByte() else (data shr 1).toUByte()
        cpuReference.writeToAddress(targetAddress, result)

        cpuReference.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
        cpuReference.zeroFlag = result == (0x00u).toUByte()
        cpuReference.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()

        cpuReference.programCounter++
    }
}

/**
 * Return From Interrupt
 * restores program counter and status register from stack.
 */
@ExperimentalUnsignedTypes
class RTI(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "RTI"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.stackPointer++
        val statusRegisterValue = cpuReference.readAddress(cpuReference.stackPointer.toUShort())
        cpuReference.stackPointer++
        val targetLeastSignificantByte = cpuReference.readAddress(cpuReference.stackPointer.toUShort())
        cpuReference.stackPointer++
        val targetMostSignificantByte = cpuReference.readAddress(cpuReference.stackPointer.toUShort())

        val negativeBitMask: UByte = 0x80u
        val overflowBitMask: UByte = 0x40u
        val extraBitMask: UByte = 0x20u
        val breakBitMask: UByte = 0x10u
        val decimalBitMask: UByte = 0x08u
        val interruptDisableBitMask: UByte = 0x04u
        val zeroBitMask: UByte = 0x02u
        val carryBitMask: UByte = 0x01u

        cpuReference.negativeFlag = statusRegisterValue and negativeBitMask == negativeBitMask
        cpuReference.overflowFlag = statusRegisterValue and overflowBitMask == overflowBitMask
        cpuReference.extraFlag = statusRegisterValue and extraBitMask == extraBitMask
        cpuReference.breakFlag = statusRegisterValue and breakBitMask == breakBitMask
        cpuReference.decimalFlag = statusRegisterValue and decimalBitMask == decimalBitMask
        cpuReference.interruptDisableFlag = statusRegisterValue and interruptDisableBitMask == interruptDisableBitMask
        cpuReference.zeroFlag = statusRegisterValue and zeroBitMask == zeroBitMask
        cpuReference.carryFlag = statusRegisterValue and carryBitMask == carryBitMask

        cpuReference.programCounter = ((targetMostSignificantByte.toUInt() shl 8) + targetLeastSignificantByte).toUShort()
    }
}

/**
 * Return From Subroutine
 * restores program counter from stack.
 */
@ExperimentalUnsignedTypes
class RTS(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "RTS"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.stackPointer++
        val targetLeastSignificantByte = cpuReference.readAddress(cpuReference.stackPointer.toUShort())
        cpuReference.stackPointer++
        val targetMostSignificantByte = cpuReference.readAddress(cpuReference.stackPointer.toUShort())

        cpuReference.programCounter = ((targetMostSignificantByte.toUInt() shl 8) + targetLeastSignificantByte).toUShort()
        cpuReference.programCounter++
    }
}

/**
 * Subtract with Carry
 * Subtract accumulator with memory and carry-bit complement.
 * negativeFlag and zeroFlag are set by the result.
 * carry is called when unsigned values overflow 255.
 * overflow only can occur when subtracting positive from negative and visa versa.
 */
@ExperimentalUnsignedTypes
class SBC(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "SBC"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(operand: UByte) {
        val signBitMask: UByte = 0x80u
        val accumulatorSignedBit = cpuReference.accumulator and signBitMask == signBitMask
        val operandSignedBit = operand and signBitMask == signBitMask

        val rawResult = cpuReference.accumulator - operand - (if (cpuReference.carryFlag) 0u else 1u)
        val result = rawResult.toUByte()
        cpuReference.accumulator = result

        cpuReference.carryFlag = (rawResult shr 8) == 1u
        cpuReference.negativeFlag = result and signBitMask == signBitMask
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        if (accumulatorSignedBit == operandSignedBit) {
            cpuReference.overflowFlag = false
            cpuReference.programCounter++
            return
        }

        if (accumulatorSignedBit != cpuReference.negativeFlag) {
            cpuReference.overflowFlag = true
            cpuReference.programCounter++
            return
        }

        cpuReference.overflowFlag = false

        cpuReference.programCounter++
    }

    override fun execute(targetAddress: UShort) {
        val operand = cpuReference.readAddress(targetAddress)
        val signBitMask: UByte = 0x80u
        val accumulatorSignedBit = cpuReference.accumulator and signBitMask == signBitMask
        val operandSignedBit = operand and signBitMask == signBitMask

        val rawResult = cpuReference.accumulator - operand - (if (cpuReference.carryFlag) 0u else 1u)
        val result = rawResult.toUByte()
        cpuReference.accumulator = result

        cpuReference.carryFlag = (rawResult shr 8) == 1u
        cpuReference.negativeFlag = result and signBitMask == signBitMask
        cpuReference.zeroFlag = result == (0x00u).toUByte()

        if (accumulatorSignedBit == operandSignedBit) {
            cpuReference.overflowFlag = false
            cpuReference.programCounter++
            return
        }

        if (accumulatorSignedBit != cpuReference.negativeFlag) {
            cpuReference.overflowFlag = true
            cpuReference.programCounter++
            return
        }

        cpuReference.overflowFlag = false

        cpuReference.programCounter++
    }
}

/**
 * Set Carry Flag
 * This instruction initializes the carry flag to a 1.
 * This instruction affects no registers in the microprocessor and no flags other than the carry flag which is set.
 */
@ExperimentalUnsignedTypes
class SEC(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "SEC"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.carryFlag = true
        cpuReference.programCounter++
    }
}

/**
 * Set Decimal Flag
 * This instruction sets the decimal mode flag D to a 1.
 * SED affects no registers in the microprocessor and no flags other than the decimal mode which is set to a 1.
 */
@ExperimentalUnsignedTypes
class SED(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "SED"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.decimalFlag = true
        cpuReference.programCounter++
    }
}

/**
 * Set Interrupt Disable
 * This instruction initializes the interrupt disable to a 1.
 * It is used to mask interrupt requests during system reset operations and during interrupt commands.
 * It affects no registers in the microprocessor and no flags other than the interrupt disable which is set.
 */
@ExperimentalUnsignedTypes
class SEI(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "SEI"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.interruptDisableFlag = true
        cpuReference.programCounter++
    }
}

/**
 * Store Accumulator In Memory
 * This instruction transfers the contents of the accumulator to memory.
 * This instruction affects none of the flags in the processor status register and does not affect the accumulator.
 */
@ExperimentalUnsignedTypes
class STA(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "STA"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        cpuReference.writeToAddress(targetAddress, cpuReference.accumulator)
        cpuReference.programCounter++
    }
}

/**
 * Store Register X in Memory
 * Transfers value of X register to addressed memory location.
 * No flags or registers in the microprocessor are affected by the store operation.
 */
@ExperimentalUnsignedTypes
class STX(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "STX"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        cpuReference.writeToAddress(targetAddress, cpuReference.xRegister)
        cpuReference.programCounter++
    }
}

/**
 * Store Register Y in Memory
 * Transfer the value of the Y register to the addressed memory location.
 * STY does not affect any flags or registers in the microprocessor.
 */
@ExperimentalUnsignedTypes
class STY(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "STY"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute(targetAddress: UShort) {
        cpuReference.writeToAddress(targetAddress, cpuReference.yRegister)
        cpuReference.programCounter++
    }
}

/**
 * Transfer Accumulator to X register.
 * This instruction takes the value from the accumulator and loads it to register X
 * without disturbing the content of the accumulator A.
 *
 * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
 */
@ExperimentalUnsignedTypes
class TAX(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "TAX"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.xRegister = cpuReference.accumulator
        cpuReference.zeroFlag = cpuReference.xRegister == (0x00u).toUByte()
        cpuReference.negativeFlag = (cpuReference.xRegister.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }
}

/**
 * Transfer Accumulator to Y register.
 * This instruction takes the value from the accumulator and loads it to register Y
 * without disturbing the content of the accumulator A.
 *
 * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
 */
@ExperimentalUnsignedTypes
class TAY(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "TAY"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.yRegister = cpuReference.accumulator
        cpuReference.zeroFlag = cpuReference.yRegister == (0x00u).toUByte()
        cpuReference.negativeFlag = (cpuReference.yRegister.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }
}

/**
 * Transfer Stack Pointer to X register.
 * This instruction takes the value from the stack pointer and loads it to register X
 * without disturbing the content of the stack pointer.
 *
 * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
 */
@ExperimentalUnsignedTypes
class TSX(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "TSX"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.xRegister = cpuReference.stackPointer
        cpuReference.zeroFlag = cpuReference.xRegister == (0x00u).toUByte()
        cpuReference.negativeFlag = (cpuReference.xRegister.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }
}


/**
 * Transfer X register to Accumulator.
 * This instruction takes the value from the x register and loads it into the accumulator
 * without disturbing the content of the x register.
 *
 * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
 */
@ExperimentalUnsignedTypes
class TXA(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "TXA"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.accumulator = cpuReference.xRegister
        cpuReference.zeroFlag = cpuReference.accumulator == (0x00u).toUByte()
        cpuReference.negativeFlag = (cpuReference.accumulator.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }
}

/**
 * Transfer X register to Stack Pointer
 * This instruction transfers the value in the index register X to the stack pointer.
 * TXS changes only the stack pointer, making it equal to the content of the index
 * register X. It does not affect any of the flags.
 */
@ExperimentalUnsignedTypes
class TXS(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "TXS"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.stackPointer = cpuReference.xRegister
        cpuReference.programCounter++
    }
}

/**
 * Transfer Y register to Accumulator.
 * This instruction takes the value from the y register and loads it into the accumulator
 * without disturbing the content of the y register.
 *
 * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
 */
@ExperimentalUnsignedTypes
class TYA(override val cpuReference: CPU6502): AssemblyCode {
    override val opcodeName = "TYA"
    override val cycleCount: Map<AddressingMode, Int> = mapOf()

    override fun execute() {
        cpuReference.accumulator = cpuReference.yRegister
        cpuReference.zeroFlag = cpuReference.accumulator == (0x00u).toUByte()
        cpuReference.negativeFlag = (cpuReference.accumulator.toUInt() shr 7) == 1u

        cpuReference.programCounter++
    }
}
