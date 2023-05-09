import CPU.CPU
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class StackInstructionTests {
    private lateinit var testCPU: CPU
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_535)
        testBus = Bus(testRam)
        testCPU = CPU(testBus)
    }

    /**
     * PHA
     */

    @Test
    fun `test PHA push accumulator on stack`() {
        val data: UByte = 0xADu

        testCPU.apply {
            accumulator = data
            stackPointer = 0xFFu
            PHA().run(0x0000u)
        }

        testCPU.also {
            assertEquals(0xFEu, it.stackPointer)
            assertEquals(data, it.bus.ram[(0xFFu).toInt()])
        }
    }
}