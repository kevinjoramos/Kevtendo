package CPU

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
        // get current state of cpu for disassembly
        val startingProgramCounter = programCounter
        val startingStackPointer = stackPointer
        val startingAccumulator = accumulator
        val startingXRegister = xRegister
        val startingYRegister = yRegister
        val startingStatusRegister = statusRegister

        // get opcode from the memory address pointed at by program counter.
        val opcodeValue: UByte = readAddress(startingProgramCounter)
        //println("PC = ${programCounter.to4DigitHexString()}. Opcode = ${opcode.to2DigitHexString()}")

        // get the addressing mode high order, instruction high order, and name of the opcode
        val instruction = fetchInstruction(opcodeValue)

        // execute functions
        instruction.executionFunction.invoke()

        //disassemble

        //interruptSignalTriage.map { it.invoke() }
    }

    private fun disassembleInstruction(
        programCounter: UShort,
        opcodeHexValue: UByte,
        operandLowByte: UByte,
        operandHighByte: UByte,
        opcodeName: String,
        addressingMode: () -> Unit,
        effectiveAddress: UShort,
        accumulatorValue: UByte,
        xRegisterValue: UByte,
        yRegisterValue: UByte,
        statusRegisterValue: UByte,
        stackPointerValue: UByte,
    ) {

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

    private val opcodeTable: Map<UByte, Any> = mapOf(
        (0x00u).toUByte() to Instruction(AddressingMode.IMP, , this),
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





    val disassembledProgram: MutableMap<UShort, String> = mutableMapOf()

    /**
     * Disassemble
     * Traverses program code and disassembles it for ui viewing.
     * This will be run once a cpu is instantiated, so you should call
     * reset() before using the cpu.
     */
    fun disassemble(opcodeLocation: UShort, instruction: InstructionWrapper): MutableMap<UShort, String> {
        disassembledProgram[opcodeLocation] =
            "$${opcodeLocation.to4DigitHexString()}: ${instruction.opcodeName} ${instruction.addressingMode}"


        return disassembledProgram
    }


}