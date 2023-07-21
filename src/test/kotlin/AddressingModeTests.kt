import bus.Bus
import bus.TestBus
import cpu.CPU6502
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class AddressingModeTests {
    private lateinit var testCPU: CPU6502
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRamSize = 65_536
        val testBus = TestBus(testRamSize)
        testCPU = testBus.cpu
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

        testBus.apply {
            testBus.ram[(opcodeAddress + 1u).toInt()] = leastSignificantByte
            testBus.ram[(opcodeAddress + 2u).toInt()] = mostSignificantByte
        }

        testBus.apply {

        }

        testCPU.apply {
            programCounter = opcodeAddress
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

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = leastSignificantByte
            ram[(opcodeAddress + 2u).toInt()] = mostSignificantByte
        }

        testBus.apply {

        }

        testCPU.apply {
            programCounter = opcodeAddress
            xRegister = xRegisterValue
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

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = leastSignificantByte
            ram[(opcodeAddress + 2u).toInt()] = mostSignificantByte
        }

        testBus.apply {

        }

        testCPU.apply {
            programCounter = opcodeAddress
            xRegister = xRegisterValue
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

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = leastSignificantByte
            ram[(opcodeAddress + 2u).toInt()] = mostSignificantByte
        }

        testBus.apply {

        }

        testCPU.apply {
            programCounter = opcodeAddress
            yRegister = yRegisterValue
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

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = leastSignificantByte
            ram[(opcodeAddress + 2u).toInt()] = mostSignificantByte
        }

        testBus.apply {

        }

        testCPU.apply {
            programCounter = opcodeAddress
            yRegister = yRegisterValue
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

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = zeroPageAddress
        }

        testBus.apply {

        }

        testCPU.apply {
            programCounter = opcodeAddress
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

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = zeroPageAddress
        }

        testBus.apply {

        }

        testCPU.apply {
            programCounter = opcodeAddress
            xRegister = xRegisterValue
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
        val result: UShort = 0x007Fu

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = zeroPageAddress
        }

        testBus.apply {

        }

        testCPU.apply {
            programCounter = opcodeAddress
            xRegister = xRegisterValue
        }

        testCPU.also {
            assertEquals(result, it.zeroPageXIndexedAddressing())
            assertEquals((opcodeAddress + 1u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test get zero page address with Zero Y Page Addressing`() {
        val opcodeAddress: UShort = 0x0105u
        val zeroPageAddress: UByte = 0x80u
        val yRegisterValue: UByte = 0x02u
        val result: UShort = 0x0082u

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = zeroPageAddress
        }

        testCPU.apply {
            programCounter = opcodeAddress
            yRegister = yRegisterValue
        }

        testCPU.also {
            assertEquals(result, it.zeroPageYIndexedAddressing())
            assertEquals((opcodeAddress + 1u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test get zero page address with Zero Y Page Addressing with wrap`() {
        val opcodeAddress: UShort = 0x0105u
        val zeroPageAddress: UByte = 0x80u
        val yRegisterValue: UByte = 0xFFu
        val result: UShort = 0x007Fu

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = zeroPageAddress
        }

        testCPU.apply {
            programCounter = opcodeAddress
            yRegister = yRegisterValue
        }

        testCPU.also {
            assertEquals(result, it.zeroPageYIndexedAddressing())
            assertEquals((opcodeAddress + 1u).toUShort(), it.programCounter)
        }
    }

    /**
     * Indirect Addressing
     */

    @Test
    fun `test indirect addressing mode`() {
        val opcodeAddress: UShort = 0x0105u
        val operandMostSignificantByte: UByte = 0xFFu
        val operandLeastSignificantByte: UByte = 0x82u
        val operandResultAddress: UShort = 0xFF82u
        val targetMostSignificantByte: UByte = 0x80u
        val targetLeastSignificantByte: UByte = 0xC4u
        val resultTargetAddress: UShort = 0x80C4u

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = operandLeastSignificantByte
            ram[(opcodeAddress + 2u).toInt()] = operandMostSignificantByte
            ram[(operandResultAddress).toInt()] = targetLeastSignificantByte
            ram[(operandResultAddress + 1u).toInt()] = targetMostSignificantByte
        }

        testCPU.apply {
            programCounter = opcodeAddress
        }

        testCPU.also {
            assertEquals(resultTargetAddress, it.indirectAddressing())
            assertEquals((opcodeAddress + 2u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test pre-indexed indirect addressing mode (x indexed indirect)`() {
        val opcodeAddress: UShort = 0x0105u
        val operand: UByte = 0x70u
        val xRegisterValue: UByte = 0x05u
        val indirectAddress: UShort = 0x0075u
        val targetMostSignificantByte: UByte = 0x30u
        val targetLeastSignificantByte: UByte = 0x32u
        val targetAddress: UShort = 0x3032u

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = operand
            ram[(indirectAddress).toInt()] = targetLeastSignificantByte
            ram[(indirectAddress + 1u).toInt()] = targetMostSignificantByte
        }

        testCPU.apply {
            programCounter = opcodeAddress
            xRegister = xRegisterValue
        }

        testCPU.also {
            assertEquals(targetAddress, it.xIndexedIndirectAddressing())
            assertEquals((opcodeAddress + 1u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test pre-indexed indirect addressing mode (x indexed indirect) with wrap`() {
        val opcodeAddress: UShort = 0x0105u
        val operand: UByte = 0xFFu
        val xRegisterValue: UByte = 0x05u
        val indirectAddress: UShort = 0x0004u
        val targetMostSignificantByte: UByte = 0x30u
        val targetLeastSignificantByte: UByte = 0x32u
        val targetAddress: UShort = 0x3032u

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = operand
            ram[(indirectAddress).toInt()] = targetLeastSignificantByte
            ram[(indirectAddress + 1u).toInt()] = targetMostSignificantByte
        }

        testCPU.apply {
            programCounter = opcodeAddress
            xRegister = xRegisterValue
        }

        testCPU.also {
            assertEquals(targetAddress, it.xIndexedIndirectAddressing())
            assertEquals((opcodeAddress + 1u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test pre-indexed indirect addressing mode (x indexed indirect) split`() {
        val opcodeAddress: UShort = 0x0105u
        val operand: UByte = 0xF0u
        val xRegisterValue: UByte = 0x0Fu
        val indirectAddress: UShort = 0x00FFu
        val targetMostSignificantByte: UByte = 0x30u
        val targetLeastSignificantByte: UByte = 0x32u
        val targetAddress: UShort = 0x3032u

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = operand
            ram[(indirectAddress).toInt()] = targetLeastSignificantByte
            ram[0] = targetMostSignificantByte
        }

        testCPU.apply {
            programCounter = opcodeAddress
            xRegister = xRegisterValue
        }

        testCPU.also {
            assertEquals(targetAddress, it.xIndexedIndirectAddressing())
            assertEquals((opcodeAddress + 1u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test post-indexed indirect addressing mode (indirect y indexed)`() {
        val opcodeAddress: UShort = 0x0105u
        val operand: UByte = 0x70u
        val indirectAddress: UShort = 0x0070u
        val targetMostSignificantByte: UByte = 0x35u
        val targetLeastSignificantByte: UByte = 0x43u
        val yRegisterValue: UByte = 0x10u
        val targetAddress: UShort = 0x3553u

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = operand
            ram[(indirectAddress).toInt()] = targetLeastSignificantByte
            ram[(indirectAddress + 1u).toInt()] = targetMostSignificantByte
        }

        testCPU.apply {
            programCounter = opcodeAddress
            yRegister = yRegisterValue
        }

        testCPU.also {
            assertEquals(targetAddress, it.indirectYIndexedAddressing())
            assertEquals((opcodeAddress + 1u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test post-indexed indirect addressing mode (indirect y indexed) wrap`() {
        val opcodeAddress: UShort = 0x0105u
        val operand: UByte = 0x70u
        val indirectAddress: UShort = 0x0070u
        val targetMostSignificantByte: UByte = 0xFFu
        val targetLeastSignificantByte: UByte = 0xFFu
        val yRegisterValue: UByte = 0x10u
        val targetAddress: UShort = 0x000Fu

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = operand
            ram[(indirectAddress).toInt()] = targetLeastSignificantByte
            ram[(indirectAddress + 1u).toInt()] = targetMostSignificantByte
        }

        testCPU.apply {
            programCounter = opcodeAddress
            yRegister = yRegisterValue
        }

        testCPU.also {
            assertEquals(targetAddress, it.indirectYIndexedAddressing())
            assertEquals((opcodeAddress + 1u).toUShort(), it.programCounter)
        }
    }

    @Test
    fun `test relative addressing mode offset`() {
        val opcodeAddress: UShort = 0x1000u
        val offset: UByte = 0x03u
        val targetAddress: UShort = 0x1005u

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = offset
        }

        testCPU.apply {
            programCounter = opcodeAddress
        }

        testCPU.also {
            assertEquals(targetAddress, it.relativeAddressing())
        }
    }

    @Test
    fun `test relative addressing mode negative offset with wrap`() {
        val opcodeAddress: UShort = 0x0000u
        val offset: UByte = 0x80u
        val targetAddress: UShort = 0xFF82u

        testBus.apply {
            ram[(opcodeAddress + 1u).toInt()] = offset
        }

        testCPU.apply {
            programCounter = opcodeAddress
        }

        testCPU.also {
            assertEquals(targetAddress, it.relativeAddressing())
        }
    }
}