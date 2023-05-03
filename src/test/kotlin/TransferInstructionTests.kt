import CPU.CPU
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class TransferInstructionTests {
    private lateinit var testCPU: CPU
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_535)
        testBus = Bus(testRam)
        testCPU = CPU(testBus)
    }

    @Test
    fun `test LDA load accumulator with memory`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x05u

        testBus.ram[targetAddress.toInt()] = data
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true

        testCPU.LDA().run(targetAddress)
        assertEquals(data, testCPU.accumulator)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test LDA load accumulator with memory with negative flag`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x80u

        testBus.ram[targetAddress.toInt()] = data
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true

        testCPU.LDA().run(targetAddress)
        assertEquals(data, testCPU.accumulator)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test LDA load accumulator with memory with zero flag`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x00u

        testBus.ram[targetAddress.toInt()] = data
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true

        testCPU.LDA().run(targetAddress)
        assertEquals(data, testCPU.accumulator)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test LDX load x register with memory`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x05u

        testBus.ram[targetAddress.toInt()] = data
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true

        testCPU.LDX().run(targetAddress)
        assertEquals(data, testCPU.xRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test LDX load x register with memory with negative flag`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x80u

        testBus.ram[targetAddress.toInt()] = data
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true

        testCPU.LDX().run(targetAddress)
        assertEquals(data, testCPU.xRegister)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test LDX load x register with memory with zero flag`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x00u

        testBus.ram[targetAddress.toInt()] = data
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true

        testCPU.LDX().run(targetAddress)
        assertEquals(data, testCPU.xRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test LDY load y register with memory`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x05u

        testBus.ram[targetAddress.toInt()] = data
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true

        testCPU.LDY().run(targetAddress)
        assertEquals(data, testCPU.yRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test LDY load y register with memory with negative flag`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x80u

        testBus.ram[targetAddress.toInt()] = data
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true

        testCPU.LDY().run(targetAddress)
        assertEquals(data, testCPU.yRegister)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test LDY load y register with memory with zero flag`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x00u

        testBus.ram[targetAddress.toInt()] = data
        testCPU.negativeFlag = true
        testCPU.zeroFlag = true

        testCPU.LDY().run(targetAddress)
        assertEquals(data, testCPU.yRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test STA store accumulator in memory`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x35u

        testCPU.apply {
            accumulator = data
            negativeFlag = false
            zeroFlag = false
            STA().run(targetAddress)
        }

        assertEquals(data, testBus.ram[targetAddress.toInt()])
        assertEquals(data, testCPU.accumulator)
        assertEquals(false, testCPU.zeroFlag)
        assertEquals(false, testCPU.negativeFlag)
    }

    @Test
    fun `test STX store x register in memory`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x35u

        testCPU.apply {
            xRegister = data
            negativeFlag = false
            zeroFlag = false
            STX().run(targetAddress)
        }

        assertEquals(data, testBus.ram[targetAddress.toInt()])
        assertEquals(data, testCPU.xRegister)
        assertEquals(false, testCPU.zeroFlag)
        assertEquals(false, testCPU.negativeFlag)
    }

    @Test
    fun `test STY store y register in memory`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x35u

        testCPU.apply {
            yRegister = data
            negativeFlag = false
            zeroFlag = false
            STY().run(targetAddress)
        }

        assertEquals(data, testBus.ram[targetAddress.toInt()])
        assertEquals(data, testCPU.yRegister)
        assertEquals(false, testCPU.zeroFlag)
        assertEquals(false, testCPU.negativeFlag)
    }

    @Test
    fun `test TAX transfer accumulator to x register`() {
        val data: UByte = 0x45u

        testCPU.apply {
            accumulator = data
            negativeFlag = true
            zeroFlag = true
            TAX().run(0x0000u)
        }

        assertEquals(data, testCPU.xRegister)
        assertEquals(data, testCPU.accumulator)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test TAX transfer accumulator to x register with negative flag`() {
        val data: UByte = 0x80u

        testCPU.apply {
            accumulator = data
            negativeFlag = false
            zeroFlag = true
            TAX().run(0x0000u)
        }

        assertEquals(data, testCPU.xRegister)
        assertEquals(data, testCPU.accumulator)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test TAX transfer accumulator to x register with zero flag`() {
        val data: UByte = 0x00u

        testCPU.apply {
            accumulator = data
            negativeFlag = true
            zeroFlag = false
            TAX().run(0x0000u)
        }

        assertEquals(data, testCPU.xRegister)
        assertEquals(data, testCPU.accumulator)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test TAY transfer accumulator to y register`() {
        val data: UByte = 0x45u

        testCPU.apply {
            accumulator = data
            negativeFlag = true
            zeroFlag = true
            TAY().run(0x0000u)
        }

        assertEquals(data, testCPU.yRegister)
        assertEquals(data, testCPU.accumulator)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test TAY transfer accumulator to y register with negative flag`() {
        val data: UByte = 0x80u

        testCPU.apply {
            accumulator = data
            negativeFlag = false
            zeroFlag = true
            TAY().run(0x0000u)
        }

        assertEquals(data, testCPU.yRegister)
        assertEquals(data, testCPU.accumulator)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test TAY transfer accumulator to y register with zero flag`() {
        val data: UByte = 0x00u

        testCPU.apply {
            accumulator = data
            negativeFlag = true
            zeroFlag = false
            TAY().run(0x0000u)
        }

        assertEquals(data, testCPU.yRegister)
        assertEquals(data, testCPU.accumulator)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test TSX transfer stack pointer to x register`() {
        val data: UByte = 0x45u

        testCPU.apply {
            stackPointer = data
            negativeFlag = true
            zeroFlag = true
            TSX().run(0x0000u)
        }

        assertEquals(data, testCPU.xRegister)
        assertEquals(data, testCPU.stackPointer)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test TSX transfer stack pointer to x register with negative flag`() {
        val data: UByte = 0x80u

        testCPU.apply {
            stackPointer = data
            negativeFlag = false
            zeroFlag = true
            TSX().run(0x0000u)
        }

        assertEquals(data, testCPU.xRegister)
        assertEquals(data, testCPU.stackPointer)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test TSX transfer stack pointer to x register with zero flag`() {
        val data: UByte = 0x00u

        testCPU.apply {
            stackPointer = data
            negativeFlag = true
            zeroFlag = false
            TSX().run(0x0000u)
        }

        assertEquals(data, testCPU.xRegister)
        assertEquals(data, testCPU.stackPointer)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test TXA transfer x register to accumulator`() {
        val data: UByte = 0x45u

        testCPU.apply {
            xRegister = data
            negativeFlag = true
            zeroFlag = true
            TXA().run(0x0000u)
        }

        assertEquals(data, testCPU.accumulator)
        assertEquals(data, testCPU.xRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test TXA transfer x register to accumulator with negative flag`() {
        val data: UByte = 0x80u

        testCPU.apply {
            xRegister = data
            negativeFlag = false
            zeroFlag = true
            TXA().run(0x0000u)
        }

        assertEquals(data, testCPU.accumulator)
        assertEquals(data, testCPU.xRegister)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test TXA transfer x register to accumulator with zero flag`() {
        val data: UByte = 0x00u

        testCPU.apply {
            xRegister = data
            negativeFlag = true
            zeroFlag = false
            TXA().run(0x0000u)
        }

        assertEquals(data, testCPU.accumulator)
        assertEquals(data, testCPU.xRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test TXS transfer x register to stack pointer`() {
        val data: UByte = 0x00u

        testCPU.apply {
            xRegister = data
            TXS().run(0x0000u)
        }

        assertEquals(data, testCPU.stackPointer)
        assertEquals(data, testCPU.xRegister)
    }

    @Test
    fun `test TYA transfer y register to accumulator`() {
        val data: UByte = 0x45u

        testCPU.apply {
            yRegister = data
            negativeFlag = true
            zeroFlag = true
            TYA().run(0x0000u)
        }

        assertEquals(data, testCPU.accumulator)
        assertEquals(data, testCPU.yRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test TYA transfer y register to accumulator with negative flag`() {
        val data: UByte = 0x80u

        testCPU.apply {
            yRegister = data
            negativeFlag = false
            zeroFlag = true
            TYA().run(0x0000u)
        }

        assertEquals(data, testCPU.accumulator)
        assertEquals(data, testCPU.yRegister)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test TYA transfer y register to accumulator with zero flag`() {
        val data: UByte = 0x00u

        testCPU.apply {
            yRegister = data
            negativeFlag = true
            zeroFlag = false
            TYA().run(0x0000u)
        }

        assertEquals(data, testCPU.accumulator)
        assertEquals(data, testCPU.yRegister)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }
}
