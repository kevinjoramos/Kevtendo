import CPU.CPU
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

@ExperimentalUnsignedTypes
class FlagInstructionTests {
    private lateinit var testCPU: CPU
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(16)
        testBus = Bus(testRam)
        testCPU = CPU(testBus)
    }

    @Test
    fun `test CLC Clear Carry Flag`() {
        testCPU.carryFlag = true
        testCPU.CLC()
        assertEquals(false, testCPU.carryFlag)
    }

    @Test
    fun `test CLD Clear decimal flag`() {
        testCPU.decimalFlag = true
        testCPU.CLD()
        assertEquals(false, testCPU.decimalFlag)
    }

    @Test
    fun `test CLI Clear interrupt disable flag`() {
        testCPU.interruptDisableFlag = true
        testCPU.CLI()
        assertEquals(false, testCPU.interruptDisableFlag)
    }

    @Test
    fun `test CLV Clear overflow flag`() {
        testCPU.overflowFlag = true
        testCPU.CLV()
        assertEquals(false, testCPU.overflowFlag)
    }

    @Test
    fun `test SEC Set carry flag`() {
        testCPU.carryFlag = false
        testCPU.SEC()
        assertEquals(true, testCPU.carryFlag)
    }

    @Test
    fun `test SED Set decimal flag`() {
        testCPU.decimalFlag = false
        testCPU.SED()
        assertEquals(true, testCPU.decimalFlag)
    }

    @Test
    fun `test SEI Set interrupt disable flag`() {
        testCPU.interruptDisableFlag = false
        testCPU.SEI()
        assertEquals(false, testCPU.interruptDisableFlag)
    }

}