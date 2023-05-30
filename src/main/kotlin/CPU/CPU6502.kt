package CPU

import disassembler.DisassembledInstruction
import mediator.Component
import mediator.Mediator
import util.to2DigitHexString
import util.to4DigitHexString

/**
 * Emulation of the 6502 processor.
 * TODO("implement all opcodes")
 * TODO("implement control signals")
 * TODO("implement clock cycles")
 *
 *
 */
@ExperimentalUnsignedTypes
class CPU6502(override var bus: Mediator) : Component {

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

    val interruptSignalTriage: List<() -> Unit> = mutableListOf()

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

    fun run() {
        val opcode: UByte = readAddress(programCounter)
        println("PC = ${programCounter.to4DigitHexString()}. Opcode = ${opcode.to2DigitHexString()}")
        val instruction = fetchInstruction(opcode)
        instruction.executionFunction.invoke()
        
        //programCounter++

        //interruptSignalTriage.map { it.invoke() }
    }

    /**
     * Three steps
     * 1. Decode byte to addressing mode
     * 2. fetch operands
     * 3. call the correct instruction.
     */
    private fun fetchInstruction(opcode: UByte): InstructionWrapper {
        println(opcode.to2DigitHexString())
        return this.opcodeTable.getValue(opcode) //?: throw InvalidOpcodeException("Opcode $opcode not found in opcode table.")
    }

    private val opcodeTable: Map<UByte, InstructionWrapper> = mapOf(
        (0x00u).toUByte() to InstructionWrapper({ BRK().execute() }, BRK().opcodeName, "impl", null),
        (0x01u).toUByte() to InstructionWrapper({ ORA().execute(xIndexedIndirectAddressing()) }, ORA().opcodeName, "x ind"),
        (0x05u).toUByte() to InstructionWrapper({ ORA().execute(zeroPageAddressing()) }, ORA().opcodeName, "zpg"),
        (0x06u).toUByte() to InstructionWrapper({ ASL().execute(zeroPageAddressing()) }, ASL().opcodeName, "zpg"),
        (0x08u).toUByte() to InstructionWrapper({ PHP().execute() }, PHP().opcodeName, "impl"),
        (0x09u).toUByte() to InstructionWrapper({ ORA().execute(immediateAddressing()) }, ORA().opcodeName, "imm"),
        (0x0Au).toUByte() to InstructionWrapper({ ASL().execute() }, ASL().opcodeName, "A"),
        (0x0Du).toUByte() to InstructionWrapper({ ORA().execute(absoluteAddressing()) }, ORA().opcodeName, "abs"),
        (0x0Eu).toUByte() to InstructionWrapper({ ASL().execute(absoluteAddressing()) }, ASL().opcodeName, "abs"),

        (0x10u).toUByte() to InstructionWrapper({ BPL().execute(relativeAddressing()) }, BPL().opcodeName, "rel"),
        (0x11u).toUByte() to InstructionWrapper({ ORA().execute(indirectYIndexedAddressing()) }, ORA().opcodeName, "ind y"),
        (0x15u).toUByte() to InstructionWrapper({ ORA().execute(zeroPageXIndexedAddressing()) }, ORA().opcodeName, "zpg x"),
        (0x16u).toUByte() to InstructionWrapper({ ASL().execute(zeroPageXIndexedAddressing()) }, ASL().opcodeName, "zpg x"),
        (0x18u).toUByte() to InstructionWrapper({ CLC().execute() }, CLC().opcodeName, "impl"),
        (0x19u).toUByte() to InstructionWrapper({ ORA().execute(absoluteYIndexedAddressing()) }, ORA().opcodeName, "abs y"),
        (0x1Du).toUByte() to InstructionWrapper({ ORA().execute(absoluteXIndexedAddressing()) }, ORA().opcodeName, "abs x"),
        (0x1Eu).toUByte() to InstructionWrapper({ ASL().execute(absoluteXIndexedAddressing()) }, ASL().opcodeName, "abs x"),

        (0x20u).toUByte() to InstructionWrapper({ JSR().execute(absoluteAddressing()) }, JSR().opcodeName, "abs"),
        (0x21u).toUByte() to InstructionWrapper({ AND().execute(xIndexedIndirectAddressing()) }, AND().opcodeName, "x ind"),
        (0x24u).toUByte() to InstructionWrapper({ BIT().execute(zeroPageAddressing()) }, BIT().opcodeName, "zpg"),
        (0x25u).toUByte() to InstructionWrapper({ AND().execute(zeroPageAddressing()) }, AND().opcodeName, "zpg"),
        (0x26u).toUByte() to InstructionWrapper({ ROL().execute(zeroPageAddressing()) }, ROL().opcodeName, "zpg"),
        (0x28u).toUByte() to InstructionWrapper({ PLP().execute() }, PLP().opcodeName, "impl"),
        (0x29u).toUByte() to InstructionWrapper({ AND().execute(immediateAddressing()) }, AND().opcodeName, "imm"),
        (0x2Au).toUByte() to InstructionWrapper({ ROL().execute() }, ROL().opcodeName, "A"),
        (0x2Cu).toUByte() to InstructionWrapper({ BIT().execute(absoluteAddressing()) }, BIT().opcodeName, "abs"),
        (0x2Du).toUByte() to InstructionWrapper({ AND().execute(absoluteAddressing()) }, AND().opcodeName, "abs"),
        (0x2Eu).toUByte() to InstructionWrapper({ ROL().execute(absoluteAddressing()) }, ROL().opcodeName, "abs"),

        (0x30u).toUByte() to InstructionWrapper({ BMI().execute(relativeAddressing()) }, BMI().opcodeName, "rel"),
        (0x31u).toUByte() to InstructionWrapper({ AND().execute(indirectYIndexedAddressing()) }, AND().opcodeName, "ind y"),
        (0x35u).toUByte() to InstructionWrapper({ AND().execute(zeroPageXIndexedAddressing()) }, AND().opcodeName, "zpg x"),
        (0x36u).toUByte() to InstructionWrapper({ ROL().execute(zeroPageXIndexedAddressing()) }, ROL().opcodeName, "zpg x"),
        (0x38u).toUByte() to InstructionWrapper({ SEC().execute() }, SEC().opcodeName, "impl"),
        (0x39u).toUByte() to InstructionWrapper({ AND().execute(absoluteYIndexedAddressing()) }, AND().opcodeName, "abs y"),
        (0x3Du).toUByte() to InstructionWrapper({ AND().execute(absoluteXIndexedAddressing()) }, AND().opcodeName, "abs x"),
        (0x3Eu).toUByte() to InstructionWrapper({ ROL().execute(absoluteXIndexedAddressing()) }, ROL().opcodeName, "abs x"),

        (0x40u).toUByte() to InstructionWrapper({ RTI().execute() }, RTI().opcodeName, "impl"),
        (0x41u).toUByte() to InstructionWrapper({ EOR().execute(xIndexedIndirectAddressing()) }, EOR().opcodeName, "x ind"),
        (0x45u).toUByte() to InstructionWrapper({ EOR().execute(zeroPageAddressing()) }, EOR().opcodeName, "zpg"),
        (0x46u).toUByte() to InstructionWrapper({ LSR().execute(zeroPageAddressing()) }, LSR().opcodeName, "zpg"),
        (0x48u).toUByte() to InstructionWrapper({ PHA().execute() }, PHA().opcodeName, "impl"),
        (0x49u).toUByte() to InstructionWrapper({ EOR().execute(immediateAddressing()) }, EOR().opcodeName, "imm"),
        (0x4Au).toUByte() to InstructionWrapper({ LSR().execute() }, LSR().opcodeName, "A"),
        (0x4Cu).toUByte() to InstructionWrapper({ JMP().execute(absoluteAddressing()) }, JMP().opcodeName, "abs"),
        (0x4Du).toUByte() to InstructionWrapper({ EOR().execute(absoluteAddressing()) }, EOR().opcodeName, "abs"),
        (0x4Eu).toUByte() to InstructionWrapper({ LSR().execute(absoluteAddressing()) }, LSR().opcodeName, "abs"),

        (0x50u).toUByte() to InstructionWrapper({ BVC().execute(relativeAddressing()) }, BVC().opcodeName, "rel"),
        (0x51u).toUByte() to InstructionWrapper({ EOR().execute(indirectYIndexedAddressing()) }, EOR().opcodeName, "ind y"),
        (0x55u).toUByte() to InstructionWrapper({ EOR().execute(zeroPageXIndexedAddressing()) }, EOR().opcodeName, "zpg x"),
        (0x56u).toUByte() to InstructionWrapper({ LSR().execute(zeroPageXIndexedAddressing()) }, LSR().opcodeName, "zpg x"),
        (0x58u).toUByte() to InstructionWrapper({ CLI().execute() }, CLI().opcodeName, "impl"),
        (0x59u).toUByte() to InstructionWrapper({ EOR().execute(absoluteYIndexedAddressing()) }, EOR().opcodeName, "abs y"),
        (0x5Du).toUByte() to InstructionWrapper({ EOR().execute(absoluteXIndexedAddressing()) }, EOR().opcodeName, "abs x"),
        (0x5Eu).toUByte() to InstructionWrapper({ LSR().execute(absoluteXIndexedAddressing()) }, LSR().opcodeName, "abs x"),

        (0x60u).toUByte() to InstructionWrapper({ RTS().execute() }, RTS().opcodeName, "impl"),
        (0x61u).toUByte() to InstructionWrapper({ ADC().execute(xIndexedIndirectAddressing()) }, ADC().opcodeName, "x ind"),
        (0x65u).toUByte() to InstructionWrapper({ ADC().execute(zeroPageAddressing()) }, ADC().opcodeName, "zpg"),
        (0x66u).toUByte() to InstructionWrapper({ ROR().execute(zeroPageAddressing()) }, ROR().opcodeName, "zpg"),
        (0x68u).toUByte() to InstructionWrapper({ PLA().execute() }, PLA().opcodeName, "impl"),
        (0x69u).toUByte() to InstructionWrapper({ ADC().execute(immediateAddressing()) }, ADC().opcodeName, "imm"),
        (0x6Au).toUByte() to InstructionWrapper({ ROR().execute() }, ROR().opcodeName, "A"),
        (0x6Cu).toUByte() to InstructionWrapper({ JMP().execute(indirectAddressing()) }, JMP().opcodeName, "ind"),
        (0x6Du).toUByte() to InstructionWrapper({ ADC().execute(absoluteAddressing()) }, ADC().opcodeName, "abs"),
        (0x6Eu).toUByte() to InstructionWrapper({ ROR().execute(absoluteAddressing()) }, ROR().opcodeName, "abs"),

        (0x70u).toUByte() to InstructionWrapper({ BVS().execute(relativeAddressing()) }, BVS().opcodeName, "rel"),
        (0x71u).toUByte() to InstructionWrapper({ ADC().execute(indirectYIndexedAddressing()) }, ADC().opcodeName, "ind y"),
        (0x75u).toUByte() to InstructionWrapper({ ADC().execute(zeroPageXIndexedAddressing()) }, ADC().opcodeName, "zpg x"),
        (0x76u).toUByte() to InstructionWrapper({ ROR().execute(zeroPageXIndexedAddressing()) }, ROR().opcodeName, "zpg x"),
        (0x78u).toUByte() to InstructionWrapper({ SEI().execute() }, SEI().opcodeName, "impl"),
        (0x79u).toUByte() to InstructionWrapper({ ADC().execute(absoluteYIndexedAddressing()) }, ADC().opcodeName, "abs y"),
        (0x7Du).toUByte() to InstructionWrapper({ ADC().execute(absoluteXIndexedAddressing()) }, ADC().opcodeName, "abs x"),
        (0x7Eu).toUByte() to InstructionWrapper({ ROR().execute(absoluteXIndexedAddressing()) }, ROR().opcodeName, "abs x"),

        (0x81u).toUByte() to InstructionWrapper({ STA().execute(xIndexedIndirectAddressing()) }, STA().opcodeName, "x ind"),
        (0x84u).toUByte() to InstructionWrapper({ STY().execute(zeroPageAddressing()) }, STY().opcodeName, "zpg"),
        (0x85u).toUByte() to InstructionWrapper({ STA().execute(zeroPageAddressing()) }, STA().opcodeName, "zpg"),
        (0x86u).toUByte() to InstructionWrapper({ STX().execute(zeroPageAddressing()) }, STX().opcodeName, "zpg"),
        (0x88u).toUByte() to InstructionWrapper({ DEY().execute() }, DEY().opcodeName, "impl"),
        (0x8Au).toUByte() to InstructionWrapper({ TXA().execute() }, TXA().opcodeName, "impl"),
        (0x8Cu).toUByte() to InstructionWrapper({ STY().execute(absoluteAddressing()) }, STY().opcodeName, "abs"),
        (0x8Du).toUByte() to InstructionWrapper({ STA().execute(absoluteAddressing()) }, STA().opcodeName, "abs"),
        (0x8Eu).toUByte() to InstructionWrapper({ STX().execute(absoluteAddressing()) }, STX().opcodeName, "abs"),

        (0x90u).toUByte() to InstructionWrapper({ BCC().execute(relativeAddressing()) }, BCC().opcodeName, "rel"),
        (0x91u).toUByte() to InstructionWrapper({ STA().execute(indirectYIndexedAddressing()) }, STA().opcodeName, "ind y"),
        (0x94u).toUByte() to InstructionWrapper({ STY().execute(zeroPageXIndexedAddressing()) }, STY().opcodeName, "zpg x"),
        (0x95u).toUByte() to InstructionWrapper({ STA().execute(zeroPageXIndexedAddressing()) }, STA().opcodeName, "zpg x"),
        (0x96u).toUByte() to InstructionWrapper({ STX().execute(zeroPageYIndexedAddressing()) }, STX().opcodeName, "zpg y"),
        (0x98u).toUByte() to InstructionWrapper({ TYA().execute() }, TYA().opcodeName, "impl"),
        (0x99u).toUByte() to InstructionWrapper({ STA().execute(absoluteYIndexedAddressing()) }, STA().opcodeName, "abs y"),
        (0x9Au).toUByte() to InstructionWrapper({ TXS().execute() }, TXS().opcodeName, "impl"),
        (0x9Du).toUByte() to InstructionWrapper({ STA().execute(absoluteXIndexedAddressing()) }, STA().opcodeName, "abs x"),

        (0xA0u).toUByte() to InstructionWrapper({ LDY().execute(immediateAddressing()) }, LDY().opcodeName, "imm"),
        (0xA1u).toUByte() to InstructionWrapper({ LDA().execute(xIndexedIndirectAddressing()) }, LDA().opcodeName, "x ind"),
        (0xA2u).toUByte() to InstructionWrapper({ LDX().execute(immediateAddressing()) }, LDX().opcodeName, "imm"),
        (0xA4u).toUByte() to InstructionWrapper({ LDY().execute(zeroPageAddressing()) }, LDY().opcodeName, "zpg"),
        (0xA5u).toUByte() to InstructionWrapper({ LDA().execute(zeroPageAddressing()) }, LDA().opcodeName, "zpg"),
        (0xA6u).toUByte() to InstructionWrapper({ LDX().execute(zeroPageAddressing()) }, LDX().opcodeName, "zpg"),
        (0xA8u).toUByte() to InstructionWrapper({ TAY().execute() }, TAY().opcodeName, "impl"),
        (0xA9u).toUByte() to InstructionWrapper({ LDA().execute(immediateAddressing()) }, LDA().opcodeName, "imm"),
        (0xAAu).toUByte() to InstructionWrapper({ TAX().execute() }, TAX().opcodeName, "impl"),
        (0xACu).toUByte() to InstructionWrapper({ LDY().execute(absoluteAddressing()) }, LDY().opcodeName, "abs"),
        (0xADu).toUByte() to InstructionWrapper({ LDA().execute(absoluteAddressing()) }, LDA().opcodeName, "abs"),
        (0xAEu).toUByte() to InstructionWrapper({ LDX().execute(absoluteAddressing()) }, LDX().opcodeName, "abs"),

        (0xB0u).toUByte() to InstructionWrapper({ BCS().execute(relativeAddressing()) }, BCS().opcodeName, "rel"),
        (0xB1u).toUByte() to InstructionWrapper({ LDA().execute(indirectYIndexedAddressing()) }, LDA().opcodeName, "ind y"),
        (0xB4u).toUByte() to InstructionWrapper({ LDY().execute(zeroPageXIndexedAddressing()) }, LDY().opcodeName, "zpg x"),
        (0xB5u).toUByte() to InstructionWrapper({ LDA().execute(zeroPageXIndexedAddressing()) }, LDA().opcodeName, "zpg x"),
        (0xB6u).toUByte() to InstructionWrapper({ LDX().execute(zeroPageYIndexedAddressing()) }, LDX().opcodeName, "zpg y"),
        (0xB8u).toUByte() to InstructionWrapper({ CLV().execute() }, CLV().opcodeName, "impl"),
        (0xB9u).toUByte() to InstructionWrapper({ LDA().execute(absoluteYIndexedAddressing()) }, LDA().opcodeName, "abs y"),
        (0xBAu).toUByte() to InstructionWrapper({ TSX().execute() }, TSX().opcodeName, "impl"),
        (0xBCu).toUByte() to InstructionWrapper({ LDY().execute(absoluteXIndexedAddressing()) }, LDY().opcodeName, "abs x"),
        (0xBDu).toUByte() to InstructionWrapper({ LDA().execute(absoluteXIndexedAddressing()) }, LDA().opcodeName, "abs x"),
        (0xBEu).toUByte() to InstructionWrapper({ LDX().execute(absoluteYIndexedAddressing()) }, LDX().opcodeName, "abs y"),

        (0xC0u).toUByte() to InstructionWrapper({ CPY().execute(immediateAddressing()) }, CPY().opcodeName, "imm"),
        (0xC1u).toUByte() to InstructionWrapper({ CMP().execute(xIndexedIndirectAddressing()) }, CMP().opcodeName, "x ind"),
        (0xC4u).toUByte() to InstructionWrapper({ CPY().execute(zeroPageAddressing()) }, CPY().opcodeName, "zpg"),
        (0xC5u).toUByte() to InstructionWrapper({ CMP().execute(zeroPageAddressing()) }, CMP().opcodeName, "zpg"),
        (0xC6u).toUByte() to InstructionWrapper({ DEC().execute(zeroPageAddressing()) }, DEC().opcodeName, "zpg"),
        (0xC8u).toUByte() to InstructionWrapper({ INY().execute() }, INY().opcodeName, "impl"),
        (0xC9u).toUByte() to InstructionWrapper({ CMP().execute(immediateAddressing()) }, CMP().opcodeName, "imm"),
        (0xCAu).toUByte() to InstructionWrapper({ DEX().execute() }, DEX().opcodeName, "impl"),
        (0xCCu).toUByte() to InstructionWrapper({ CPY().execute(absoluteAddressing()) }, CPY().opcodeName, "abs"),
        (0xCDu).toUByte() to InstructionWrapper({ CMP().execute(absoluteAddressing()) }, CMP().opcodeName, "abs"),
        (0xCEu).toUByte() to InstructionWrapper({ DEC().execute(absoluteAddressing()) }, DEC().opcodeName, "abs"),

        (0xD0u).toUByte() to InstructionWrapper({ BNE().execute(relativeAddressing()) }, BNE().opcodeName, "rel"),
        (0xD1u).toUByte() to InstructionWrapper({ CMP().execute(indirectYIndexedAddressing()) }, CMP().opcodeName, "ind y"),
        (0xD5u).toUByte() to InstructionWrapper({ CMP().execute(zeroPageXIndexedAddressing()) }, CMP().opcodeName, "zpg x"),
        (0xD6u).toUByte() to InstructionWrapper({ DEC().execute(zeroPageXIndexedAddressing()) }, DEC().opcodeName, "zpg x"),
        (0xD8u).toUByte() to InstructionWrapper({ CLD().execute() }, CLD().opcodeName, "impl"),
        (0xD9u).toUByte() to InstructionWrapper({ CMP().execute(absoluteYIndexedAddressing()) }, CMP().opcodeName, "abs y"),
        (0xDDu).toUByte() to InstructionWrapper({ CMP().execute(absoluteXIndexedAddressing()) }, CMP().opcodeName, "abs x"),
        (0xDEu).toUByte() to InstructionWrapper({ DEC().execute(absoluteXIndexedAddressing()) }, DEC().opcodeName, "abs x"),

        (0xE0u).toUByte() to InstructionWrapper({ CPX().execute(immediateAddressing()) }, CPX().opcodeName, "imm"),
        (0xE1u).toUByte() to InstructionWrapper({ SBC().execute(xIndexedIndirectAddressing()) }, SBC().opcodeName, "x ind"),
        (0xE4u).toUByte() to InstructionWrapper({ CPX().execute(zeroPageAddressing()) }, CPX().opcodeName, "zpg"),
        (0xE5u).toUByte() to InstructionWrapper({ SBC().execute(zeroPageAddressing()) }, SBC().opcodeName, "zpg"),
        (0xE6u).toUByte() to InstructionWrapper({ INC().execute(zeroPageAddressing()) }, INC().opcodeName, "zpg"),
        (0xE8u).toUByte() to InstructionWrapper({ INX().execute() }, INX().opcodeName, "impl"),
        (0xE9u).toUByte() to InstructionWrapper({ SBC().execute(immediateAddressing()) }, SBC().opcodeName, "imm"),
        (0xEAu).toUByte() to InstructionWrapper({ NOP().execute() }, NOP().opcodeName, "impl"),
        (0xECu).toUByte() to InstructionWrapper({ CPX().execute(absoluteAddressing()) }, CPX().opcodeName, "abs"),
        (0xEDu).toUByte() to InstructionWrapper({ SBC().execute(absoluteAddressing()) }, SBC().opcodeName, "abs"),
        (0xEEu).toUByte() to InstructionWrapper({ INC().execute(absoluteAddressing()) }, INC().opcodeName, "abs"),

        (0xF0u).toUByte() to InstructionWrapper({ BEQ().execute(relativeAddressing()) }, BEQ().opcodeName, "rel"),
        (0xF1u).toUByte() to InstructionWrapper({ SBC().execute(indirectYIndexedAddressing()) }, SBC().opcodeName, "ind y"),
        (0xF5u).toUByte() to InstructionWrapper({ SBC().execute(zeroPageXIndexedAddressing()) }, SBC().opcodeName, "zpg x"),
        (0xF6u).toUByte() to InstructionWrapper({ INC().execute(zeroPageXIndexedAddressing()) }, INC().opcodeName, "zpg x"),
        (0xF8u).toUByte() to InstructionWrapper({ SED().execute() }, SED().opcodeName, "impl"),
        (0xF9u).toUByte() to InstructionWrapper({ SBC().execute(absoluteYIndexedAddressing()) }, SBC().opcodeName, "abs y"),
        (0xFDu).toUByte() to InstructionWrapper({ SBC().execute(absoluteXIndexedAddressing()) }, SBC().opcodeName, "abs x"),
        (0xFEu).toUByte() to InstructionWrapper({ INC().execute(absoluteXIndexedAddressing()) }, INC().opcodeName, "abs x"),
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
    fun absoluteAddressing(): UShort {
        programCounter++
        val leastSignificantByte = readAddress(programCounter).toUShort()
        programCounter++
        val mostSignificantByte = readAddress((programCounter)).toUInt()
        return ((mostSignificantByte shl 8).toUShort() or leastSignificantByte)
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
    inner class ADC(): Instruction() {
        override val opcodeName = "ADC"

        fun execute(operand: UByte) {
            val signBitMask: UByte = 0x80u
            val accumulatorSignedBit = this@CPU6502.accumulator and signBitMask == signBitMask
            val operandSignedBit = operand and signBitMask == signBitMask

            val rawResult = this@CPU6502.accumulator + operand + (if (carryFlag) 1u else 0u)
            val result = rawResult.toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (rawResult shr 8) == 1u
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            if (accumulatorSignedBit != operandSignedBit) {
                this@CPU6502.overflowFlag = false
                return
            }

            if (accumulatorSignedBit == this@CPU6502.negativeFlag) {
                this@CPU6502.overflowFlag = false
                return
            }

            this@CPU6502.overflowFlag = true
        }

        fun execute(targetAddress: UShort) {
            val signBitMask: UByte = 0x80u
            val operand: UByte = this@CPU6502.readAddress(targetAddress)

            val accumulatorSignedBit = this@CPU6502.accumulator and signBitMask == signBitMask
            val operandSignedBit = operand and signBitMask == signBitMask

            val rawResult = this@CPU6502.accumulator + operand + (if (carryFlag) 1u else 0u)
            val result = rawResult.toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (rawResult shr 8) == 1u
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            if (accumulatorSignedBit != operandSignedBit) {
                this@CPU6502.overflowFlag = false
                return
            }

            if (accumulatorSignedBit == this@CPU6502.negativeFlag) {
                this@CPU6502.overflowFlag = false
                return
            }

            this@CPU6502.overflowFlag = true
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
        override val opcodeName = "AND"

        fun execute(operand: UByte) {
            val result: UByte = this@CPU6502.accumulator and operand
            this@CPU6502.accumulator = result

            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
        }

        fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val result: UByte = this@CPU6502.accumulator and operand
            this@CPU6502.accumulator = result

            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
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
        override val opcodeName = "ASL"

        fun execute() {
            val data: UInt = this@CPU6502.accumulator.toUInt()
            val result: UByte = (data shl 1).toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (data shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
        }
        fun execute(targetAddress: UShort) {
            val data: UInt = this@CPU6502.readAddress(targetAddress).toUInt()
            val result: UByte = (data shl 1).toUByte()
            this@CPU6502.writeToAddress(targetAddress, result)

            this@CPU6502.carryFlag = (data shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
        }
    }

    /**
     * Branch on Clear Carry
     * Sets program counter to target address when carryFlag = 0
     */
    inner class BCC(): Instruction() {
        override val opcodeName = "BCC"

        fun execute(targetAddress: UShort) {
            if (!this@CPU6502.carryFlag) this@CPU6502.programCounter = targetAddress
        }
    }

    /**
     * Branch on Set Carry
     * Sets program counter to target address when carryFlag = 1
     */
    inner class BCS(): Instruction() {
        override val opcodeName = "BCS"

        fun execute(targetAddress: UShort) {
            if (this@CPU6502.carryFlag) this@CPU6502.programCounter = targetAddress
        }
    }

    /**
     * Branch on Result Zero
     * Sets program counter to target address when zeroFlag = 1
     */
    inner class BEQ(): Instruction() {
        override val opcodeName = "BEQ"

        fun execute(targetAddress: UShort) {
            if (this@CPU6502.zeroFlag) this@CPU6502.programCounter = targetAddress
        }
    }

    /**
     * Test Bits in Memory with Accumulator
     * Bit 7 of operand toggles the negative flag;
     * Bit 6 of perand toggles the overflow flag
     * the zero-flag is set to the result of operand AND accumulator.
     */
    inner class BIT(): Instruction() {
        override val opcodeName = "BIT"

        fun execute(targetAddress: UShort) {
            val operand: UInt = readAddress(targetAddress).toUInt()
            val result: UByte = this@CPU6502.accumulator and operand.toUByte()

            this@CPU6502.negativeFlag = (operand shr 7) == 1u
            this@CPU6502.overflowFlag = (operand and 0x40u) != 0u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
        }
    }

    /**
     * Branch on Result Minus
     * Sets program counter to target address when negativeFlag = 1
     */
    inner class BMI(): Instruction() {
        override val opcodeName = "BMI"

        fun execute(targetAddress: UShort) {
            if (this@CPU6502.negativeFlag) this@CPU6502.programCounter = targetAddress
        }
    }

    /**
     * Branch on Result Not Zero
     * Sets program counter to target address when zeroFlag = 0
     */
    inner class BNE(): Instruction() {
        override val opcodeName = "BNE"

        fun execute(targetAddress: UShort) {
            if (!this@CPU6502.zeroFlag) this@CPU6502.programCounter = targetAddress
        }
    }

    /**
     * Branch on Result Plus
     * Sets program counter to target address when negativeFlag = 0
     */
    inner class BPL(): Instruction() {
        override val opcodeName = "BPL"

        fun execute(targetAddress: UShort) {
            if (!this@CPU6502.negativeFlag) this@CPU6502.programCounter = targetAddress
        }
    }

    /**
     * Break Command
     * performs a programed interrupt similar to IRQ
     */
    inner class BRK(): Instruction() {
        override val opcodeName = "BRK"

        fun execute() {
            val vectorLeastSignificantByte = readAddress(0xFFFEu)
            val vectorMostSignificantByte = readAddress(0xFFFFu)

            this@CPU6502.programCounter++
            this@CPU6502.programCounter++
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
    }

    /**
     * Branch on Overflow Clear
     * Sets program counter to target address when overFlow = 0
     */
    inner class BVC(): Instruction() {
        override val opcodeName = "BVC"

        fun execute(targetAddress: UShort) {
            if (!this@CPU6502.overflowFlag) this@CPU6502.programCounter = targetAddress
        }
    }

    /**
     * Branch on Overflow Set
     * Sets program counter to target address when overFlow = 1
     */
    inner class BVS(): Instruction() {
        override val opcodeName = "BVS"

        fun execute(targetAddress: UShort) {
            if (this@CPU6502.overflowFlag) this@CPU6502.programCounter = targetAddress
        }
    }

    /**
     * Clear Carry Flag
     * This instruction initializes the carry flag to a 0.
     * This instruction affects no registers in the microprocessor and no flags other than the carry flag which is reset.
     */
    inner class CLC(): Instruction() {
        override val opcodeName = "CLC"

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
        override val opcodeName = "CLD"

        fun execute() {
            this@CPU6502.decimalFlag = false
        }
    }

    /**
     * Clear Interrupt Disable
     * This instruction initializes the interrupt disable to a 0. This allows the microprocessor to receive interrupts.
     * It affects no registers in the microprocessor and no flags other than the interrupt disable which is cleared.
     */
    inner class CLI(): Instruction() {
        override val opcodeName = "CLI"

        fun execute() {
            this@CPU6502.interruptDisableFlag = false
        }
    }


    /**
     * Clear Overflow Flag
     * This instruction clears the overflow flag to a 0.
     * CLV affects no registers in the microprocessor and no flags other than the overflow flag which is set to a 0.
     */
    inner class CLV(): Instruction() {
        override val opcodeName = "CLV"

        fun execute() {
            this@CPU6502.overflowFlag = false
        }
    }

    /**
     * Compare Memory and Accumulator
     * subtracts the value in memory from the accumulator without storing the result.
     * zero flag is set when operands are equal.
     * negative flag set if result is negative.
     * carry flag set if register >= operand
     */
    inner class CMP(): Instruction() {
        override val opcodeName = "CMP"

        fun execute(operand: UByte) {
            val signBitMask: UByte = 0x80u
            val rawResult = (this@CPU6502.accumulator.toByte() - operand.toByte()).toUInt()
            val result = rawResult.toUByte()

            this@CPU6502.carryFlag = this@CPU6502.accumulator >= operand
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
        }

        fun execute(targetAddress: UShort) {
            val signBitMask: UByte = 0x80u
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val rawResult = (this@CPU6502.accumulator.toByte() - operand.toByte()).toUInt()
            val result = rawResult.toUByte()

            this@CPU6502.carryFlag = this@CPU6502.accumulator >= operand
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
        }
    }

    /**
     * Compare Memory and X Register
     * subtracts the value in memory from the register without storing the result.
     * zero flag is set when operands are equal.
     * negative flag set if result is negative.
     * carry flag set if register >= operand
     */
    inner class CPX(): Instruction() {
        override val opcodeName = "CPX"

        fun execute(operand: UByte) {
            val signBitMask: UByte = 0x80u
            val rawResult = (this@CPU6502.xRegister.toByte() - operand.toByte()).toUInt()
            val result = rawResult.toUByte()

            this@CPU6502.carryFlag = this@CPU6502.xRegister >= operand
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
        }

        fun execute(targetAddress: UShort) {
            val signBitMask: UByte = 0x80u
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val rawResult = (this@CPU6502.xRegister.toByte() - operand.toByte()).toUInt()
            val result = rawResult.toUByte()

            this@CPU6502.carryFlag = this@CPU6502.xRegister >= operand
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
        }
    }

    /**
     * Compare Memory and Y Register
     * subtracts the value in memory from the register without storing the result.
     * zero flag is set when operands are equal.
     * negative flag set if result is negative.
     * carry flag set if register >= operand
     */
    inner class CPY(): Instruction() {
        override val opcodeName = "CPY"

        fun execute(operand: UByte) {
            val signBitMask: UByte = 0x80u
            val rawResult = (this@CPU6502.yRegister.toByte() - operand.toByte()).toUInt()
            val result = rawResult.toUByte()

            this@CPU6502.carryFlag = this@CPU6502.yRegister >= operand
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
        }

        fun execute(targetAddress: UShort) {
            val signBitMask: UByte = 0x80u
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val rawResult = (this@CPU6502.yRegister.toByte() - operand.toByte()).toUInt()
            val result = rawResult.toUByte()

            this@CPU6502.carryFlag = this@CPU6502.yRegister >= operand
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()        }
    }

    inner class DEC(): Instruction() {
        override val opcodeName = "DEC"

        fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val result = operand.dec()
            this@CPU6502.writeToAddress(targetAddress, result)

            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
        }
    }

    inner class DEX(): Instruction() {
        override val opcodeName = "DEX"

        fun execute() {
            this@CPU6502.xRegister--

            this@CPU6502.zeroFlag = this@CPU6502.xRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.xRegister.toUInt() shr 7) == 1u
        }
    }

    inner class DEY(): Instruction() {
        override val opcodeName = "DEY"

        fun execute() {
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
        override val opcodeName = "EOR"

        fun execute(operand: UByte) {
            val result: UByte = this@CPU6502.accumulator xor operand
            this@CPU6502.accumulator = result

            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
        }

        fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
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
        override val opcodeName = "INC"

        fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val result = operand.inc()
            this@CPU6502.writeToAddress(targetAddress, result)

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
        override val opcodeName = "INX"

        fun execute() {
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
        override val opcodeName = "INY"

        fun execute() {
            this@CPU6502.yRegister++

            this@CPU6502.zeroFlag = this@CPU6502.yRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.yRegister.toUInt() shr 7) == 1u
        }
    }

    inner class JMP(): Instruction() {
        override val opcodeName = "JMP"

        fun execute(targetAddress: UShort) {
            this@CPU6502.programCounter = targetAddress
        }
    }


    /**
     * Jump to Subroutine
     * jumps program counter to target address, but first saves last address of current instruction to stack.
     * Decrements the stack twice in the process.
     */
    inner class JSR(): Instruction() {
        override val opcodeName = "JSR"

        fun execute(targetAddress: UShort) {
            val currentAddressMostSignificantByte: UByte = (this@CPU6502.programCounter.toUInt() shr 8).toUByte()
            val currentAddressLeastSignificantByte: UByte = this@CPU6502.programCounter.toUByte()

            this@CPU6502.writeToAddress(stackPointer.toUShort(), currentAddressMostSignificantByte)
            this@CPU6502.stackPointer--
            this@CPU6502.writeToAddress(stackPointer.toUShort(), currentAddressLeastSignificantByte)
            this@CPU6502.stackPointer--

            this@CPU6502.programCounter = targetAddress
        }
    }

    /**
     * Load Accumulator
     * Load the accumulator from memory
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class LDA(): Instruction() {
        override val opcodeName = "LDA"

        fun execute(operand: UByte) {
            this@CPU6502.accumulator = operand

            this@CPU6502.zeroFlag = operand == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (operand.toUInt() shr 7) == 1u        }

        fun execute(targetAddress: UShort) {
            val data: UByte = readAddress(targetAddress)
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
        override val opcodeName = "LDX"

        fun execute(operand: UByte) {
            this@CPU6502.xRegister = operand

            this@CPU6502.zeroFlag = operand == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (operand.toUInt() shr 7) == 1u
        }

        fun execute(targetAddress: UShort) {
            val data: UByte = readAddress(targetAddress)
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
        override val opcodeName = "LDY"

        fun execute(operand: UByte) {
            this@CPU6502.yRegister = operand

            this@CPU6502.zeroFlag = operand == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (operand.toUInt() shr 7) == 1u
        }

        fun execute(targetAddress: UShort) {
            val data: UByte = readAddress(targetAddress)
            this@CPU6502.yRegister = data

            this@CPU6502.zeroFlag = data == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (data.toUInt() shr 7) == 1u
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
        override val opcodeName = "LSR"

        fun execute() {
            val data: UInt = this@CPU6502.accumulator.toUInt()
            val result: UByte = (data shr 1).toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = false
        }

        fun execute(targetAddress: UShort){
            val data: UInt = this@CPU6502.readAddress(targetAddress).toUInt()
            val result: UByte = (data shr 1).toUByte()
            this@CPU6502.writeToAddress(targetAddress, result)

            this@CPU6502.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = false
        }
    }

    /**
     * No Operation
     */
    inner class NOP(): Instruction() {
        override val opcodeName = "NOP"

        fun execute() {
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
        override val opcodeName = "ORA"

        fun execute(operand: UByte) {
            val result: UByte = this@CPU6502.accumulator or operand
            this@CPU6502.accumulator = result

            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
        }

        fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
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
        override val opcodeName = "PHA"

        fun execute() {
            this@CPU6502.writeToAddress(stackPointer.toUShort(), this@CPU6502.accumulator)
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
        override val opcodeName = "PHP"

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

            this@CPU6502.writeToAddress(this@CPU6502.stackPointer.toUShort(), result)
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
        override val opcodeName = "PLA"

        fun execute() {
            this@CPU6502.stackPointer++
            val data: UByte = this@CPU6502.readAddress(stackPointer.toUShort())
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
        override val opcodeName = "PLP"

        fun execute() {
            this@CPU6502.stackPointer++
            val data: UByte = this@CPU6502.readAddress(stackPointer.toUShort())

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
     */
    inner class ROL(): Instruction() {
        override val opcodeName = "ROL"

        fun execute() {
            val data: UInt = this@CPU6502.accumulator.toUInt()
            val result: UByte = if (carryFlag) ((data shl 1) or (1u)).toUByte() else (data shl 1).toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (data shr 7).toUByte() == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()
        }

        fun execute(targetAddress: UShort) {
            val data: UInt = this@CPU6502.readAddress(targetAddress).toUInt()
            val result: UByte = if (carryFlag) ((data shl 1) or (1u)).toUByte() else (data shl 1).toUByte()
            this@CPU6502.writeToAddress(targetAddress, result)

            this@CPU6502.carryFlag = (data shr 7).toUByte() == (1u).toUByte()
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
        override val opcodeName = "ROR"

        fun execute() {
            val data: UInt = this@CPU6502.accumulator.toUInt()
            val result: UByte = if (carryFlag) ((data shr 1) or (0x80u)).toUByte() else (data shr 1).toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()
        }

        fun execute(targetAddress: UShort) {
            val data: UInt = this@CPU6502.readAddress(targetAddress).toUInt()
            val result: UByte = if (carryFlag) ((data shr 1) or (0x80u)).toUByte() else (data shr 1).toUByte()
            this@CPU6502.writeToAddress(targetAddress, result)

            this@CPU6502.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()
        }
    }

    /**
     * Return From Interrupt
     * restores program counter and status register from stack.
     */
    inner class RTI(): Instruction() {
        override val opcodeName = "RTI"

        fun execute() {
            this@CPU6502.stackPointer++
            val statusRegisterValue = this@CPU6502.readAddress(stackPointer.toUShort())
            this@CPU6502.stackPointer++
            val targetLeastSignificantByte = this@CPU6502.readAddress(stackPointer.toUShort())
            this@CPU6502.stackPointer++
            val targetMostSignificantByte = this@CPU6502.readAddress(stackPointer.toUShort())

            val negativeBitMask: UByte = 0x80u
            val overflowBitMask: UByte = 0x40u
            val extraBitMask: UByte = 0x20u
            val breakBitMask: UByte = 0x10u
            val decimalBitMask: UByte = 0x08u
            val interruptDisableBitMask: UByte = 0x04u
            val zeroBitMask: UByte = 0x02u
            val carryBitMask: UByte = 0x01u

            this@CPU6502.negativeFlag = statusRegisterValue and negativeBitMask == negativeBitMask
            this@CPU6502.overflowFlag = statusRegisterValue and overflowBitMask == overflowBitMask
            this@CPU6502.extraFlag = statusRegisterValue and extraBitMask == extraBitMask
            this@CPU6502.breakFlag = statusRegisterValue and breakBitMask == breakBitMask
            this@CPU6502.decimalFlag = statusRegisterValue and decimalBitMask == decimalBitMask
            this@CPU6502.interruptDisableFlag = statusRegisterValue and interruptDisableBitMask == interruptDisableBitMask
            this@CPU6502.zeroFlag = statusRegisterValue and zeroBitMask == zeroBitMask
            this@CPU6502.carryFlag = statusRegisterValue and carryBitMask == carryBitMask

            this@CPU6502.programCounter = ((targetMostSignificantByte.toUInt() shl 8) + targetLeastSignificantByte).toUShort()
        }
    }

    /**
     * Return From Subroutine
     * restores program counter from stack.
     */
    inner class RTS(): Instruction() {
        override val opcodeName = "RTS"

        fun execute() {
            this@CPU6502.stackPointer++
            val targetLeastSignificantByte = this@CPU6502.readAddress(stackPointer.toUShort())
            this@CPU6502.stackPointer++
            val targetMostSignificantByte = this@CPU6502.readAddress(stackPointer.toUShort())

            this@CPU6502.programCounter = ((targetMostSignificantByte.toUInt() shl 8) + targetLeastSignificantByte).toUShort()
            this@CPU6502.programCounter++
        }
    }

    /**
     * Subtract with Carry
     * Subtract accumulator with memory and carry-bit complement.
     * negativeFlag and zeroFlag are set by the result.
     * carry is called when unsigned values overflow 255.
     * overflow only can occur when subtracting positive from negative and visa versa.
     */
    inner class SBC(): Instruction() {
        override val opcodeName = "SBC"

        fun execute(operand: UByte) {
            val signBitMask: UByte = 0x80u
            val accumulatorSignedBit = this@CPU6502.accumulator and signBitMask == signBitMask
            val operandSignedBit = operand and signBitMask == signBitMask

            val rawResult = this@CPU6502.accumulator - operand - (if (carryFlag) 0u else 1u)
            val result = rawResult.toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (rawResult shr 8) == 1u
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            if (accumulatorSignedBit == operandSignedBit) {
                this@CPU6502.overflowFlag = false
                return
            }

            if (accumulatorSignedBit != this@CPU6502.negativeFlag) {
                this@CPU6502.overflowFlag = true
                return
            }

            this@CPU6502.overflowFlag = false
        }

        fun execute(targetAddress: UShort) {
            val operand = this@CPU6502.readAddress(targetAddress)
            val signBitMask: UByte = 0x80u
            val accumulatorSignedBit = this@CPU6502.accumulator and signBitMask == signBitMask
            val operandSignedBit = operand and signBitMask == signBitMask

            val rawResult = this@CPU6502.accumulator - operand - (if (carryFlag) 0u else 1u)
            val result = rawResult.toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (rawResult shr 8) == 1u
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            if (accumulatorSignedBit == operandSignedBit) {
                this@CPU6502.overflowFlag = false
                return
            }

            if (accumulatorSignedBit != this@CPU6502.negativeFlag) {
                this@CPU6502.overflowFlag = true
                return
            }

            this@CPU6502.overflowFlag = false
        }
    }

    /**
     * Set Carry Flag
     * This instruction initializes the carry flag to a 1.
     * This instruction affects no registers in the microprocessor and no flags other than the carry flag which is set.
     */
    inner class SEC(): Instruction() {
        override val opcodeName = "SEC"

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
        override val opcodeName = "SED"

        fun execute() {
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
        override val opcodeName = "SEI"

        fun execute() {
            this@CPU6502.interruptDisableFlag = true
        }
    }

    /**
     * Store Accumulator In Memory
     * This instruction transfers the contents of the accumulator to memory.
     * This instruction affects none of the flags in the processor status register and does not affect the accumulator.
     */
    inner class STA(): Instruction() {
        override val opcodeName = "STA"

        fun execute(targetAddress: UShort) {
            this@CPU6502.writeToAddress(targetAddress, this@CPU6502.accumulator)
        }
    }

    /**
     * Store Register X in Memory
     * Transfers value of X register to addressed memory location.
     * No flags or registers in the microprocessor are affected by the store operation.
     */
    inner class STX(): Instruction() {
        override val opcodeName = "STX"

        fun execute(targetAddress: UShort) {
            this@CPU6502.writeToAddress(targetAddress, this@CPU6502.xRegister)
        }
    }

    /**
     * Store Register Y in Memory
     * Transfer the value of the Y register to the addressed memory location.
     * STY does not affect any flags or registers in the microprocessor.
     */
    inner class STY(): Instruction() {
        override val opcodeName = "STY"

        fun execute(targetAddress: UShort) {
            this@CPU6502.writeToAddress(targetAddress, this@CPU6502.yRegister)
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
        override val opcodeName = "TAX"

        fun execute() {
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
        override val opcodeName = "TAY"

        fun execute() {
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
        override val opcodeName = "TSX"

        fun execute() {
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
        override val opcodeName = "TXA"

        fun execute() {
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
        override val opcodeName = "TXS"

        fun execute() {
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
        override val opcodeName = "TYA"

        fun execute() {
            this@CPU6502.accumulator = this@CPU6502.yRegister
            this@CPU6502.zeroFlag = this@CPU6502.accumulator == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.accumulator.toUInt() shr 7) == 1u
        }
    }


    /**
     * Disassemble
     * Traverses program code and disassembles it for ui viewing.
     * This will be run once a cpu is instantiated, so you should call
     * reset() before using the cpu.
     */
    fun disassemble(): Map<UShort, DisassembledInstruction> {
        val disassembledProgram = mutableMapOf<UShort, DisassembledInstruction>()

        while(programCounter.toInt() <= 0xFFFF) {
            val opcodeLocation = programCounter
            val opcode: UByte = readAddress(opcodeLocation)
            val decodedInstruction = fetchInstruction(opcode)
            disassembledProgram[opcodeLocation] = DisassembledInstruction(
                decodedInstruction.opcodeName,
                decodedInstruction.addressingMode,
                decodedInstruction.operand
            )

            decodedInstruction.also {
                if (
                    it.addressingMode == "impl" ||
                    it.addressingMode == "A"
                ) {
                    programCounter++
                    if (it.opcodeName == "BRK") programCounter++
                }

                if (
                    it.addressingMode == "imm" ||
                    it.addressingMode == "zpg" ||
                    it.addressingMode == "zpg x" ||
                    it.addressingMode == "zpg y" ||
                    it.addressingMode == "x ind" ||
                    it.addressingMode == "ind y" ||
                    it.addressingMode == "rel"
                ) {
                    programCounter++
                    programCounter++
                }

                if (
                    it.addressingMode == "abs" ||
                    it.addressingMode == "abs x" ||
                    it.addressingMode == "abs y" ||
                    it.addressingMode == "ind"
                ) {
                    programCounter++
                    programCounter++
                    programCounter++
                }
            }

        }

        return disassembledProgram
    }

    private var disassembledProgram: Map<UShort, DisassembledInstruction>

    init {
        reset()
        disassembledProgram = disassemble()
        reset()
    }



}