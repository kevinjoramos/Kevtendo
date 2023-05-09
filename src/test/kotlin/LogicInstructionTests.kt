import CPU.CPU
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class LogicInstructionTests {
    private lateinit var testCPU: CPU
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_535)
        testBus = Bus(testRam)
        testCPU = CPU(testBus)
    }

    @Test
    fun `test AND and memory with accumulator`() {
        val targetAddress: UShort = 0x0105u
        val accumulatorValue: UByte = 0x77u
        val data: UByte = 0xAAu
        val result: UByte = 0x22u

        testCPU.apply {
            accumulator = accumulatorValue
            bus.ram[targetAddress.toInt()] = data
            negativeFlag = true
            zeroFlag = true
            AND().run(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    fun `test AND and memory with accumulator with negative`() {
        val targetAddress: UShort = 0x0105u
        val accumulatorValue: UByte = 0xFFu
        val data: UByte = 0x80u
        val result: UByte = 0x80u

        testCPU.apply {
            accumulator = accumulatorValue
            bus.ram[targetAddress.toInt()] = data
            negativeFlag = false
            zeroFlag = true
            AND().run(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    fun `test AND and memory with accumulator with zero`() {
        val targetAddress: UShort = 0x0105u
        val accumulatorValue: UByte = 0x55u
        val data: UByte = 0xAAu
        val result: UByte = 0x00u

        testCPU.apply {
            accumulator = accumulatorValue
            bus.ram[targetAddress.toInt()] = data
            negativeFlag = true
            zeroFlag = false
            AND().run(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }
}