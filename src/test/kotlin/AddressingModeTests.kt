import CPU.CPU6502
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class AddressingModeTests {
    private lateinit var testCPU: CPU6502
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_535)
        testBus = Bus(testRam)
        testCPU = CPU6502(testBus)
    }

    /**
     * Immediate
     */
    @Test
    fun `test get constant with immediate addressing mode`() {
        val opcodeAddress: UShort = 0x0105u
        val operand: UByte = 0x0Au
        testBus.ram[(opcodeAddress + 1u).toInt()] = operand
        testCPU.programCounter = opcodeAddress

        assertEquals(operand, testCPU.immediateAddressing())
        assertEquals((opcodeAddress + 1u).toUShort(), testCPU.programCounter)
    }

    /**
     * Absolute
     */

    @Test
    fun `test absolute addressing mode`() {
        val opcodeAddress: UShort = 0x0105u
        val mostSignificantByte: UByte = 0x30u
        val leastSignificantByte: UByte = 0x10u
        val result: UShort = 0x3010u

        testCPU.apply {
            programCounter = opcodeAddress
            bus.ram[(opcodeAddress + 1u).toInt()] = leastSignificantByte
            bus.ram[(opcodeAddress + 2u).toInt()] = mostSignificantByte
        }

        testCPU.also {
            assertEquals(result, it.absoluteAddressing())
            assertEquals((opcodeAddress + 2u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test absolute X addressing mode with x offset`() {
        val opcodeAddress: UShort = 0x0105u
        val mostSignificantByte: UByte = 0x31u
        val leastSignificantByte: UByte = 0x20u
        val xRegisterValue: UByte = 0x12u
        val result: UShort = 0x3132u

        testCPU.apply {
            programCounter = opcodeAddress
            xRegister = xRegisterValue
            bus.ram[(opcodeAddress + 1u).toInt()] = leastSignificantByte
            bus.ram[(opcodeAddress + 2u).toInt()] = mostSignificantByte
        }

        testCPU.also {
            assertEquals(result, it.absoluteXIndexedAddressing())
            assertEquals((opcodeAddress + 2u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test absolute X addressing mode with x offset with wrapping`() {
        val opcodeAddress: UShort = 0x0105u
        val mostSignificantByte: UByte = 0xFFu
        val leastSignificantByte: UByte = 0xFEu
        val xRegisterValue: UByte = 0x12u
        val result: UShort = 0x10u

        testCPU.apply {
            programCounter = opcodeAddress
            xRegister = xRegisterValue
            bus.ram[(opcodeAddress + 1u).toInt()] = leastSignificantByte
            bus.ram[(opcodeAddress + 2u).toInt()] = mostSignificantByte
        }

        testCPU.also {
            assertEquals(result, it.absoluteXIndexedAddressing())
            assertEquals((opcodeAddress + 2u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test absolute Y addressing mode with y offset`() {
        val opcodeAddress: UShort = 0x0105u
        val mostSignificantByte: UByte = 0x31u
        val leastSignificantByte: UByte = 0x20u
        val yRegisterValue: UByte = 0x12u
        val result: UShort = 0x3132u

        testCPU.apply {
            programCounter = opcodeAddress
            yRegister = yRegisterValue
            bus.ram[(opcodeAddress + 1u).toInt()] = leastSignificantByte
            bus.ram[(opcodeAddress + 2u).toInt()] = mostSignificantByte
        }

        testCPU.also {
            assertEquals(result, it.absoluteYIndexedAddressing())
            assertEquals((opcodeAddress + 2u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test absolute Y addressing mode with y offset with wrapping`() {
        val opcodeAddress: UShort = 0x0105u
        val mostSignificantByte: UByte = 0xFFu
        val leastSignificantByte: UByte = 0xFEu
        val yRegisterValue: UByte = 0x12u
        val result: UShort = 0x10u

        testCPU.apply {
            programCounter = opcodeAddress
            yRegister = yRegisterValue
            bus.ram[(opcodeAddress + 1u).toInt()] = leastSignificantByte
            bus.ram[(opcodeAddress + 2u).toInt()] = mostSignificantByte
        }

        testCPU.also {
            assertEquals(result, it.absoluteYIndexedAddressing())
            assertEquals((opcodeAddress + 2u).toUShort(), it.programCounter)
        }
    }

    /**
     * Zero-Page Addressing
     */

    @Test
    fun `test get zero page address with Zero Page Addressing`() {
        val opcodeAddress: UShort = 0x0105u
        val zeroPageAddress: UByte = 0x80u
        val result: UShort = 0x0080u

        testCPU.apply {
            programCounter = opcodeAddress
            bus.ram[(opcodeAddress + 1u).toInt()] = zeroPageAddress
        }

        testCPU.also {
            assertEquals(result, it.zeroPageAddressing())
            assertEquals((opcodeAddress + 1u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test get zero page address with X offset`() {
        val opcodeAddress: UShort = 0x0105u
        val zeroPageAddress: UByte = 0x80u
        val xRegisterValue: UByte = 0x02u
        val result: UShort = 0x0082u

        testCPU.apply {
            programCounter = opcodeAddress
            xRegister = xRegisterValue
            bus.ram[(opcodeAddress + 1u).toInt()] = zeroPageAddress
        }

        testCPU.also {
            assertEquals(result, it.zeroPageXIndexedAddressing())
            assertEquals((opcodeAddress + 1u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test get zero page address with Zero X Page Addressing with wrap`() {
        val opcodeAddress: UShort = 0x0105u
        val zeroPageAddress: UByte = 0x80u
        val xRegisterValue: UByte = 0xFFu
        val result: UShort = 0x00BFu

        testCPU.apply {
            programCounter = opcodeAddress
            xRegister = xRegisterValue
            bus.ram[(opcodeAddress + 1u).toInt()] = zeroPageAddress
        }

        testCPU.also {
            assertEquals(result, it.zeroPageXIndexedAddressing())
            assertEquals((opcodeAddress + 1u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test get zero page address with Zero Y Page Addressing`() {
        val instructionAddress: UShort = 0x0F0Fu
        val operandAddress: UShort = 0x0F10u
        val operand: UByte = 0x0Au
        val yRegisterValue: UByte = 0x05u
        val targetAddress: UShort = 0x000Fu

        testBus.ram[operandAddress.toInt()] = operand
        testCPU.programCounter = instructionAddress
        testCPU.yRegister = yRegisterValue

        assertEquals(targetAddress, testCPU.zeroPageYIndexedAddressing())
    }

    @Test
    fun `test get zero page address with Zero Y Page Addressing with wrap`() {
        val instructionAddress: UShort = 0x0F0Fu
        val operandAddress: UShort = 0x0F10u
        val operand: UByte = 0xFAu
        val yRegisterValue: UByte = 0x06u
        val targetAddress: UShort = 0x0000u

        testBus.ram[operandAddress.toInt()] = operand
        testCPU.programCounter = instructionAddress
        testCPU.yRegister = yRegisterValue

        assertEquals(targetAddress, testCPU.zeroPageYIndexedAddressing())
    }

    @Test
    fun `test relative addressing mode offset`() {
        val instructionAddress: UShort = 0x0AAAu
        val operandAddress: UShort = 0x0AABu
        val operand: UByte = 0x10u
        val targetAddress: UShort = 0x0ABBu

        testBus.ram[operandAddress.toInt()] = operand
        testCPU.programCounter = instructionAddress

        assertEquals(targetAddress, testCPU.relativeAddressing())
    }



    @Test
    fun `test indirect addressing mode`() {
        val startAddress: UShort = 0x0031u
        val firstByteAddress: UShort = 0x0032u
        val secondByteAddress: UShort = 0x0033u
        val operandAddressMostSignificantBits: UByte = 0x12u
        val operandAddressLeastSignificantBits: UByte = 0x34u
        val indirectAddress: UShort = 0x1234u

        testBus.ram[firstByteAddress.toInt()] = operandAddressLeastSignificantBits
        testBus.ram[secondByteAddress.toInt()] = operandAddressMostSignificantBits
        testCPU.programCounter = startAddress

        val targetSecondByteAddress: UShort = 0x1235u
        val targetAddressMostSignificantByte: UByte = 0x56u
        val targetAddressLeastSignificantByte: UByte = 0x78u
        val targetAddress: UShort = 0x5678u

        testBus.ram[indirectAddress.toInt()] = targetAddressLeastSignificantByte
        testBus.ram[targetSecondByteAddress.toInt()] = targetAddressMostSignificantByte

        assertEquals(targetAddress, testCPU.indirectAddressing())
    }

    @Test
    fun `test indexed indirect addressing mode`() {
        val startAddress: UShort = 0x0031u
        val operandAddress: UShort = 0x0032u
        val operand: UByte = 0x51u
        val xRegisterValue: UByte = 0xE9u
        val zeroPageAddress: UShort = 0x003Au
        val targetMostSignificantByte: UByte = 0x12u
        val targetLeastSignificantByte: UByte = 0x34u
        val targetAddress: UShort = 0x1234u

        testCPU.programCounter = startAddress
        testBus.ram[operandAddress.toInt()] = operand
        testCPU.xRegister = xRegisterValue
        testBus.ram[zeroPageAddress.toInt()] = targetLeastSignificantByte
        testBus.ram[zeroPageAddress.toInt() + 1] = targetMostSignificantByte

        assertEquals(targetAddress, testCPU.xIndexedIndirectAddressing())
    }

    @Test
    fun `test indirect indexed addressing mode`() {
        val startAddress: UShort = 0x1000u
        val operand: UByte = 0x51u
        val yRegisterValue: UByte = 0xE9u
        //val targetLSBWithCarry: UShort = 0x013Au
        //val targetLSB: UByte = 0x3Au
        val targetMSBOperand: UByte = 0x3Fu
        //val targetMSB: UByte = 0x40u
        val targetAddress: UShort = 0x403Au

        testCPU.programCounter = startAddress
        testCPU.yRegister = yRegisterValue
        testBus.ram[startAddress.toInt() + 1] = operand
        testBus.ram[startAddress.toInt() + 2] = targetMSBOperand

        assertEquals(targetAddress, testCPU.indirectYIndexedAddressing())
    }
}