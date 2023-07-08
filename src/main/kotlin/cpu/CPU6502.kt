package cpu

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import mediator.Component
import mediator.Mediator
import util.Logger
import util.to2DigitHexString
import util.to4DigitHexString
import java.util.SortedMap

/**
 * Emulation of the 6502 processor.
 * 1. reads the opcode and operands from memory
 * 2. executes the instruction and changes its internal state.
 * 3. writes to memory as necessary.
 *
 * TODO("implement control signals")
 * TODO("implement clock cycles")
 */
@ExperimentalUnsignedTypes
class CPU6502(override var bus: Mediator) : Component {

    /**
     * 6502 Architecture components
     */
    var programCounter: UShort = 0xC000u
        set(value) {
            field = value
            _programCounterState.value = programCounter.to4DigitHexString()
            _instructionState3.value = disassembler.allInstructions[(programCounter - 2u).toUShort()] ?: "${programCounter.to4DigitHexString()}:"
            _instructionState3.value = disassembler.allInstructions[(programCounter - 1u).toUShort()] ?: "${programCounter.to4DigitHexString()}:"
            _instructionState3.value = disassembler.allInstructions[programCounter] ?: "${programCounter.to4DigitHexString()}:"
            _instructionState4.value = disassembler.allInstructions[(programCounter + 1u).toUShort()] ?: "${programCounter.to4DigitHexString()}:"
            _instructionState5.value = disassembler.allInstructions[(programCounter + 2u).toUShort()] ?: "${programCounter.to4DigitHexString()}:"
            _instructionState6.value = disassembler.allInstructions[(programCounter + 3u).toUShort()] ?: "${programCounter.to4DigitHexString()}:"
        }
    private val _programCounterState = MutableStateFlow("0000")
    val programCounterState = _programCounterState.asStateFlow()

    var stackPointer: UShort = 0x0000u
        set(value: UShort) {
            field = (0x0100u).toUShort() or value.toUByte().toUShort()
            _stackPointerState.value = stackPointer.to4DigitHexString()
        }
    private val _stackPointerState = MutableStateFlow("0000")
    val stackPointerState = _stackPointerState.asStateFlow()

    var accumulator: UByte = 0x00u
        set(value) {
            field = value
            _accumulatorState.value = accumulator.to4DigitHexString()
        }
    private val _accumulatorState = MutableStateFlow("00")
    val accumulatorState = _accumulatorState.asStateFlow()

    var xRegister: UByte = 0x00u
        set(value) {
            field = value
            _xRegisterState.value = xRegister.to2DigitHexString()
        }
    private val _xRegisterState = MutableStateFlow("00")
    val xRegisterState = _xRegisterState.asStateFlow()

    var yRegister: UByte = 0x00u
        set(value) {
            field = value
            _yRegisterState.value = yRegister.to2DigitHexString()
        }
    val _yRegisterState = MutableStateFlow("00")
    val yRegisterState = _yRegisterState.asStateFlow()

    var statusRegister: UByte = 0x34u
    val _statusRegisterState = MutableStateFlow(statusRegister)
    val statusRegisterState = _statusRegisterState.asStateFlow()

    var negativeFlag: Boolean
        get() = getFlagValue(NEGATIVE_BITMASK)
        set(value) {
            setFlagValue(value, NEGATIVE_BITMASK)
            _negativeFlagState.value = negativeFlag
        }
    private val _negativeFlagState = MutableStateFlow(false)
    val negativeFlagState = _negativeFlagState.asStateFlow()

    var overflowFlag: Boolean
        get() = getFlagValue(OVERFLOW_BITMASK)
        set(value) {
            setFlagValue(value, OVERFLOW_BITMASK)
        }
    private val _overflowFlagState = MutableStateFlow(false)
    val overflowFlagState = _overflowFlagState.asStateFlow()

    var extraFlag: Boolean
        get() = getFlagValue(EXTRA_BITMASK)
        set(value) {
            setFlagValue(value, EXTRA_BITMASK)
            _extraFlagState.value = extraFlag
        }
    private val _extraFlagState = MutableStateFlow(false)
    val extraFlagState = _extraFlagState.asStateFlow()

    var breakFlag: Boolean
        get() = getFlagValue(BREAK_BITMASK)
        set(value) {
            setFlagValue(value, BREAK_BITMASK)
            _breakFlagState.value = breakFlag
        }
    private val _breakFlagState = MutableStateFlow(false)
    val breakFlagState = _breakFlagState.asStateFlow()

    var decimalFlag: Boolean
        get() = getFlagValue(DECIMAL_BITMASK)
        set(value) {
            setFlagValue(value, DECIMAL_BITMASK)
            _decimalFlagState.value = decimalFlag
        }
    private val _decimalFlagState = MutableStateFlow(false)
    val decimalFlagState = _decimalFlagState.asStateFlow()

    var interruptDisableFlag: Boolean
        get() = getFlagValue(INTERRUPT_DISABLE_BITMASK)
        set(value) {
            setFlagValue(value, INTERRUPT_DISABLE_BITMASK)
            _interruptDisableFlagState.value = interruptDisableFlag
        }
    private val _interruptDisableFlagState = MutableStateFlow(false)
    val interruptDisableFlagState = _interruptDisableFlagState.asStateFlow()

    var zeroFlag: Boolean
        get() = getFlagValue(ZERO_BITMASK)
        set(value) {
            setFlagValue(value, ZERO_BITMASK)
            _zeroFlagState.value = zeroFlag
        }
    private val _zeroFlagState = MutableStateFlow(false)
    val zeroFlagState = _zeroFlagState.asStateFlow()

    var carryFlag: Boolean
        get() = getFlagValue(CARRY_BITMASK)
        set(value) {
            setFlagValue(value, CARRY_BITMASK)
            _carryFlagState.value = carryFlag
        }
    private val _carryFlagState = MutableStateFlow(carryFlag)
    val carryFlagState = _carryFlagState.asStateFlow()

    private fun getFlagValue(bitMask: UByte): Boolean = (statusRegister and bitMask) == bitMask

    private fun setFlagValue(value: Boolean, bitMask: UByte) {
        statusRegister = if (value) statusRegister or bitMask else statusRegister and bitMask.inv()
    }

    private val _instructionState1 = MutableStateFlow("$0000:")
    private val _instructionState2 = MutableStateFlow("$0000:")
    private val _instructionState3 = MutableStateFlow("$0000:")
    private val _instructionState4 = MutableStateFlow("$0000:")
    private val _instructionState5 = MutableStateFlow("$0000:")
    private val _instructionState6 = MutableStateFlow("$0000:")

    val instructionState1 = _instructionState1.asStateFlow()
    val instructionState2 = _instructionState2.asStateFlow()
    val instructionState3 = _instructionState3.asStateFlow()
    val instructionState4 = _instructionState4.asStateFlow()
    val instructionState5 = _instructionState5.asStateFlow()
    val instructionState6 = _instructionState6.asStateFlow()

    /**
     * Other member variables for developer convenience
     */
    private var opcodeValue: UByte= 0x00u
    private var operandLowByte: UByte? = null
    private var operandHighByte: UByte? = null
    private var targetAddress: UShort? = null
    private var immediateOperand: UByte? = null

    private var cycleCount: Int = 0
    private var hasOverflowCycle = false

    var isPendingNMI = false
    var isPendingIRQ = false
    var isPendingReset = false

    private var disassembler: Disassembler = Disassembler()

    private val opcodeTable: Map<UByte, Triple<AddressingMode, Operation, Int>> = mapOf(
        (0x00u).toUByte() to Triple(AddressingMode.IMP, BRK(), 7),
        (0x01u).toUByte() to Triple(AddressingMode.X_IND, ORA(), 6),
        (0x05u).toUByte() to Triple(AddressingMode.ZPG, ORA(), 3),
        (0x06u).toUByte() to Triple(AddressingMode.ZPG, ASL(), 5),
        (0x08u).toUByte() to Triple(AddressingMode.IMP, PHP(), 3),
        (0x09u).toUByte() to Triple(AddressingMode.IMM, ORA(), 2),
        (0x0Au).toUByte() to Triple(AddressingMode.A, ASL(), 2),
        (0x0Du).toUByte() to Triple(AddressingMode.ABS, ORA(), 4),
        (0x0Eu).toUByte() to Triple(AddressingMode.ABS, ASL(), 6),
        (0x10u).toUByte() to Triple(AddressingMode.REL, BPL(), 2),
        (0x11u).toUByte() to Triple(AddressingMode.IND_Y, ORA(), 5),
        (0x15u).toUByte() to Triple(AddressingMode.ZPG_X, ORA(), 4),
        (0x16u).toUByte() to Triple(AddressingMode.ZPG_X, ASL(), 6),
        (0x18u).toUByte() to Triple(AddressingMode.IMP, CLC(), 2),
        (0x19u).toUByte() to Triple(AddressingMode.ABS_Y, ORA(), 4),
        (0x1Du).toUByte() to Triple(AddressingMode.ABS_X, ORA(), 4),
        (0x1Eu).toUByte() to Triple(AddressingMode.ABS_X, ASL(), 7),

        (0x20u).toUByte() to Triple(AddressingMode.ABS, JSR(), 6),
        (0x21u).toUByte() to Triple(AddressingMode.X_IND, AND(), 6),
        (0x24u).toUByte() to Triple(AddressingMode.ZPG, BIT(), 3),
        (0x25u).toUByte() to Triple(AddressingMode.ZPG, AND(), 3),
        (0x26u).toUByte() to Triple(AddressingMode.ZPG, ROL(), 5),
        (0x28u).toUByte() to Triple(AddressingMode.IMP, PLP(), 4),
        (0x29u).toUByte() to Triple(AddressingMode.IMM, AND(), 2),
        (0x2Au).toUByte() to Triple(AddressingMode.A, ROL(), 2),
        (0x2Cu).toUByte() to Triple(AddressingMode.ABS, BIT(), 4),
        (0x2Du).toUByte() to Triple(AddressingMode.ABS, AND(), 4),
        (0x2Eu).toUByte() to Triple(AddressingMode.ABS, ROL(), 6),

        (0x30u).toUByte() to Triple(AddressingMode.REL, BMI(), 2),
        (0x31u).toUByte() to Triple(AddressingMode.IND_Y, AND(), 5),
        (0x35u).toUByte() to Triple(AddressingMode.ZPG_X, AND(), 4),
        (0x36u).toUByte() to Triple(AddressingMode.ZPG_X, ROL(), 6),
        (0x38u).toUByte() to Triple(AddressingMode.IMP, SEC(), 2),
        (0x39u).toUByte() to Triple(AddressingMode.ABS_Y, AND(), 4),
        (0x3Du).toUByte() to Triple(AddressingMode.ABS_X, AND(), 4),
        (0x3Eu).toUByte() to Triple(AddressingMode.ABS_X, ROL(), 7),

        (0x40u).toUByte() to Triple(AddressingMode.IMP, RTI(), 6),
        (0x41u).toUByte() to Triple(AddressingMode.X_IND, EOR(), 6),
        (0x45u).toUByte() to Triple(AddressingMode.ZPG, EOR(), 3),
        (0x46u).toUByte() to Triple(AddressingMode.ZPG, LSR(), 5),
        (0x48u).toUByte() to Triple(AddressingMode.IMP, PHA(), 3),
        (0x49u).toUByte() to Triple(AddressingMode.IMM, EOR(), 2),
        (0x4Au).toUByte() to Triple(AddressingMode.A, LSR(), 2),
        (0x4Cu).toUByte() to Triple(AddressingMode.ABS, JMP(), 3),
        (0x4Du).toUByte() to Triple(AddressingMode.ABS, EOR(), 4),
        (0x4Eu).toUByte() to Triple(AddressingMode.ABS, LSR(), 6),

        (0x50u).toUByte() to Triple(AddressingMode.REL, BVC(), 2),
        (0x51u).toUByte() to Triple(AddressingMode.IND_Y, EOR(), 5),
        (0x55u).toUByte() to Triple(AddressingMode.ZPG_X, EOR(), 4),
        (0x56u).toUByte() to Triple(AddressingMode.ZPG_X, LSR(), 6),
        (0x58u).toUByte() to Triple(AddressingMode.IMP, CLI(), 2),
        (0x59u).toUByte() to Triple(AddressingMode.ABS_Y, EOR(), 4),
        (0x5Du).toUByte() to Triple(AddressingMode.ABS_X, EOR(), 4),
        (0x5Eu).toUByte() to Triple(AddressingMode.ABS_X, LSR(), 7),

        (0x60u).toUByte() to Triple(AddressingMode.IMP, RTS(), 6),
        (0x61u).toUByte() to Triple(AddressingMode.X_IND, ADC(), 6),
        (0x65u).toUByte() to Triple(AddressingMode.ZPG, ADC(), 3),
        (0x66u).toUByte() to Triple(AddressingMode.ZPG, ROR(), 5),
        (0x68u).toUByte() to Triple(AddressingMode.IMP, PLA(), 4),
        (0x69u).toUByte() to Triple(AddressingMode.IMM, ADC(), 2),
        (0x6Au).toUByte() to Triple(AddressingMode.A, ROR(), 2),
        (0x6Cu).toUByte() to Triple(AddressingMode.IND, JMP(), 5),
        (0x6Du).toUByte() to Triple(AddressingMode.ABS, ADC(), 4),
        (0x6Eu).toUByte() to Triple(AddressingMode.ABS, ROR(), 6),

        (0x70u).toUByte() to Triple(AddressingMode.REL, BVS(), 2),
        (0x71u).toUByte() to Triple(AddressingMode.IND_Y, ADC(), 5),
        (0x75u).toUByte() to Triple(AddressingMode.ZPG_X, ADC(), 4),
        (0x76u).toUByte() to Triple(AddressingMode.ZPG_X, ROR(), 6),
        (0x78u).toUByte() to Triple(AddressingMode.IMP, SEI(), 2),
        (0x79u).toUByte() to Triple(AddressingMode.ABS_Y, ADC(), 4),
        (0x7Du).toUByte() to Triple(AddressingMode.ABS_X, ADC(), 4),
        (0x7Eu).toUByte() to Triple(AddressingMode.ABS_X, ROR(), 7),

        (0x81u).toUByte() to Triple(AddressingMode.X_IND, STA(), 6),
        (0x84u).toUByte() to Triple(AddressingMode.ZPG, STY(), 3),
        (0x85u).toUByte() to Triple(AddressingMode.ZPG, STA(), 3),
        (0x86u).toUByte() to Triple(AddressingMode.ZPG, STX(), 3),
        (0x88u).toUByte() to Triple(AddressingMode.IMP, DEY(), 2),
        (0x8Au).toUByte() to Triple(AddressingMode.IMP, TXA(), 2),
        (0x8Cu).toUByte() to Triple(AddressingMode.ABS, STY(), 4),
        (0x8Du).toUByte() to Triple(AddressingMode.ABS, STA(), 4),
        (0x8Eu).toUByte() to Triple(AddressingMode.ABS, STX(), 4),

        (0x90u).toUByte() to Triple(AddressingMode.REL, BCC(), 2),
        (0x91u).toUByte() to Triple(AddressingMode.IND_Y, STA(), 6),
        (0x94u).toUByte() to Triple(AddressingMode.ZPG_X, STY(), 4),
        (0x95u).toUByte() to Triple(AddressingMode.ZPG_X, STA(), 4),
        (0x96u).toUByte() to Triple(AddressingMode.ZPG_Y, STX(), 4),
        (0x98u).toUByte() to Triple(AddressingMode.IMP, TYA(), 2),
        (0x99u).toUByte() to Triple(AddressingMode.ABS_Y, STA(), 5),
        (0x9Au).toUByte() to Triple(AddressingMode.IMP, TXS(), 2),
        (0x9Du).toUByte() to Triple(AddressingMode.ABS_X, STA(), 5),

        (0xA0u).toUByte() to Triple(AddressingMode.IMM, LDY(), 2),
        (0xA1u).toUByte() to Triple(AddressingMode.X_IND, LDA(), 6),
        (0xA2u).toUByte() to Triple(AddressingMode.IMM, LDX(), 2),
        (0xA4u).toUByte() to Triple(AddressingMode.ZPG, LDY(), 3),
        (0xA5u).toUByte() to Triple(AddressingMode.ZPG, LDA(), 3),
        (0xA6u).toUByte() to Triple(AddressingMode.ZPG, LDX(), 3),
        (0xA8u).toUByte() to Triple(AddressingMode.IMP, TAY(), 2),
        (0xA9u).toUByte() to Triple(AddressingMode.IMM, LDA(), 2),
        (0xAAu).toUByte() to Triple(AddressingMode.IMP, TAX(), 2),
        (0xACu).toUByte() to Triple(AddressingMode.ABS, LDY(), 4),
        (0xADu).toUByte() to Triple(AddressingMode.ABS, LDA(), 4),
        (0xAEu).toUByte() to Triple(AddressingMode.ABS, LDX(), 4),

        (0xB0u).toUByte() to Triple(AddressingMode.REL, BCS(), 2),
        (0xB1u).toUByte() to Triple(AddressingMode.IND_Y, LDA(), 5),
        (0xB4u).toUByte() to Triple(AddressingMode.ZPG_X, LDY(), 4),
        (0xB5u).toUByte() to Triple(AddressingMode.ZPG_X, LDA(), 4),
        (0xB6u).toUByte() to Triple(AddressingMode.ZPG_Y, LDX(), 4),
        (0xB8u).toUByte() to Triple(AddressingMode.IMP, CLV(), 2),
        (0xB9u).toUByte() to Triple(AddressingMode.ABS_Y, LDA(), 4),
        (0xBAu).toUByte() to Triple(AddressingMode.IMP, TSX(), 2),
        (0xBCu).toUByte() to Triple(AddressingMode.ABS_X, LDY(), 4),
        (0xBDu).toUByte() to Triple(AddressingMode.ABS_X, LDA(), 4),
        (0xBEu).toUByte() to Triple(AddressingMode.ABS_Y, LDX(), 4),

        (0xC0u).toUByte() to Triple(AddressingMode.IMM, CPY(), 2),
        (0xC1u).toUByte() to Triple(AddressingMode.X_IND, CMP(), 6),
        (0xC4u).toUByte() to Triple(AddressingMode.ZPG, CPY(), 3),
        (0xC5u).toUByte() to Triple(AddressingMode.ZPG, CMP(), 3),
        (0xC6u).toUByte() to Triple(AddressingMode.ZPG, DEC(), 5),
        (0xC8u).toUByte() to Triple(AddressingMode.IMP, INY(), 2),
        (0xC9u).toUByte() to Triple(AddressingMode.IMM, CMP(), 2),
        (0xCAu).toUByte() to Triple(AddressingMode.IMP, DEX(), 2),
        (0xCCu).toUByte() to Triple(AddressingMode.ABS, CPY(), 4),
        (0xCDu).toUByte() to Triple(AddressingMode.ABS, CMP(), 4),
        (0xCEu).toUByte() to Triple(AddressingMode.ABS, DEC(), 6),

        (0xD0u).toUByte() to Triple(AddressingMode.REL, BNE(), 7),
        (0xD1u).toUByte() to Triple(AddressingMode.IND_Y, CMP(), 5),
        (0xD5u).toUByte() to Triple(AddressingMode.ZPG_X, CMP(), 4),
        (0xD6u).toUByte() to Triple(AddressingMode.ZPG_X, DEC(), 6),
        (0xD8u).toUByte() to Triple(AddressingMode.IMP, CLD(), 2),
        (0xD9u).toUByte() to Triple(AddressingMode.ABS_Y, CMP(), 4),
        (0xDDu).toUByte() to Triple(AddressingMode.ABS_X, CMP(), 4),
        (0xDEu).toUByte() to Triple(AddressingMode.ABS_X, DEC(), 7),

        (0xE0u).toUByte() to Triple(AddressingMode.IMM, CPX(), 2),
        (0xE1u).toUByte() to Triple(AddressingMode.X_IND, SBC(), 6),
        (0xE4u).toUByte() to Triple(AddressingMode.ZPG, CPX(), 3),
        (0xE5u).toUByte() to Triple(AddressingMode.ZPG, SBC(), 3),
        (0xE6u).toUByte() to Triple(AddressingMode.ZPG, INC(), 5),
        (0xE8u).toUByte() to Triple(AddressingMode.IMP, INX(), 2),
        (0xE9u).toUByte() to Triple(AddressingMode.IMM, SBC(), 2),
        (0xEAu).toUByte() to Triple(AddressingMode.IMP, NOP(), 2),
        (0xECu).toUByte() to Triple(AddressingMode.ABS, CPX(), 4),
        (0xEDu).toUByte() to Triple(AddressingMode.ABS, SBC(), 4),
        (0xEEu).toUByte() to Triple(AddressingMode.ABS, INC(), 6),

        (0xF0u).toUByte() to Triple(AddressingMode.REL, BEQ(), 2),
        (0xF1u).toUByte() to Triple(AddressingMode.IND_Y, SBC(), 5),
        (0xF5u).toUByte() to Triple(AddressingMode.ZPG_X, SBC(), 4),
        (0xF6u).toUByte() to Triple(AddressingMode.ZPG_X, INC(), 6),
        (0xF8u).toUByte() to Triple(AddressingMode.IMP, SED(), 2),
        (0xF9u).toUByte() to Triple(AddressingMode.ABS_Y, SBC(), 4),
        (0xFDu).toUByte() to Triple(AddressingMode.ABS_X, SBC(), 4),
        (0xFEu).toUByte() to Triple(AddressingMode.ABS_X, INC(), 7),
    ).withDefault { Triple(AddressingMode.ILL, NOP(), 0) }

    init {
        disassembler.disassembleAssemblyCode(0x8000u, 0xFFFFu)
        reset()
    }

    fun run() {
        if (cycleCount == 0) {

            if (isPendingNMI) {
                isPendingNMI = false
                isPendingIRQ = false
                nmi()
            } else {
                this.opcodeValue = readAddress(programCounter)

                val (addressingMode, operation, cycles) = fetchInstruction(opcodeValue)

                cycleCount += cycles

                executeOperation(addressingMode, operation)

                operandLowByte = null
                operandHighByte = null
                targetAddress = null
                immediateOperand = null
            }
        }

        cycleCount--
    }

    /**
     * Three steps
     * 1. Decode byte to addressing mode
     * 2. fetch operands
     * 3. call the correct instruction.
     */
    private fun fetchInstruction(opcode: UByte): Triple<AddressingMode, Operation, Int> {
        return this.opcodeTable.getValue(opcode) //?: throw InvalidOpcodeException("Opcode $opcode not found in opcode table.")
    }

    fun executeOperation(addressingMode: AddressingMode, operation: Operation) {
        // Get current state for logs.
        val currentProgramCounter = programCounter
        val accumulatorValue = accumulator
        val xRegisterValue = xRegister
        val yRegisterValue = yRegister
        val statusRegisterValue = statusRegister
        val stackPointerValue = stackPointer

        when (addressingMode) {
            AddressingMode.IMM -> {
                immediateAddressing()
                operation.execute(immediateOperand!!) // If null, bug is in program rom.
            }
            AddressingMode.ABS -> {
                absoluteAddressing()
                operation.execute(targetAddress!!) // If null, bug is in program rom.
            }
            AddressingMode.ABS_X -> {
                absoluteXIndexedAddressing()
                operation.execute(targetAddress!!) // If null, bug is in program rom.
            }
            AddressingMode.ABS_Y -> {
                absoluteYIndexedAddressing()
                operation.execute(targetAddress!!) // If null, bug is in program rom.
            }
            AddressingMode.ZPG -> {
                zeroPageAddressing()
                operation.execute(targetAddress!!) // If null, bug is in program rom.
            }
            AddressingMode.ZPG_X -> {
                zeroPageXIndexedAddressing()
                operation.execute(targetAddress!!) // If null, bug is in program rom.
            }
            AddressingMode.ZPG_Y -> {
                zeroPageYIndexedAddressing()
                operation.execute(targetAddress!!) // If null, bug is in program rom.
            }
            AddressingMode.IND -> {
                indirectAddressing()
                operation.execute(targetAddress!!) // If null, bug is in program rom.
            }
            AddressingMode.X_IND -> {
                xIndexedIndirectAddressing()
                operation.execute(targetAddress!!) // If null, bug is in program rom.
            }
            AddressingMode.IND_Y -> {
                indirectYIndexedAddressing()
                operation.execute(targetAddress!!) // If null, bug is in program rom.
            }
            AddressingMode.REL -> {
                relativeAddressing()
                operation.execute(targetAddress!!) // If null, bug is in program rom.
            }
            AddressingMode.A -> {
                operation.execute()
            }
            AddressingMode.IMP->
                operation.execute()

            else -> {}
        }

        Logger.addLog(
            currentProgramCounter,
            opcodeValue,
            operandLowByte,
            operandHighByte,
            operation.opcodeName,
            targetAddress,
            immediateOperand,
            accumulatorValue,
            xRegisterValue,
            yRegisterValue,
            statusRegisterValue,
            stackPointerValue
        )
    }

    /**
     * Immediate Addressing Mode
     * Returns the literal 8-bit operand located immediately after the opcode.
     * Total instruction length is: Opcode + Operand = 2 bytes.
     */
    fun immediateAddressing() {
        programCounter++
        operandLowByte = readAddress(programCounter)
        immediateOperand  = operandLowByte
    }

    /**
     * Absolute Addressing Mode
     * Returns a 16-bit memory address that contains operand.
     * This address is provided in the next 2 bytes after the opcode; little-endian.
     * Total instruction length is: Opcode + LSB + MSB = 3 bytes.
     */
    fun absoluteAddressing() {
        programCounter++
        val operandLowByte = readAddress(programCounter)
        this.operandLowByte = operandLowByte

        programCounter++
        val operandHighByte = readAddress((programCounter))
        this.operandHighByte = operandHighByte

        this.targetAddress = (operandHighByte.toUInt() shl 8).toUShort() or operandLowByte.toUShort()
    }

    /**
     * Zero-Page Addressing Mode
     * Returns a 16-bit zero-page memory address that contains operand.
     * Since the high-byte of this address is 00, only the lower-byte is placed after the operand.
     * Total instruction length is: Opcode + LSB = 2 bytes.
     */
    fun zeroPageAddressing() {
        programCounter++

        val operandLowByte = readAddress(programCounter)
        this.operandLowByte = operandLowByte

        this.targetAddress = operandLowByte.toUShort()
    }

    /**
     * Absolute Addressing Mode X Indexed
     * Returns a 16-bit memory address computed from the given 16-bit address + contents of x register.
     * The given address is provided in the next 2 bytes after the opcode; little-endian.
     * Total instruction length is: Opcode + LSB + MSB = 3 bytes.
     */
    fun absoluteXIndexedAddressing() {
        programCounter++
        val operandLowByte = readAddress(programCounter)
        this.operandLowByte = operandLowByte

        programCounter++
        val operandHighByte = readAddress(programCounter)
        this.operandHighByte = operandHighByte

        val targetAddress = ((operandHighByte.toUInt() shl 8) + operandLowByte.toUInt() + xRegister.toUInt()).toUShort()

        if ((targetAddress.toUInt() shr 8).toUByte() != operandHighByte) {
            hasOverflowCycle = true
        }

        this.targetAddress = targetAddress
    }

    /**
     * Absolute Addressing Mode Y Indexed
     * Returns a 16-bit memory address computed from the given 16-bit address + contents of y register.
     * The given address is provided in the next 2 bytes after the opcode; little-endian.
     * Total instruction length is: Opcode + LSB + MSB = 3 bytes.
     */
    fun absoluteYIndexedAddressing() {
        programCounter++
        val operandLowByte = readAddress(programCounter)
        this.operandLowByte = operandLowByte

        programCounter++
        val operandHighByte = readAddress(programCounter)
        this.operandHighByte = operandHighByte

        val targetAddress = ((operandHighByte.toUInt() shl 8) + operandLowByte.toUInt() + yRegister.toUInt()).toUShort()

        if ((targetAddress.toUInt() shr 8).toUByte() != operandHighByte) {
            hasOverflowCycle = true
        }

        this.targetAddress = targetAddress
    }

    /**
     * Zero-Page Addressing Mode X Indexed
     * Returns a 16-bit zero-page memory address that contains operand.
     * Is similar to zero-page addressing except the value of the X register
     * is added to point two a new address.
     * Total instruction length is: Opcode + LSB = 2 bytes.
     */
    fun zeroPageXIndexedAddressing() {
        programCounter++
        val operandLowByte = readAddress(programCounter)
        this.operandLowByte = operandLowByte

        this.targetAddress =  (operandLowByte + xRegister).toUByte().toUShort()
    }

    /**
     * Zero-Page Addressing Mode Y Indexed
     * Returns a 16-bit zero-page memory address that contains operand.
     * Is similar to zero-page addressing except the value of the Y register
     * is added to point two a new address.
     * Total instruction length is: Opcode + LSB = 2 bytes.
     */
    fun zeroPageYIndexedAddressing() {
        programCounter++
        val operandLowByte = readAddress(programCounter)
        this.operandLowByte = operandLowByte

        this.targetAddress =  (operandLowByte + yRegister).toUByte().toUShort()
    }

    fun indirectAddressing() {
        programCounter++
        val operandLowByte = readAddress(programCounter)
        this.operandLowByte = operandLowByte

        programCounter++
        val operandHighByte = readAddress(programCounter)
        this.operandHighByte = operandHighByte

        val indirectAddress: UShort = ((operandHighByte.toUInt() shl 8) + operandLowByte.toUInt()).toUShort()

        val targetLeastSignificantByte: UByte = readAddress(indirectAddress)

        // this wonkyness is here because of the original JMP bug in the 6502.
        val targetMostSignificantByte =
            if (indirectAddress.toUByte() == (0xFFu).toUByte()) {
                readAddress((indirectAddress - 0xFFu).toUShort()).toUInt()
            } else {
                readAddress((indirectAddress + 1u).toUShort()).toUInt()
            }

        this.targetAddress = ((targetMostSignificantByte shl 8) + targetLeastSignificantByte).toUShort()
    }

    fun xIndexedIndirectAddressing() {
        programCounter++
        val operandLowByte = readAddress(programCounter)
        this.operandLowByte = operandLowByte

        val zeroPageAddress: UShort = (operandLowByte + xRegister).toUByte().toUShort()

        val targetLeastSignificantByte: UByte = readAddress(zeroPageAddress)
        val targetMostSignificantByte: UByte = readAddress((zeroPageAddress + 1u).toUByte().toUShort())

        this.targetAddress = ((targetMostSignificantByte.toUInt() shl 8) + targetLeastSignificantByte).toUShort()
    }

    fun indirectYIndexedAddressing() {
        programCounter++
        val operandLowByte = readAddress(programCounter)
        this.operandLowByte = operandLowByte

        val targetLeastSignificantByte = readAddress(operandLowByte.toUShort())
        val targetMostSignificantByte: UByte =  readAddress((operandLowByte + 1u).toUByte().toUShort())

        val targetAddress = ((targetMostSignificantByte.toUInt() shl 8) + targetLeastSignificantByte + yRegister).toUShort()

        if ((targetAddress.toUInt() shr 8).toUByte() != targetMostSignificantByte) {
            hasOverflowCycle = true
        }

        this.targetAddress = targetAddress
    }

    fun relativeAddressing() {
        programCounter++
        val offset: Byte = readAddress(programCounter).toByte()
        val nextInstructionAddress = (programCounter.toInt() + 1).toUShort()
        val targetAddress = (nextInstructionAddress.toInt() + offset.toInt()).toUShort()

        if ((targetAddress.toUInt() shr 8).toUByte() != (nextInstructionAddress.toUInt() shr 8).toUByte()) {
            hasOverflowCycle = true
        }

        this.targetAddress = (programCounter.toInt() + (offset.toInt()) + 1).toUShort()
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
    inner class ADC(): Operation {
        override val opcodeName = "ADC"

        override fun execute(operand: UByte) {
            val signBitMask: UByte = 0x80u
            val accumulatorSignedBit = this@CPU6502.accumulator and signBitMask == signBitMask
            val operandSignedBit = operand and signBitMask == signBitMask

            val rawResult = this@CPU6502.accumulator + operand + (if (this@CPU6502.carryFlag) 1u else 0u)
            val result = rawResult.toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (rawResult shr 8) == 1u
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            if (accumulatorSignedBit != operandSignedBit) {
                this@CPU6502.overflowFlag = false
                this@CPU6502.programCounter++
                return
            }

            if (accumulatorSignedBit == this@CPU6502.negativeFlag) {
                this@CPU6502.overflowFlag = false
                this@CPU6502.programCounter++
                return
            }

            this@CPU6502.overflowFlag = true
            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val signBitMask: UByte = 0x80u
            val operand: UByte = this@CPU6502.readAddress(targetAddress)

            val accumulatorSignedBit = this@CPU6502.accumulator and signBitMask == signBitMask
            val operandSignedBit = operand and signBitMask == signBitMask

            val rawResult = this@CPU6502.accumulator + operand + (if (this@CPU6502.carryFlag) 1u else 0u)
            val result = rawResult.toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (rawResult shr 8) == 1u
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            if (hasOverflowCycle) this@CPU6502.cycleCount++

            if (accumulatorSignedBit != operandSignedBit) {
                this@CPU6502.overflowFlag = false
                this@CPU6502.programCounter++
                return
            }

            if (accumulatorSignedBit == this@CPU6502.negativeFlag) {
                this@CPU6502.overflowFlag = false
                this@CPU6502.programCounter++
                return
            }

            this@CPU6502.overflowFlag = true

            this@CPU6502.programCounter++
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
    inner class AND(): Operation {
        override val opcodeName = "AND"

        override fun execute(operand: UByte) {
            val result: UByte = this@CPU6502.accumulator and operand
            this@CPU6502.accumulator = result

            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val result: UByte = this@CPU6502.accumulator and operand
            this@CPU6502.accumulator = result

            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u

            if (hasOverflowCycle) this@CPU6502.cycleCount++
            this@CPU6502.programCounter++
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
    inner class ASL(): Operation {
        override val opcodeName = "ASL"

        override fun execute() {
            val data: UInt = this@CPU6502.accumulator.toUInt()
            val result: UByte = (data shl 1).toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (data shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val data: UInt = this@CPU6502.readAddress(targetAddress).toUInt()
            val result: UByte = (data shl 1).toUByte()
            this@CPU6502.writeToAddress(targetAddress, result)

            this@CPU6502.carryFlag = (data shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }
    }

    /**
     * Branch on Clear Carry
     * Sets program counter to target address when carryFlag = 0
     */
    inner class BCC(): Operation {
        override val opcodeName = "BCC"

        override fun execute(targetAddress: UShort) {
            if (!this@CPU6502.carryFlag) {
                this@CPU6502.programCounter = targetAddress
                this@CPU6502.cycleCount++
                if (hasOverflowCycle) this@CPU6502.cycleCount++
            }
            else this@CPU6502.programCounter++
        }
    }

    /**
     * Branch on Set Carry
     * Sets program counter to target address when carryFlag = 1
     */
    inner class BCS(): Operation {
        override val opcodeName = "BCS"

        override fun execute(targetAddress: UShort) {
            if (this@CPU6502.carryFlag) {
                this@CPU6502.programCounter = targetAddress
                this@CPU6502.cycleCount++
                if (hasOverflowCycle) this@CPU6502.cycleCount++
            }
            else this@CPU6502.programCounter++
        }
    }

    /**
     * Branch on Result Zero
     * Sets program counter to target address when zeroFlag = 1
     */
    inner class BEQ(): Operation {
        override val opcodeName = "BEQ"

        override fun execute(targetAddress: UShort) {
            if (this@CPU6502.zeroFlag) {
                this@CPU6502.programCounter = targetAddress
                this@CPU6502.cycleCount++
                if (hasOverflowCycle) this@CPU6502.cycleCount++
            }
            else this@CPU6502.programCounter++
        }
    }

    /**
     * Test Bits in Memory with Accumulator
     * Bit 7 of operand toggles the negative flag;
     * Bit 6 of perand toggles the overflow flag
     * the zero-flag is set to the result of operand AND accumulator.
     */
    inner class BIT(): Operation {
        override val opcodeName = "BIT"

        override fun execute(targetAddress: UShort) {
            val operand: UInt = this@CPU6502.readAddress(targetAddress).toUInt()
            val result: UByte = this@CPU6502.accumulator and operand.toUByte()

            this@CPU6502.negativeFlag = (operand shr 7) == 1u
            this@CPU6502.overflowFlag = (operand and 0x40u) != 0u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            this@CPU6502.programCounter++
        }
    }

    /**
     * Branch on Result Minus
     * Sets program counter to target address when negativeFlag = 1
     */
    inner class BMI(): Operation {
        override val opcodeName = "BMI"

        override fun execute(targetAddress: UShort) {
            if (this@CPU6502.negativeFlag) {
                this@CPU6502.programCounter = targetAddress
                this@CPU6502.cycleCount++
                if (hasOverflowCycle) this@CPU6502.cycleCount++
            }
            else this@CPU6502.programCounter++
        }
    }

    /**
     * Branch on Result Not Zero
     * Sets program counter to target address when zeroFlag = 0
     */
    inner class BNE(): Operation {
        override val opcodeName = "BNE"

        override fun execute(targetAddress: UShort) {
            if (!this@CPU6502.zeroFlag) {
                this@CPU6502.programCounter = targetAddress
                this@CPU6502.cycleCount++
                if (hasOverflowCycle) this@CPU6502.cycleCount++
            }
            else this@CPU6502.programCounter++
        }
    }

    /**
     * Branch on Result Plus
     * Sets program counter to target address when negativeFlag = 0
     */
    inner class BPL(): Operation {
        override val opcodeName = "BPL"

        override fun execute(targetAddress: UShort) {
            if (!this@CPU6502.negativeFlag) {
                this@CPU6502.programCounter = targetAddress
                this@CPU6502.cycleCount++
                if (hasOverflowCycle) this@CPU6502.cycleCount++
            }
            else this@CPU6502.programCounter++
        }
    }

    /**
     * Break Command
     * performs a programed interrupt similar to IRQ
     */
    inner class BRK(): Operation {
        override val opcodeName = "BRK"

        override fun execute() {
            val vectorLeastSignificantByte = this@CPU6502.readAddress(0xFFFEu)
            val vectorMostSignificantByte = this@CPU6502.readAddress(0xFFFFu)

            this@CPU6502.programCounter++
            this@CPU6502.programCounter++

            this@CPU6502.writeToAddress(this@CPU6502.stackPointer, (this@CPU6502.programCounter.toInt() shr 8).toUByte())
            this@CPU6502.stackPointer--

            this@CPU6502.writeToAddress(this@CPU6502.stackPointer, this@CPU6502.programCounter.toUByte())
            this@CPU6502.stackPointer--

            val statusRegisterValue = this@CPU6502.statusRegister or EXTRA_BITMASK or BREAK_BITMASK

            this@CPU6502.writeToAddress(this@CPU6502.stackPointer, statusRegisterValue)
            this@CPU6502.stackPointer--

            this@CPU6502.interruptDisableFlag = true

            this@CPU6502.programCounter = (((vectorMostSignificantByte.toUInt() shl 8) + vectorLeastSignificantByte).toUShort())
        }
    }

    /**
     * Branch on Overflow Clear
     * Sets program counter to target address when overFlow = 0
     */
    inner class BVC(): Operation {
        override val opcodeName = "BVC"

        override fun execute(targetAddress: UShort) {
            if (!this@CPU6502.overflowFlag) {
                this@CPU6502.programCounter = targetAddress
                this@CPU6502.cycleCount++
                if (hasOverflowCycle) this@CPU6502.cycleCount++
            }
            else this@CPU6502.programCounter++
        }
    }

    /**
     * Branch on Overflow Set
     * Sets program counter to target address when overFlow = 1
     */
    inner class BVS(): Operation {
        override val opcodeName = "BVS"

        override fun execute(targetAddress: UShort) {
            if (this@CPU6502.overflowFlag) {
                this@CPU6502.programCounter = targetAddress
                this@CPU6502.cycleCount++
                if (hasOverflowCycle) this@CPU6502.cycleCount++
            }
            else this@CPU6502.programCounter++
        }
    }

    /**
     * Clear Carry Flag
     * This instruction initializes the carry flag to a 0.
     * This instruction affects no registers in the microprocessor and no flags other than the carry flag which is reset.
     */
    inner class CLC(): Operation {
        override val opcodeName = "CLC"

        override fun execute() {
            this@CPU6502.carryFlag = false
            this@CPU6502.programCounter++
        }
    }

    /**
     * Clear Decimal Mode
     * This instruction sets the decimal mode flag to a 0.
     * CLD affects no registers in the microprocessor and no flags other than the decimal mode flag which is set to a 0.
     */
    inner class CLD(): Operation {
        override val opcodeName = "CLD"

        override fun execute() {
            this@CPU6502.decimalFlag = false
            this@CPU6502.programCounter++
        }
    }

    /**
     * Clear Interrupt Disable
     * This instruction initializes the interrupt disable to a 0. This allows the microprocessor to receive interrupts.
     * It affects no registers in the microprocessor and no flags other than the interrupt disable which is cleared.
     */
    inner class CLI(): Operation {
        override val opcodeName = "CLI"

        override fun execute() {
            this@CPU6502.interruptDisableFlag = false
            this@CPU6502.programCounter++
        }
    }


    /**
     * Clear Overflow Flag
     * This instruction clears the overflow flag to a 0.
     * CLV affects no registers in the microprocessor and no flags other than the overflow flag which is set to a 0.
     */
    inner class CLV(): Operation {
        override val opcodeName = "CLV"

        override fun execute() {
            this@CPU6502.overflowFlag = false
            this@CPU6502.programCounter++
        }
    }

    /**
     * Compare Memory and Accumulator
     * subtracts the value in memory from the accumulator without storing the result.
     * zero flag is set when operands are equal.
     * negative flag set if result is negative.
     * carry flag set if register >= operand
     */
    inner class CMP(): Operation {
        override val opcodeName = "CMP"

        override fun execute(operand: UByte) {
            val signBitMask: UByte = 0x80u
            val rawResult = (this@CPU6502.accumulator.toByte() - operand.toByte()).toUInt()
            val result = rawResult.toUByte()

            this@CPU6502.carryFlag = this@CPU6502.accumulator >= operand
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val signBitMask: UByte = 0x80u
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val rawResult = (this@CPU6502.accumulator.toByte() - operand.toByte()).toUInt()
            val result = rawResult.toUByte()

            this@CPU6502.carryFlag = this@CPU6502.accumulator >= operand
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            if (hasOverflowCycle) this@CPU6502.cycleCount++
            this@CPU6502.programCounter++
        }
    }

    /**
     * Compare Memory and X Register
     * subtracts the value in memory from the register without storing the result.
     * zero flag is set when operands are equal.
     * negative flag set if result is negative.
     * carry flag set if register >= operand
     */
    inner class CPX(): Operation {
        override val opcodeName = "CPX"

        override fun execute(operand: UByte) {
            val signBitMask: UByte = 0x80u
            val rawResult = (this@CPU6502.xRegister.toByte() - operand.toByte()).toUInt()
            val result = rawResult.toUByte()

            this@CPU6502.carryFlag = this@CPU6502.xRegister >= operand
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val signBitMask: UByte = 0x80u
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val rawResult = (this@CPU6502.xRegister.toByte() - operand.toByte()).toUInt()
            val result = rawResult.toUByte()

            this@CPU6502.carryFlag = this@CPU6502.xRegister >= operand
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            this@CPU6502.programCounter++
        }
    }

    /**
     * Compare Memory and Y Register
     * subtracts the value in memory from the register without storing the result.
     * zero flag is set when operands are equal.
     * negative flag set if result is negative.
     * carry flag set if register >= operand
     */
    inner class CPY(): Operation {
        override val opcodeName = "CPY"

        override fun execute(operand: UByte) {
            val signBitMask: UByte = 0x80u
            val rawResult = (this@CPU6502.yRegister.toByte() - operand.toByte()).toUInt()
            val result = rawResult.toUByte()

            this@CPU6502.carryFlag = this@CPU6502.yRegister >= operand
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val signBitMask: UByte = 0x80u
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val rawResult = (this@CPU6502.yRegister.toByte() - operand.toByte()).toUInt()
            val result = rawResult.toUByte()

            this@CPU6502.carryFlag = this@CPU6502.yRegister >= operand
            this@CPU6502.negativeFlag = result and signBitMask == signBitMask
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            this@CPU6502.programCounter++
        }
    }

    inner class DEC(): Operation {
        override val opcodeName = "DEC"

        override fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val result = operand.dec()
            this@CPU6502.writeToAddress(targetAddress, result)

            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }
    }

    inner class DEX(): Operation {
        override val opcodeName = "DEX"

        override fun execute() {
            this@CPU6502.xRegister--

            this@CPU6502.zeroFlag = this@CPU6502.xRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.xRegister.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }
    }

    inner class DEY(): Operation {
        override val opcodeName = "DEY"

        override fun execute() {
            this@CPU6502.yRegister--

            this@CPU6502.zeroFlag = this@CPU6502.yRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.yRegister.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }
    }

    /**
     * Exclusive OR Memory with Accumulator
     * performs a binary "EXCLUSIVE OR" on a bit-by-bit basis and stores the result in the accumulator.
     * Negative flag toggled by bit 7 of result.
     * Zero flag toggled by result.
     */
    inner class EOR(): Operation {
        override val opcodeName = "EOR"

        override fun execute(operand: UByte) {
            val result: UByte = this@CPU6502.accumulator xor operand
            this@CPU6502.accumulator = result

            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val result: UByte = this@CPU6502.accumulator xor operand
            this@CPU6502.accumulator = result

            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            if (hasOverflowCycle) this@CPU6502.cycleCount++
            this@CPU6502.programCounter++
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
    inner class INC(): Operation {
        override val opcodeName = "INC"

        override fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val result = operand.inc()
            this@CPU6502.writeToAddress(targetAddress, result)

            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
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
    inner class INX(): Operation {
        override val opcodeName = "INX"

        override fun execute() {
            this@CPU6502.xRegister++

            this@CPU6502.zeroFlag = this@CPU6502.xRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.xRegister.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
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
    inner class INY(): Operation {
        override val opcodeName = "INY"

        override fun execute() {
            this@CPU6502.yRegister++

            this@CPU6502.zeroFlag = this@CPU6502.yRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.yRegister.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }
    }

    inner class JMP(): Operation {
        override val opcodeName = "JMP"

        override fun execute(targetAddress: UShort) {
            this@CPU6502.programCounter = targetAddress
        }
    }


    /**
     * Jump to Subroutine
     * jumps program counter to target address, but first saves last address of current instruction to stack.
     * Decrements the stack twice in the process.
     */
    inner class JSR(): Operation {
        override val opcodeName = "JSR"

        override fun execute(targetAddress: UShort) {
            val currentAddressMostSignificantByte: UByte = (this@CPU6502.programCounter.toUInt() shr 8).toUByte()
            val currentAddressLeastSignificantByte: UByte = this@CPU6502.programCounter.toUByte()

            this@CPU6502.writeToAddress(this@CPU6502.stackPointer, currentAddressMostSignificantByte)
            this@CPU6502.stackPointer--
            this@CPU6502.writeToAddress(this@CPU6502.stackPointer, currentAddressLeastSignificantByte)
            this@CPU6502.stackPointer--

            this@CPU6502.programCounter = targetAddress
        }
    }

    /**
     * Load Accumulator
     * Load the accumulator from memory
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class LDA(): Operation {
        override val opcodeName = "LDA"

        override fun execute(operand: UByte) {
            this@CPU6502.accumulator = operand

            this@CPU6502.zeroFlag = operand == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (operand.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val data: UByte = this@CPU6502.readAddress(targetAddress)
            this@CPU6502.accumulator = data

            this@CPU6502.zeroFlag = data == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (data.toUInt() shr 7) == 1u

            if (hasOverflowCycle) this@CPU6502.cycleCount++
            this@CPU6502.programCounter++
        }
    }

    /**
     * Load X Register
     * Load the index register X from memory.
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class LDX(): Operation {
        override val opcodeName = "LDX"

        override fun execute(operand: UByte) {
            this@CPU6502.xRegister = operand

            this@CPU6502.zeroFlag = operand == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (operand.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val data: UByte = this@CPU6502.readAddress(targetAddress)
            this@CPU6502.xRegister = data

            this@CPU6502.zeroFlag = data == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (data.toUInt() shr 7) == 1u

            if (hasOverflowCycle) this@CPU6502.cycleCount++
            this@CPU6502.programCounter++
        }
    }

    /**
     * Load Y Register
     * Load the index register Y from memory.
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class LDY(): Operation {
        override val opcodeName = "LDY"

        override fun execute(operand: UByte) {
            this@CPU6502.yRegister = operand

            this@CPU6502.zeroFlag = operand == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (operand.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val data: UByte = this@CPU6502.readAddress(targetAddress)
            this@CPU6502.yRegister = data

            this@CPU6502.zeroFlag = data == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (data.toUInt() shr 7) == 1u

            if (hasOverflowCycle) this@CPU6502.cycleCount++
            this@CPU6502.programCounter++
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
    inner class LSR(): Operation {
        override val opcodeName = "LSR"

        override fun execute() {
            val data: UInt = this@CPU6502.accumulator.toUInt()
            val result: UByte = (data shr 1).toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = false

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort){
            val data: UInt = this@CPU6502.readAddress(targetAddress).toUInt()
            val result: UByte = (data shr 1).toUByte()
            this@CPU6502.writeToAddress(targetAddress, result)

            this@CPU6502.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = false

            this@CPU6502.programCounter++
        }
    }

    /**
     * No Operation
     */
    inner class NOP(): Operation {
        override val opcodeName = "NOP"

        override fun execute() {
            this@CPU6502.programCounter++
        }
    }

    /**
     * OR Memory with Accumulator
     * performs a binary OR on a bit-by-bit basis and stores the result in the accumulator.
     * Negative flag toggled by bit 7 of result.
     * Zero flag toggled by result.
     */
    inner class ORA(): Operation {
        override val opcodeName = "ORA"

        override fun execute(operand: UByte) {
            val result: UByte = this@CPU6502.accumulator or operand
            this@CPU6502.accumulator = result

            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val operand: UByte = this@CPU6502.readAddress(targetAddress)
            val result: UByte = this@CPU6502.accumulator or operand
            this@CPU6502.accumulator = result

            this@CPU6502.negativeFlag = (result.toUInt() shr 7) == 1u
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            if (hasOverflowCycle) this@CPU6502.cycleCount++
            this@CPU6502.programCounter++
        }
    }

    /**
     * Push Accumulator On Stack
     * copies the current value of the accumulator into the memory location the stack register points to.
     * decrements the stack pointer value
     * does not affect any flags or registers.
     */
    inner class PHA(): Operation {
        override val opcodeName = "PHA"
        val baseCycleCost = 3

        override fun execute() {
            this@CPU6502.writeToAddress(this@CPU6502.stackPointer, this@CPU6502.accumulator)
            this@CPU6502.stackPointer--

            this@CPU6502.programCounter++
            cycleCount = baseCycleCost
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
    inner class PHP(): Operation {
        override val opcodeName = "PHP"

        override fun execute() {
            val statusRegisterValue = this@CPU6502.statusRegister or EXTRA_BITMASK or BREAK_BITMASK

            this@CPU6502.writeToAddress(this@CPU6502.stackPointer, statusRegisterValue)
            this@CPU6502.stackPointer--

            this@CPU6502.programCounter++
        }
    }

    /**
     * Pull Accumulator from Stack
     * increments the stack pointer, and copies the value in that location to accumulator.
     * -bit 7 of the result toggles negative flag.
     * -toggles zero flag if result is 0.
     */
    inner class PLA(): Operation {
        override val opcodeName = "PLA"

        override fun execute() {
            this@CPU6502.stackPointer++
            val data: UByte = this@CPU6502.readAddress(this@CPU6502.stackPointer)
            this@CPU6502.accumulator = data

            this@CPU6502.negativeFlag = (data.toUInt() shr 7) == 1u
            this@CPU6502.zeroFlag = data == (0x00u).toUByte()

            this@CPU6502.programCounter++
        }
    }

    /**
     * Pull Processor Status from Stack
     * increments the stack pointer, and copies the value in that address to the status register.
     */
    inner class PLP(): Operation {
        override val opcodeName = "PLP"

        override fun execute() {
            this@CPU6502.stackPointer++

            val extraFlagValue = this@CPU6502.extraFlag
            val breakFlagValue = this@CPU6502.breakFlag

            val data: UByte = this@CPU6502.readAddress(this@CPU6502.stackPointer)
            this@CPU6502.statusRegister = data

            this@CPU6502.extraFlag = extraFlagValue
            this@CPU6502.breakFlag = breakFlagValue

            this@CPU6502.programCounter++
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
    inner class ROL(): Operation {
        override val opcodeName = "ROL"

        override fun execute() {
            val data: UInt = this@CPU6502.accumulator.toUInt()
            val result: UByte = if (this@CPU6502.carryFlag) ((data shl 1) or (1u)).toUByte() else (data shl 1).toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (data shr 7).toUByte() == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val data: UInt = this@CPU6502.readAddress(targetAddress).toUInt()
            val result: UByte = if (this@CPU6502.carryFlag) ((data shl 1) or (1u)).toUByte() else (data shl 1).toUByte()
            this@CPU6502.writeToAddress(targetAddress, result)

            this@CPU6502.carryFlag = (data shr 7).toUByte() == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()

            this@CPU6502.programCounter++
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
    inner class ROR(): Operation {
        override val opcodeName = "ROR"

        override fun execute() {
            val data: UInt = this@CPU6502.accumulator.toUInt()
            val result: UByte = if (this@CPU6502.carryFlag) ((data shr 1) or (0x80u)).toUByte() else (data shr 1).toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val data: UInt = this@CPU6502.readAddress(targetAddress).toUInt()
            val result: UByte = if (this@CPU6502.carryFlag) ((data shr 1) or (0x80u)).toUByte() else (data shr 1).toUByte()
            this@CPU6502.writeToAddress(targetAddress, result)

            this@CPU6502.carryFlag = (data.toUByte() and (0x01).toUByte()) == (1u).toUByte()
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (result.toUInt() shr 7).toUByte() == (1u).toUByte()

            this@CPU6502.programCounter++
        }
    }

    /**
     * Return From Interrupt
     * restores program counter and status register from stack.
     */
    inner class RTI(): Operation {
        override val opcodeName = "RTI"

        override fun execute() {
            val extraFlagValue = this@CPU6502.extraFlag
            val breakFlagValue = this@CPU6502.breakFlag

            this@CPU6502.stackPointer++
            val statusRegisterValue = this@CPU6502.readAddress(this@CPU6502.stackPointer)
            this@CPU6502.statusRegister = statusRegisterValue
            this@CPU6502.extraFlag = extraFlagValue
            this@CPU6502.breakFlag = breakFlagValue

            this@CPU6502.stackPointer++
            val targetLeastSignificantByte = this@CPU6502.readAddress(this@CPU6502.stackPointer)

            this@CPU6502.stackPointer++
            val targetMostSignificantByte = this@CPU6502.readAddress(this@CPU6502.stackPointer)

            this@CPU6502.programCounter = ((targetMostSignificantByte.toUInt() shl 8) + targetLeastSignificantByte).toUShort()
        }
    }

    /**
     * Return From Subroutine
     * restores program counter from stack.
     */
    inner class RTS(): Operation {
        override val opcodeName = "RTS"

        override fun execute() {
            this@CPU6502.stackPointer++
            val targetLeastSignificantByte = this@CPU6502.readAddress(this@CPU6502.stackPointer)
            this@CPU6502.stackPointer++
            val targetMostSignificantByte = this@CPU6502.readAddress(this@CPU6502.stackPointer)

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
    inner class SBC(): Operation {
        override val opcodeName = "SBC"

        override fun execute(operand: UByte) {
            val accumulatorSignedBit = this@CPU6502.accumulator and NEGATIVE_BITMASK == NEGATIVE_BITMASK
            val operandSignedBit = operand and NEGATIVE_BITMASK == NEGATIVE_BITMASK

            val rawResult = this@CPU6502.accumulator - operand - (if (this@CPU6502.carryFlag) 0u else 1u)
            val result = rawResult.toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = result.toByte() >= 0
            this@CPU6502.negativeFlag = result and NEGATIVE_BITMASK == NEGATIVE_BITMASK
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            if (accumulatorSignedBit == operandSignedBit) {
                this@CPU6502.overflowFlag = false
                this@CPU6502.programCounter++
                return
            }

            if (accumulatorSignedBit != this@CPU6502.negativeFlag) {
                this@CPU6502.overflowFlag = true
                this@CPU6502.programCounter++
                return
            }

            this@CPU6502.overflowFlag = false

            this@CPU6502.programCounter++
        }

        override fun execute(targetAddress: UShort) {
            val operand = this@CPU6502.readAddress(targetAddress)
            val accumulatorSignedBit = this@CPU6502.accumulator and NEGATIVE_BITMASK == NEGATIVE_BITMASK
            val operandSignedBit = operand and NEGATIVE_BITMASK == NEGATIVE_BITMASK

            val rawResult = this@CPU6502.accumulator - operand - (if (this@CPU6502.carryFlag) 0u else 1u)
            val result = rawResult.toUByte()
            this@CPU6502.accumulator = result

            this@CPU6502.carryFlag = result.toByte() >= 0
            this@CPU6502.negativeFlag = result and NEGATIVE_BITMASK == NEGATIVE_BITMASK
            this@CPU6502.zeroFlag = result == (0x00u).toUByte()

            if (hasOverflowCycle) this@CPU6502.cycleCount++

            if (accumulatorSignedBit == operandSignedBit) {
                this@CPU6502.overflowFlag = false
                this@CPU6502.programCounter++
                return
            }

            if (accumulatorSignedBit != this@CPU6502.negativeFlag) {
                this@CPU6502.overflowFlag = true
                this@CPU6502.programCounter++
                return
            }

            this@CPU6502.overflowFlag = false

            this@CPU6502.programCounter++
        }
    }

    /**
     * Set Carry Flag
     * This instruction initializes the carry flag to a 1.
     * This instruction affects no registers in the microprocessor and no flags other than the carry flag which is set.
     */
    inner class SEC(): Operation {
        override val opcodeName = "SEC"

        override fun execute() {
            this@CPU6502.carryFlag = true
            this@CPU6502.programCounter++
        }
    }

    /**
     * Set Decimal Flag
     * This instruction sets the decimal mode flag D to a 1.
     * SED affects no registers in the microprocessor and no flags other than the decimal mode which is set to a 1.
     */
    inner class SED(): Operation {
        override val opcodeName = "SED"

        override fun execute() {
            this@CPU6502.decimalFlag = true
            this@CPU6502.programCounter++
        }
    }

    /**
     * Set Interrupt Disable
     * This instruction initializes the interrupt disable to a 1.
     * It is used to mask interrupt requests during system reset operations and during interrupt commands.
     * It affects no registers in the microprocessor and no flags other than the interrupt disable which is set.
     */
    inner class SEI(): Operation {
        override val opcodeName = "SEI"

        override fun execute() {
            this@CPU6502.interruptDisableFlag = true
            this@CPU6502.programCounter++
        }
    }

    /**
     * Store Accumulator In Memory
     * This instruction transfers the contents of the accumulator to memory.
     * This instruction affects none of the flags in the processor status register and does not affect the accumulator.
     */
    inner class STA(): Operation {
        override val opcodeName = "STA"

        override fun execute(targetAddress: UShort) {
            this@CPU6502.writeToAddress(targetAddress, this@CPU6502.accumulator)
            this@CPU6502.programCounter++
        }
    }

    /**
     * Store Register X in Memory
     * Transfers value of X register to addressed memory location.
     * No flags or registers in the microprocessor are affected by the store operation.
     */
    inner class STX(): Operation {
        override val opcodeName = "STX"

        override fun execute(targetAddress: UShort) {
            this@CPU6502.writeToAddress(targetAddress, this@CPU6502.xRegister)
            this@CPU6502.programCounter++
        }
    }

    /**
     * Store Register Y in Memory
     * Transfer the value of the Y register to the addressed memory location.
     * STY does not affect any flags or registers in the microprocessor.
     */
    inner class STY(): Operation {
        override val opcodeName = "STY"

        override fun execute(targetAddress: UShort) {
            this@CPU6502.writeToAddress(targetAddress, this@CPU6502.yRegister)
            this@CPU6502.programCounter++
        }
    }

    /**
     * Transfer Accumulator to X register.
     * This instruction takes the value from the accumulator and loads it to register X
     * without disturbing the content of the accumulator A.
     *
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class TAX(): Operation {
        override val opcodeName = "TAX"

        override fun execute() {
            this@CPU6502.xRegister = this@CPU6502.accumulator
            this@CPU6502.zeroFlag = this@CPU6502.xRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.xRegister.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }
    }

    /**
     * Transfer Accumulator to Y register.
     * This instruction takes the value from the accumulator and loads it to register Y
     * without disturbing the content of the accumulator A.
     *
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class TAY(): Operation {
        override val opcodeName = "TAY"

        override fun execute() {
            this@CPU6502.yRegister = this@CPU6502.accumulator
            this@CPU6502.zeroFlag = this@CPU6502.yRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.yRegister.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }
    }

    /**
     * Transfer Stack Pointer to X register.
     * This instruction takes the value from the stack pointer and loads it to register X
     * without disturbing the content of the stack pointer.
     *
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class TSX(): Operation {
        override val opcodeName = "TSX"

        override fun execute() {
            this@CPU6502.xRegister = this@CPU6502.stackPointer.toUByte()
            this@CPU6502.zeroFlag = this@CPU6502.xRegister == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.xRegister.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }
    }


    /**
     * Transfer X register to Accumulator.
     * This instruction takes the value from the x register and loads it into the accumulator
     * without disturbing the content of the x register.
     *
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class TXA(): Operation {
        override val opcodeName = "TXA"

        override fun execute() {
            this@CPU6502.accumulator = this@CPU6502.xRegister
            this@CPU6502.zeroFlag = this@CPU6502.accumulator == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.accumulator.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }
    }

    /**
     * Transfer X register to Stack Pointer
     * This instruction transfers the value in the index register X to the stack pointer.
     * TXS changes only the stack pointer, making it equal to the content of the index
     * register X. It does not affect any of the flags.
     */
    inner class TXS(): Operation {
        override val opcodeName = "TXS"

        override fun execute() {
            this@CPU6502.stackPointer = this@CPU6502.xRegister.toUShort()
            this@CPU6502.programCounter++
        }
    }

    /**
     * Transfer Y register to Accumulator.
     * This instruction takes the value from the y register and loads it into the accumulator
     * without disturbing the content of the y register.
     *
     * Toggles the zero flag if the value is 0, toggles negative flag if bit 7 is a 1.
     */
    inner class TYA(): Operation {
        override val opcodeName = "TYA"

        override fun execute() {
            this@CPU6502.accumulator = this@CPU6502.yRegister
            this@CPU6502.zeroFlag = this@CPU6502.accumulator == (0x00u).toUByte()
            this@CPU6502.negativeFlag = (this@CPU6502.accumulator.toUInt() shr 7) == 1u

            this@CPU6502.programCounter++
        }
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

        writeToAddress(stackPointer, (programCounter.toInt() shr 8).toUByte())
        stackPointer--

        writeToAddress(stackPointer, programCounter.toUByte())
        stackPointer--

        val statusRegisterValue = (this@CPU6502.statusRegister or EXTRA_BITMASK) and BREAK_BITMASK.inv()

        writeToAddress(stackPointer, statusRegisterValue)
        stackPointer--

        this@CPU6502.interruptDisableFlag = true

        programCounter = (((vectorMostSignificantByte.toUInt() shl 8) + vectorLeastSignificantByte).toUShort())

        cycleCount += 7
    }

    /**
     * RESET
     * load the vector at 0xFFFC and 0xFFFD into PC
     */
    fun reset() {
        stackPointer--
        stackPointer--
        stackPointer--
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

        writeToAddress(stackPointer, (programCounter.toInt() shr 8).toUByte())
        stackPointer--

        writeToAddress(stackPointer, programCounter.toUByte())
        stackPointer--

        val statusRegisterValue = (this@CPU6502.statusRegister or EXTRA_BITMASK) and BREAK_BITMASK.inv()

        writeToAddress(stackPointer, statusRegisterValue)
        stackPointer--

        interruptDisableFlag = true

        programCounter = (((vectorMostSignificantByte.toUInt() shl 8) + vectorLeastSignificantByte).toUShort())
    }

    inner class Disassembler() {
       var allInstructions: MutableMap<UShort, String> = mutableMapOf()

        fun disassembleAssemblyCode(startAddress: UInt, endAddress: UInt) {
            var currentAddress = startAddress
            while (currentAddress <= endAddress) {

                val instruction = fetchInstruction(readAddress(currentAddress.toUShort()))

                val instructionName = instruction.second.opcodeName

                this@CPU6502.programCounter = currentAddress.toUShort()
                this@CPU6502.xRegister = 0u
                this@CPU6502.yRegister = 0u

                val mnemonic = when (instruction.first) {
                    AddressingMode.IMP -> instructionName
                    AddressingMode.A -> "$instructionName A"
                    AddressingMode.IMM -> {
                        immediateAddressing()
                        "$instructionName #$${this@CPU6502.immediateOperand?.to2DigitHexString()}"
                    }
                    AddressingMode.ABS -> {
                        absoluteAddressing()
                        "$instructionName $${this@CPU6502.targetAddress?.to4DigitHexString()}"
                    }
                    AddressingMode.ZPG -> {
                        zeroPageAddressing()
                        "$instructionName $${this@CPU6502.targetAddress?.to2DigitHexString()}"
                    }
                    AddressingMode.ABS_X -> {
                        absoluteXIndexedAddressing()
                        "$instructionName $${targetAddress?.to4DigitHexString()}, X"
                    }
                    AddressingMode.ABS_Y -> {
                        absoluteYIndexedAddressing()
                        "$instructionName $${targetAddress?.to4DigitHexString()}, Y"
                    }
                    AddressingMode.ZPG_X -> {
                        zeroPageXIndexedAddressing()
                        "$instructionName $${targetAddress?.to2DigitHexString()}, X"
                    }
                    AddressingMode.ZPG_Y -> {
                        zeroPageYIndexedAddressing()
                        "$instructionName $${targetAddress?.to2DigitHexString()}, Y"
                    }
                    AddressingMode.IND -> {
                        indirectAddressing()
                        "$instructionName (${((operandHighByte!!.toUInt() shl 8) or operandLowByte!!.toUInt()).to4DigitHexString()})"
                    }
                    AddressingMode.X_IND -> {
                        xIndexedIndirectAddressing()
                        "$instructionName ($${operandLowByte?.to2DigitHexString()}, X)"
                    }
                    AddressingMode.IND_Y -> {
                        indirectYIndexedAddressing()
                        "$instructionName ($${operandLowByte?.to2DigitHexString()}), Y"
                    }
                    AddressingMode.REL -> {
                        relativeAddressing()
                        "$instructionName $${targetAddress?.to4DigitHexString()}"
                    }
                    AddressingMode.ILL -> {
                        "??? Illegal Opcode"
                    }
                }

                allInstructions[currentAddress.toUShort()] = "$${currentAddress.to4DigitHexString()}: $mnemonic {${instruction.first}}"

                currentAddress += when (instruction.first) {
                    AddressingMode.IMP -> if (instruction.second.opcodeName == "BRK") 2u else 1u
                    AddressingMode.A -> 1u
                    AddressingMode.IMM -> 2u
                    AddressingMode.ABS -> 3u
                    AddressingMode.ZPG -> 2u
                    AddressingMode.ABS_X -> 3u
                    AddressingMode.ABS_Y -> 3u
                    AddressingMode.ZPG_X -> 2u
                    AddressingMode.ZPG_Y -> 2u
                    AddressingMode.IND -> 3u
                    AddressingMode.X_IND -> 2u
                    AddressingMode.IND_Y -> 2u
                    AddressingMode.REL -> 2u
                    else -> 1u
                }

                operandLowByte = null
                operandHighByte = null
                targetAddress = null
                immediateOperand = null
            }
        }
    }

    companion object {
        private const val NEGATIVE_BITMASK: UByte = 0x80u
        private const val OVERFLOW_BITMASK: UByte = 0x40u
        private const val EXTRA_BITMASK: UByte = 0x20u
        private const val BREAK_BITMASK: UByte = 0x10u
        private const val DECIMAL_BITMASK: UByte = 0x08u
        private const val INTERRUPT_DISABLE_BITMASK: UByte = 0x04u
        private const val ZERO_BITMASK: UByte = 0x02u
        private const val CARRY_BITMASK: UByte = 0x01u
    }
}