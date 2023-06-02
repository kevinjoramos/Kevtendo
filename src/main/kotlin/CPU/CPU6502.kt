package CPU

import ADC
import AND
import ASL
import BCC
import BCS
import BEQ
import BIT
import BMI
import BNE
import BPL
import BRK
import BVC
import BVS
import CLC
import CLD
import CLI
import CLV
import CMP
import CPX
import CPY
import DEC
import DEX
import DEY
import EOR
import INC
import INX
import INY
import JMP
import JSR
import LDA
import LDX
import LDY
import LSR
import NOP
import ORA
import PHA
import PHP
import PLA
import PLP
import ROL
import ROR
import RTI
import RTS
import SBC
import SEC
import SED
import SEI
import STA
import STX
import STY
import TAX
import TAY
import TSX
import TXA
import TXS
import TYA
import mediator.Component
import mediator.Mediator
import util.to2DigitHexString
import util.to4DigitHexString

/**
 * Emulation of the 6502 processor.
 * TODO("implement control signals")
 * TODO("implement clock cycles")
 */
@ExperimentalUnsignedTypes
class CPU6502(override var bus: Mediator) : Component {

    /**
     * 6502 Architecture components
     */
    var programCounter: UShort = 0xC000u
    var stackPointer: UByte = 0xFDu
    var accumulator: UByte = 0x00u
    var xRegister: UByte = 0x00u
    var yRegister: UByte = 0x00u
    var statusRegister: UByte = 0x00u

    /**
     * Other properties that will help make implementation, and testing easier
     */
    var opcodeValue: UByte= 0x00u
    var operandHighByte: UByte? = null
    var operandLowByte: UByte? = null
    var effectAddress: UShort? = null


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

    val interruptSignalTriage: List<() -> Unit> = mutableListOf()

    fun run() {

        val opcodeValue: UByte = readAddress(programCounter)

        val instruction = fetchInstruction(opcodeValue)

        instruction.runOperation()



        //interruptSignalTriage.map { it.invoke() }
    }


    /**
     * Interrupt control signals.
     */

    /**
     * NON MASKABLE INTERRUPT
     * 1. complete current instruction
     * 2. push MSB of program counter to stack
     * 3. push LSB of program counter to stack
     * 4. push status register to stack
     * 5. set interupt disable flag
     * 6. PC is loaded with address stored at 0xFFFA 0xFFFB
     */
    fun nmi() {
        val vectorLeastSignificantByte = readAddress(0xFFFAu)
        val vectorMostSignificantByte = readAddress(0xFFFBu)
            writeToAddress(stackPointer.toUShort(), (programCounter.toInt() shr 8).toUByte())
        stackPointer--
            writeToAddress(stackPointer.toUShort(), programCounter.toUByte())
        stackPointer--

        var statusRegisterValue: UByte = 0u
        val negativeBitMask: UByte = 0x80u
        val overflowBitMask: UByte = 0x40u
        val extraBitMask: UByte = 0x20u
        val breakBitMask: UByte = 0x10u
        val decimalBitMask: UByte = 0x08u
        val interruptDisableBitMask: UByte = 0x04u
        val zeroBitMask: UByte = 0x02u
        val carryBitMask: UByte = 0x01u

        if (negativeFlag) statusRegisterValue = statusRegisterValue or negativeBitMask
        if (overflowFlag) statusRegisterValue = statusRegisterValue or overflowBitMask
        if (extraFlag) statusRegisterValue = statusRegisterValue or extraBitMask
        if (breakFlag) statusRegisterValue = statusRegisterValue or breakBitMask
        if (decimalFlag) statusRegisterValue = statusRegisterValue or decimalBitMask
        if (interruptDisableFlag) statusRegisterValue = statusRegisterValue or interruptDisableBitMask
        if (zeroFlag) statusRegisterValue = statusRegisterValue or zeroBitMask
        if (carryFlag) statusRegisterValue = statusRegisterValue or carryBitMask
            writeToAddress(stackPointer.toUShort(), statusRegisterValue)
        stackPointer--

        this@CPU6502.interruptDisableFlag = true

        programCounter = (((vectorMostSignificantByte.toUInt() shl 8) + vectorLeastSignificantByte).toUShort())
    }

    /**
     * RESET
     * load the vector at 0xFFFC and 0xFFFD into PC
     */
    fun reset() {
        val vectorLeastSignificantByte = readAddress(0xFFFCu)
        val vectorMostSignificantByte = readAddress(0xFFFDu)
        programCounter = (((vectorMostSignificantByte.toUInt() shl 8) + vectorLeastSignificantByte).toUShort())
    }

    /**
     * IRQ MASKABLE INTERRUPT will be ignored if interruptDisable flag is true
     * 1. complete current instruction
     * 2. push MSB of program counter to stack
     * 3. push LSB of program counter to stack
     * 4. push status register to stack
     * 5. set interupt disable flag
     * 6. PC is loaded with address stored at 0xFFFA 0xFFFB
     */
    fun irq() {
        if (interruptDisableFlag) return

        val vectorLeastSignificantByte = readAddress(0xFFFEu)
        val vectorMostSignificantByte = readAddress(0xFFFFu)
            writeToAddress(stackPointer.toUShort(), (programCounter.toInt() shr 8).toUByte())
        stackPointer--
            writeToAddress(stackPointer.toUShort(), programCounter.toUByte())
        stackPointer--

        var statusRegisterValue: UByte = 0u
        val negativeBitMask: UByte = 0x80u
        val overflowBitMask: UByte = 0x40u
        val extraBitMask: UByte = 0x20u
        val breakBitMask: UByte = 0x10u
        val decimalBitMask: UByte = 0x08u
        val interruptDisableBitMask: UByte = 0x04u
        val zeroBitMask: UByte = 0x02u
        val carryBitMask: UByte = 0x01u

        if (negativeFlag) statusRegisterValue = statusRegisterValue or negativeBitMask
        if (overflowFlag) statusRegisterValue = statusRegisterValue or overflowBitMask
        if (extraFlag) statusRegisterValue = statusRegisterValue or extraBitMask
        if (breakFlag) statusRegisterValue = statusRegisterValue or breakBitMask
        if (decimalFlag) statusRegisterValue = statusRegisterValue or decimalBitMask
        if (interruptDisableFlag) statusRegisterValue = statusRegisterValue or interruptDisableBitMask
        if (zeroFlag) statusRegisterValue = statusRegisterValue or zeroBitMask
        if (carryFlag) statusRegisterValue = statusRegisterValue or carryBitMask
            writeToAddress(stackPointer.toUShort(), statusRegisterValue)
        stackPointer--

        interruptDisableFlag = true

        programCounter = (((vectorMostSignificantByte.toUInt() shl 8) + vectorLeastSignificantByte).toUShort())
    }

    /**
     * Three steps
     * 1. Decode byte to addressing mode
     * 2. fetch operands
     * 3. call the correct instruction.
     */
    private fun fetchInstruction(opcode: UByte): Instruction {
        println(opcode.to2DigitHexString())
        return this.opcodeTable.getValue(opcode) //?: throw InvalidOpcodeException("Opcode $opcode not found in opcode table.")
    }

    private val adc = ADC(this)
    private val and = AND(this)
    private val asl = ASL(this)
    private val bcc = BCC(this)
    private val bcs = BCS(this)
    private val beq = BEQ(this)
    private val bit = BIT(this)
    private val bmi = BMI(this)
    private val bne = BNE(this)
    private val bpl = BPL(this)
    private val brk = BRK(this)
    private val bvc = BVC(this)
    private val bvs = BVS(this)
    private val clc = CLC(this)
    private val cld = CLD(this)
    private val cli = CLI(this)
    private val clv = CLV(this)
    private val cmp = CMP(this)
    private val cpx = CPX(this)
    private val cpy = CPY(this)
    private val dec = DEC(this)
    private val dex = DEX(this)
    private val dey = DEY(this)
    private val eor = EOR(this)
    private val inc = INC(this)
    private val inx = INX(this)
    private val iny = INY(this)
    private val jmp = JMP(this)
    private val jsr = JSR(this)
    private val lda = LDA(this)
    private val ldx = LDX(this)
    private val ldy = LDY(this)
    private val lsr = LSR(this)
    private val nop = NOP(this)
    private val ora = ORA(this)
    private val pha = PHA(this)
    private val php = PHP(this)
    private val pla = PLA(this)
    private val plp = PLP(this)
    private val rol = ROL(this)
    private val ror = ROR(this)
    private val rti = RTI(this)
    private val rts = RTS(this)
    private val sbc = SBC(this)
    private val sec = SEC(this)
    private val sed = SED(this)
    private val sei = SEI(this)
    private val sta = STA(this)
    private val stx = STX(this)
    private val sty = STY(this)
    private val tax = TAX(this)
    private val tay = TAY(this)
    private val tsx = TSX(this)
    private val txa = TXA(this)
    private val txs = TXS(this)
    private val tya = TYA(this)

    private val opcodeTable: Map<UByte, Instruction> = mapOf(
        (0x00u).toUByte() to Instruction(AddressingMode.IMP, brk),
        (0x01u).toUByte() to Instruction(AddressingMode.X_IND, ora),
        (0x05u).toUByte() to Instruction(AddressingMode.ZPG, ora),
        (0x06u).toUByte() to Instruction(AddressingMode.ZPG, asl),
        (0x08u).toUByte() to Instruction(AddressingMode.IMP, php),
        (0x09u).toUByte() to Instruction(AddressingMode.IMM, ora),
        (0x0Au).toUByte() to Instruction(AddressingMode.A, asl),
        (0x0Du).toUByte() to Instruction(AddressingMode.ABS, ora),
        (0x0Eu).toUByte() to Instruction(AddressingMode.ABS, asl),

        (0x10u).toUByte() to Instruction(AddressingMode.REL, bpl),
        (0x11u).toUByte() to Instruction(AddressingMode.IND_Y, ora),
        (0x15u).toUByte() to Instruction(AddressingMode.ZPG_X, ora),
        (0x16u).toUByte() to Instruction(AddressingMode.ZPG_X, asl),
        (0x18u).toUByte() to Instruction(AddressingMode.IMP, clc),
        (0x19u).toUByte() to Instruction(AddressingMode.ABS_Y, ora),
        (0x1Du).toUByte() to Instruction(AddressingMode.ABS_X, ora),
        (0x1Eu).toUByte() to Instruction(AddressingMode.ABS_X, asl),

        (0x20u).toUByte() to Instruction(AddressingMode.ABS, jsr),
        (0x21u).toUByte() to Instruction(AddressingMode.X_IND, and),
        (0x24u).toUByte() to Instruction(AddressingMode.ZPG, bit),
        (0x25u).toUByte() to Instruction(AddressingMode.ZPG, and),
        (0x26u).toUByte() to Instruction(AddressingMode.ZPG, rol),
        (0x28u).toUByte() to Instruction(AddressingMode.IMP, plp),
        (0x29u).toUByte() to Instruction(AddressingMode.IMM, and),
        (0x2Au).toUByte() to Instruction(AddressingMode.A, rol),
        (0x2Cu).toUByte() to Instruction(AddressingMode.ABS, bit),
        (0x2Du).toUByte() to Instruction(AddressingMode.ABS, and),
        (0x2Eu).toUByte() to Instruction(AddressingMode.ABS, rol),

        (0x30u).toUByte() to Instruction(AddressingMode.REL, bmi),
        (0x31u).toUByte() to Instruction(AddressingMode.IND_Y, and),
        (0x35u).toUByte() to Instruction(AddressingMode.ZPG_X, and),
        (0x36u).toUByte() to Instruction(AddressingMode.ZPG_X, rol),
        (0x38u).toUByte() to Instruction(AddressingMode.IMP, sec),
        (0x39u).toUByte() to Instruction(AddressingMode.ABS_Y, and),
        (0x3Du).toUByte() to Instruction(AddressingMode.ABS_X, and),
        (0x3Eu).toUByte() to Instruction(AddressingMode.ABS_X, rol),

        (0x40u).toUByte() to Instruction(AddressingMode.IMP, rti),
        (0x41u).toUByte() to Instruction(AddressingMode.X_IND, eor),
        (0x45u).toUByte() to Instruction(AddressingMode.ZPG, eor),
        (0x46u).toUByte() to Instruction(AddressingMode.ZPG, lsr),
        (0x48u).toUByte() to Instruction(AddressingMode.IMP, pha),
        (0x49u).toUByte() to Instruction(AddressingMode.IMM, eor),
        (0x4Au).toUByte() to Instruction(AddressingMode.A, lsr),
        (0x4Cu).toUByte() to Instruction(AddressingMode.ABS, jmp),
        (0x4Du).toUByte() to Instruction(AddressingMode.ABS, eor),
        (0x4Eu).toUByte() to Instruction(AddressingMode.ABS, lsr),

        (0x50u).toUByte() to Instruction(AddressingMode.REL, bvc),
        (0x51u).toUByte() to Instruction(AddressingMode.IND_Y, eor),
        (0x55u).toUByte() to Instruction(AddressingMode.ZPG_X, eor),
        (0x56u).toUByte() to Instruction(AddressingMode.ZPG_X, lsr),
        (0x58u).toUByte() to Instruction(AddressingMode.IMP, cli),
        (0x59u).toUByte() to Instruction(AddressingMode.ABS_Y, eor),
        (0x5Du).toUByte() to Instruction(AddressingMode.ABS_X, eor),
        (0x5Eu).toUByte() to Instruction(AddressingMode.ABS_X, lsr),

        (0x60u).toUByte() to Instruction(AddressingMode.IMP, rts),
        (0x61u).toUByte() to Instruction(AddressingMode.X_IND, adc),
        (0x65u).toUByte() to Instruction(AddressingMode.ZPG, adc),
        (0x66u).toUByte() to Instruction(AddressingMode.ZPG, ror),
        (0x68u).toUByte() to Instruction(AddressingMode.IMP, pla),
        (0x69u).toUByte() to Instruction(AddressingMode.IMM, adc),
        (0x6Au).toUByte() to Instruction(AddressingMode.A, ror),
        (0x6Cu).toUByte() to Instruction(AddressingMode.IND, jmp),
        (0x6Du).toUByte() to Instruction(AddressingMode.ABS, adc),
        (0x6Eu).toUByte() to Instruction(AddressingMode.ABS, ror),

        (0x70u).toUByte() to Instruction(AddressingMode.REL, bvs),
        (0x71u).toUByte() to Instruction(AddressingMode.IND_Y, adc),
        (0x75u).toUByte() to Instruction(AddressingMode.ZPG_X, adc),
        (0x76u).toUByte() to Instruction(AddressingMode.ZPG_X, ror),
        (0x78u).toUByte() to Instruction(AddressingMode.IMP, sei),
        (0x79u).toUByte() to Instruction(AddressingMode.ABS_Y, adc),
        (0x7Du).toUByte() to Instruction(AddressingMode.ABS_X, adc),
        (0x7Eu).toUByte() to Instruction(AddressingMode.ABS_X, ror),

        (0x81u).toUByte() to Instruction(AddressingMode.X_IND, sta),
        (0x84u).toUByte() to Instruction(AddressingMode.ZPG, sty),
        (0x85u).toUByte() to Instruction(AddressingMode.ZPG, sta),
        (0x86u).toUByte() to Instruction(AddressingMode.ZPG, stx),
        (0x88u).toUByte() to Instruction(AddressingMode.IMP, dey),
        (0x8Au).toUByte() to Instruction(AddressingMode.IMP, txa),
        (0x8Cu).toUByte() to Instruction(AddressingMode.ABS, sty),
        (0x8Du).toUByte() to Instruction(AddressingMode.ABS, sta),
        (0x8Eu).toUByte() to Instruction(AddressingMode.ABS, stx),

        (0x90u).toUByte() to Instruction(AddressingMode.REL, bcc),
        (0x91u).toUByte() to Instruction(AddressingMode.IND_Y, sta),
        (0x94u).toUByte() to Instruction(AddressingMode.ZPG_X, sty),
        (0x95u).toUByte() to Instruction(AddressingMode.ZPG_X, sta),
        (0x96u).toUByte() to Instruction(AddressingMode.ZPG_Y, stx),
        (0x98u).toUByte() to Instruction(AddressingMode.IMP, tya),
        (0x99u).toUByte() to Instruction(AddressingMode.ABS_Y, sta),
        (0x9Au).toUByte() to Instruction(AddressingMode.IMP, txs),
        (0x9Du).toUByte() to Instruction(AddressingMode.ABS_X, sta),

        (0xA0u).toUByte() to Instruction(AddressingMode.IMM, ldy),
        (0xA1u).toUByte() to Instruction(AddressingMode.X_IND, lda),
        (0xA2u).toUByte() to Instruction(AddressingMode.IMM, ldx),
        (0xA4u).toUByte() to Instruction(AddressingMode.ZPG, ldy),
        (0xA5u).toUByte() to Instruction(AddressingMode.ZPG, lda),
        (0xA6u).toUByte() to Instruction(AddressingMode.ZPG, ldx),
        (0xA8u).toUByte() to Instruction(AddressingMode.IMP, tay),
        (0xA9u).toUByte() to Instruction(AddressingMode.IMM, lda),
        (0xAAu).toUByte() to Instruction(AddressingMode.IMP, tax),
        (0xACu).toUByte() to Instruction(AddressingMode.ABS, ldy),
        (0xADu).toUByte() to Instruction(AddressingMode.ABS, lda),
        (0xAEu).toUByte() to Instruction(AddressingMode.ABS, ldx),

        (0xB0u).toUByte() to Instruction(AddressingMode.REL, bcs),
        (0xB1u).toUByte() to Instruction(AddressingMode.IND_Y, lda),
        (0xB4u).toUByte() to Instruction(AddressingMode.ZPG_X, ldy),
        (0xB5u).toUByte() to Instruction(AddressingMode.ZPG_X, lda),
        (0xB6u).toUByte() to Instruction(AddressingMode.ZPG_Y, ldx),
        (0xB8u).toUByte() to Instruction(AddressingMode.IMP, clv),
        (0xB9u).toUByte() to Instruction(AddressingMode.ABS_Y, lda),
        (0xBAu).toUByte() to Instruction(AddressingMode.IMP, tsx),
        (0xBCu).toUByte() to Instruction(AddressingMode.ABS_X, ldy),
        (0xBDu).toUByte() to Instruction(AddressingMode.ABS_X, lda),
        (0xBEu).toUByte() to Instruction(AddressingMode.ABS_Y, ldx),

        (0xC0u).toUByte() to Instruction(AddressingMode.IMM, cpy),
        (0xC1u).toUByte() to Instruction(AddressingMode.X_IND, cmp),
        (0xC4u).toUByte() to Instruction(AddressingMode.ZPG, cpy),
        (0xC5u).toUByte() to Instruction(AddressingMode.ZPG, cmp),
        (0xC6u).toUByte() to Instruction(AddressingMode.ZPG, dec),
        (0xC8u).toUByte() to Instruction(AddressingMode.IMP, iny),
        (0xC9u).toUByte() to Instruction(AddressingMode.IMM, cmp),
        (0xCAu).toUByte() to Instruction(AddressingMode.IMP, dex),
        (0xCCu).toUByte() to Instruction(AddressingMode.ABS, cpy),
        (0xCDu).toUByte() to Instruction(AddressingMode.ABS, cmp),
        (0xCEu).toUByte() to Instruction(AddressingMode.ABS, dec),

        (0xD0u).toUByte() to Instruction(AddressingMode.REL, bne),
        (0xD1u).toUByte() to Instruction(AddressingMode.IND_Y, cmp),
        (0xD5u).toUByte() to Instruction(AddressingMode.ZPG_X, cmp),
        (0xD6u).toUByte() to Instruction(AddressingMode.ZPG_X, dec),
        (0xD8u).toUByte() to Instruction(AddressingMode.IMP, cld),
        (0xD9u).toUByte() to Instruction(AddressingMode.ABS_Y, cmp),
        (0xDDu).toUByte() to Instruction(AddressingMode.ABS_X, cmp),
        (0xDEu).toUByte() to Instruction(AddressingMode.ABS_X, dec),

        (0xE0u).toUByte() to Instruction(AddressingMode.IMM, cpx),
        (0xE1u).toUByte() to Instruction(AddressingMode.X_IND, sbc),
        (0xE4u).toUByte() to Instruction(AddressingMode.ZPG, cpx),
        (0xE5u).toUByte() to Instruction(AddressingMode.ZPG, sbc),
        (0xE6u).toUByte() to Instruction(AddressingMode.ZPG, inc),
        (0xE8u).toUByte() to Instruction(AddressingMode.IMP, inx),
        (0xE9u).toUByte() to Instruction(AddressingMode.IMM, sbc),
        (0xEAu).toUByte() to Instruction(AddressingMode.IMP, nop),
        (0xECu).toUByte() to Instruction(AddressingMode.ABS, cpx),
        (0xEDu).toUByte() to Instruction(AddressingMode.ABS, sbc),
        (0xEEu).toUByte() to Instruction(AddressingMode.ABS, inc),

        (0xF0u).toUByte() to Instruction(AddressingMode.REL, beq),
        (0xF1u).toUByte() to Instruction(AddressingMode.IND_Y, sbc),
        (0xF5u).toUByte() to Instruction(AddressingMode.ZPG_X, sbc),
        (0xF6u).toUByte() to Instruction(AddressingMode.ZPG_X, inc),
        (0xF8u).toUByte() to Instruction(AddressingMode.IMP, sed),
        (0xF9u).toUByte() to Instruction(AddressingMode.ABS_Y, sbc),
        (0xFDu).toUByte() to Instruction(AddressingMode.ABS_X, sbc),
        (0xFEu).toUByte() to Instruction(AddressingMode.ABS_X, inc),
    )

    /**
     * Immediate Addressing Mode
     * Returns the literal 8-bit operand located immediately after the opcode.
     * Total instruction length is: Opcode + Operand = 2 bytes.
     */
    fun immediateAddressing(): UByte {
        programCounter++
        return readAddress(programCounter)
    }

    /**
     * Absolute Addressing Mode
     * Returns a 16-bit memory address that contains operand.
     * This address is provided in the next 2 bytes after the opcode; little-endian.
     * Total instruction length is: Opcode + LSB + MSB = 3 bytes.
     */
    fun absoluteAddressing(): Triple<UByte, UByte, UShort> {
        programCounter++
        val leastSignificantByte = readAddress(programCounter)

        programCounter++
        val mostSignificantByte = readAddress((programCounter))

        val targetAddress: UShort = (mostSignificantByte.toUInt() shl 8).toUShort() or leastSignificantByte.toUShort()

        return Triple(leastSignificantByte, mostSignificantByte, targetAddress)
    }

    /**
     * Zero-Page Addressing Mode
     * Returns a 16-bit zero-page memory address that contains operand.
     * Since the high-byte of this address is 00, only the lower-byte is placed after the operand.
     * Total instruction length is: Opcode + LSB = 2 bytes.
     */
    fun zeroPageAddressing(): UShort {
        programCounter++
        return readAddress(programCounter).toUShort()
    }

    /**
     * Absolute Addressing Mode X Indexed
     * Returns a 16-bit memory address computed from the given 16-bit address + contents of x register.
     * The given address is provided in the next 2 bytes after the opcode; little-endian.
     * Total instruction length is: Opcode + LSB + MSB = 3 bytes.
     */
    fun absoluteXIndexedAddressing(): UShort {
        programCounter++
        val leastSignificantByte = readAddress(programCounter).toUShort()
        programCounter++
        val mostSignificantByte = readAddress((programCounter)).toUInt()
        return (((mostSignificantByte shl 8).toUShort() or leastSignificantByte) + xRegister).toUShort()
    }

    /**
     * Absolute Addressing Mode Y Indexed
     * Returns a 16-bit memory address computed from the given 16-bit address + contents of y register.
     * The given address is provided in the next 2 bytes after the opcode; little-endian.
     * Total instruction length is: Opcode + LSB + MSB = 3 bytes.
     */
    fun absoluteYIndexedAddressing(): UShort {
        programCounter++
        val leastSignificantByte = readAddress(programCounter).toUShort()
        programCounter++
        val mostSignificantByte = readAddress((programCounter)).toUInt()
        return (((mostSignificantByte shl 8).toUShort() or leastSignificantByte) + yRegister).toUShort()
    }

    /**
     * Zero-Page Addressing Mode X Indexed
     * Returns a 16-bit zero-page memory address that contains operand.
     * Is similar to zero-page addressing except the value of the X register
     * is added to point two a new address.
     * Total instruction length is: Opcode + LSB = 2 bytes.
     */
    fun zeroPageXIndexedAddressing(): UShort {
        programCounter++
        val operand: UByte = readAddress(programCounter)
        val targetAddress: UByte = (operand + xRegister).toUByte()
        return targetAddress.toUShort()
    }

    /**
     * Zero-Page Addressing Mode Y Indexed
     * Returns a 16-bit zero-page memory address that contains operand.
     * Is similar to zero-page addressing except the value of the Y register
     * is added to point two a new address.
     * Total instruction length is: Opcode + LSB = 2 bytes.
     */
    fun zeroPageYIndexedAddressing(): UShort {
        programCounter++
        val operand: UByte = readAddress(programCounter)
        val targetAddress: UByte = (operand + yRegister).toUByte()
        return targetAddress.toUShort()
    }

    fun indirectAddressing(): UShort {
        programCounter++
        val leastSignificantByte: UByte = readAddress(programCounter)

        programCounter++
        val mostSignificantByte = readAddress(programCounter).toUInt()
        val indirectAddress: UShort = ((mostSignificantByte shl 8) + leastSignificantByte).toUShort()

        val targetLeastSignificantByte: UByte = readAddress(indirectAddress)
        val targetMostSignificantByte = readAddress((indirectAddress + 1u).toUShort()).toUInt()
        return ((targetMostSignificantByte shl 8) + targetLeastSignificantByte).toUShort()
    }

    fun xIndexedIndirectAddressing(): UShort {
        programCounter++
        val operand: UByte = readAddress(programCounter)
        val zeroPageAddress: UShort = (operand + xRegister).toUByte().toUShort()

        val targetLeastSignificantByte: UByte = readAddress(zeroPageAddress)
        val targetMostSignificantByte: UByte = readAddress((zeroPageAddress + 1u).toUByte().toUShort())

        return ((targetMostSignificantByte.toUInt() shl 8) + targetLeastSignificantByte).toUShort()
    }

    fun indirectYIndexedAddressing(): UShort {
        programCounter++
        val zeroPageOperand: UByte = readAddress(programCounter)
        val targetLeastSignificantByte = readAddress(zeroPageOperand.toUShort())
        val targetMostSignificantByte: UByte =  readAddress((zeroPageOperand + 1u).toUShort())

        return ((targetMostSignificantByte.toUInt() shl 8) + targetLeastSignificantByte + yRegister).toUShort()
    }

    fun relativeAddressing(): UShort {
        programCounter++
        val offset: Byte = readAddress(programCounter).toByte()
        return (programCounter.toInt() + (offset.toInt()) + 1).toUShort()
    }

    companion object {

    }


}