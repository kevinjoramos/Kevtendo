import CPU.CPU6502
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class IncrementAndDecrementInstructionTests {
    private lateinit var testCPU: CPU6502
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_536)
        testCPU = CPU6502()
        testBus = Bus(testCPU, testRam)
    }

    @Test
    fun `test DEC decrement no flags`() {
        val operandAddress: UShort = 0x0105u
        val operand: UByte = 0x05u
        val result: UByte = 0x04u

        testBus.ram[operandAddress.toInt()] = operand
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true
        testCPU.DEC().execute(operandAddress)


        assertEquals(result, testBus.ram[operandAddress.toInt()])
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test DEC decrement zero flag`() {
        val operandAddress: UShort = 0x0105u
        val operand: UByte = 0x01u
        val result: UByte = 0x00u

        testBus.ram[operandAddress.toInt()] = operand
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true
        testCPU.DEC().execute(operandAddress)

        assertEquals(result, testBus.ram[operandAddress.toInt()])
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test DEC decrement with negative flag`() {
        val operandAddress: UShort = 0x0105u
        val operand: UByte = 0x00u
        val result: UByte = 0xFFu

        testBus.ram[operandAddress.toInt()] = operand
        testCPU.negativeFlag = false
        testCPU.zeroFlag = false
        testCPU.DEC().execute(operandAddress)

        assertEquals(result, testBus.ram[operandAddress.toInt()])
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test DEX decrement X no flags`() {
        testCPU.xRegister = 0x05u
        val result: UByte = 0x04u

        testCPU.negativeFlag = true
        testCPU.zeroFlag = true
        testCPU.DEX().execute()

        assertEquals(result, testCPU.xRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test DEX decrement X zero flag`() {
        testCPU.xRegister = 0x01u
        val result: UByte = 0x00u

        testCPU.negativeFlag = true
        testCPU.zeroFlag = true
        testCPU.DEX().execute()

        assertEquals(result, testCPU.xRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test DEX decrement X with negative flag`() {
        testCPU.xRegister = 0x00u
        val result: UByte = 0xFFu

        testCPU.negativeFlag = false
        testCPU.zeroFlag = false
        testCPU.DEX().execute()

        assertEquals(result, testCPU.xRegister)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test DEY decrement Y no flags`() {
        testCPU.yRegister = 0x05u
        val result: UByte = 0x04u

        testCPU.negativeFlag = true
        testCPU.zeroFlag = true
        testCPU.DEY().execute()

        assertEquals(result, testCPU.yRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test DEY decrement Y zero flag`() {
        testCPU.yRegister = 0x01u
        val result: UByte = 0x00u

        testCPU.negativeFlag = true
        testCPU.zeroFlag = true
        testCPU.DEY().execute()

        assertEquals(result, testCPU.yRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test DEY decrement Y with negative flag`() {
        testCPU.yRegister = 0x00u
        val result: UByte = 0xFFu

        testCPU.negativeFlag = false
        testCPU.zeroFlag = false
        testCPU.DEY().execute()

        assertEquals(result, testCPU.yRegister)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test INC increment memory no flags`() {
        val operandAddress: UShort = 0x0105u
        val operand: UByte = 0x05u
        val result: UByte = 0x06u

        testBus.ram[operandAddress.toInt()] = operand
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true
        testCPU.INC().execute(operandAddress)


        assertEquals(result, testBus.ram[operandAddress.toInt()])
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test INC increment memory zero flag`() {
        val operandAddress: UShort = 0x0105u
        val operand: UByte = 0xFFu
        val result: UByte = 0x00u

        testBus.ram[operandAddress.toInt()] = operand
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true
        testCPU.INC().execute(operandAddress)

        assertEquals(result, testBus.ram[operandAddress.toInt()])
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test INC increment memory with negative flag`() {
        val operandAddress: UShort = 0x0105u
        val operand: UByte = 0x7Fu
        val result: UByte = 0x80u

        testBus.ram[operandAddress.toInt()] = operand
        testCPU.negativeFlag = false
        testCPU.zeroFlag = false
        testCPU.INC().execute(operandAddress)

        assertEquals(result, testBus.ram[operandAddress.toInt()])
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test INX increment X no flags`() {
        testCPU.xRegister = 0x05u
        val result: UByte = 0x06u

        testCPU.negativeFlag = true
        testCPU.zeroFlag = true
        testCPU.INX().execute()

        assertEquals(result, testCPU.xRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test INX increment X zero flag`() {
        testCPU.xRegister = 0xFFu
        val result: UByte = 0x00u

        testCPU.negativeFlag = true
        testCPU.zeroFlag = true
        testCPU.INX().execute()

        assertEquals(result, testCPU.xRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test INX increment X with negative flag`() {
        testCPU.xRegister = 0x7Fu
        val result: UByte = 0x80u

        testCPU.negativeFlag = false
        testCPU.zeroFlag = false
        testCPU.INX().execute()

        assertEquals(result, testCPU.xRegister)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test INY increment Y no flags`() {
        testCPU.yRegister = 0x05u
        val result: UByte = 0x06u

        testCPU.negativeFlag = true
        testCPU.zeroFlag = true
        testCPU.INY().execute()

        assertEquals(result, testCPU.yRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test INY increment Y zero flag`() {
        testCPU.yRegister = 0xFFu
        val result: UByte = 0x00u

        testCPU.negativeFlag = true
        testCPU.zeroFlag = true
        testCPU.INY().execute()

        assertEquals(result, testCPU.yRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test INY increment Y with negative flag`() {
        testCPU.yRegister = 0x7Fu
        val result: UByte = 0x80u

        testCPU.negativeFlag = false
        testCPU.zeroFlag = false
        testCPU.INY().execute()

        assertEquals(result, testCPU.yRegister)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

}