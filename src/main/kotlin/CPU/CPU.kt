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
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class AND(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class ASL(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class BCC(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class BCS(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class BEQ(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class BIT(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class BMI(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class BNE(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class BPL(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class BRK(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class BVC(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class BVS(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class CLC(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class CLD(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class CLI(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class CLV(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class CMP(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class CPX(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class CPY(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class DEC(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class DEX(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class DEY(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class EOR(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class INC(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class INX(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class INY(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class BEQ(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class JMP(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class JSR(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class LDA(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class LDX(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class LDY(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class LSR(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class NOP(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class ORA(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class PHA(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class PHP(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class PLA(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class PLP(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class ROL(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class ROR(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class RTI(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class RTS(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class SBC(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class SEC(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class SED(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class SEI(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class STA(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class STX(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class STY(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class TAX(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class TAY(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class TSX(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class TXA(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class TXS(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }

    class TYA(): Instruction() {
        override fun run(targetAddress: UShort): UShort {
            TODO("Not yet implemented")
        }
    }



}