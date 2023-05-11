import CPU.CPU6502
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class ArithmeticInstructionTests {
    private lateinit var testCPU: CPU6502
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_535)
        testBus = Bus(testRam)
        testCPU = CPU6502(testBus)
    }

    /**
     * ADC
     */

    @Test
    fun `test ADC add memory to accumulator with carry`() {
        val data: UByte = 0x23u
        val accumulatorValue: UByte = 0x32u
        val result = 0x55u

        testCPU.apply {
            bus.ram[]
        }
    }
}