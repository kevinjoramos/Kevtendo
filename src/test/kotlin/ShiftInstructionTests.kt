import CPU.CPU
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

@ExperimentalUnsignedTypes
class ShiftInstructionTests {
    private lateinit var testCPU: CPU
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_535)
        testBus = Bus(testRam)
        testCPU = CPU(testBus)
    }


    @Test
    fun `test ASL arithmetic shift left on accumulator`() {
        val data: UByte = 0x01u
        val result: UByte = 0x02u

        testCPU.apply {
            accumulator = data
            carryFlag = true
            negativeFlag = true
            zeroFlag = true
            ASLA().run(0x0000u)
        }

        assertEquals(result, testCPU.accumulator)
        assertEquals(false, testCPU.carryFlag)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test ASL arithmetic shift left on accumulator with carry`() {
        val data: UByte = 0x80u
        val result: UByte = 0x00u

        testCPU.apply {
            accumulator = data
            carryFlag = false
            negativeFlag = true
            zeroFlag = false
            ASLA().run(0x0000u)
        }

        assertEquals(result, testCPU.accumulator)
        assertEquals(true, testCPU.carryFlag)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test ASL arithmetic shift left on accumulator with negative`() {
        val data: UByte = 0x40u
        val result: UByte = 0x80u

        testCPU.apply {
            accumulator = data
            carryFlag = true
            negativeFlag = false
            zeroFlag = true
            ASLA().run(0x0000u)
        }

        assertEquals(result, testCPU.accumulator)
        assertEquals(false, testCPU.carryFlag)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test ASL arithmetic shift left`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x01u
        val result: UByte = 0x02u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = true
            negativeFlag = true
            zeroFlag = true
            ASL().run(targetAddress)
        }

        assertEquals(result, testCPU.bus.ram[targetAddress.toInt()])
        assertEquals(false, testCPU.carryFlag)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test ASL arithmetic shift left with carry`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x80u
        val result: UByte = 0x00u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = false
            negativeFlag = true
            zeroFlag = false
            ASL().run(targetAddress)
        }

        assertEquals(result, testCPU.bus.ram[targetAddress.toInt()])
        assertEquals(true, testCPU.carryFlag)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test ASL arithmetic shift left with negative`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x40u
        val result: UByte = 0x80u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = true
            negativeFlag = false
            zeroFlag = true
            ASL().run(targetAddress)
        }

        assertEquals(result, testCPU.bus.ram[targetAddress.toInt()])
        assertEquals(false, testCPU.carryFlag)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }
}