import CPU.CPU6502
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class ControlInstructionTests {
    private lateinit var testCPU: CPU6502
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_536)
        testBus = Bus(testRam)
        testCPU = CPU6502(testBus)
    }

    /**
     * BRK
     */

    @Test
    fun `test BRK break command`() {
        val currentAddress: UShort = 0x0105u
        val vectorMostSignificantByte: UByte = 0x31u
        val vectorLeastSignificantByte: UByte = 0x32u
        val result: UShort = 0x3132u

        testCPU.apply {
            programCounter = currentAddress

            bus.ram[(0xFFFEu).toInt()] = vectorLeastSignificantByte
            bus.ram[(0xFFFFu).toInt()] = vectorMostSignificantByte

            negativeFlag = true
            overflowFlag = false
            extraFlag = true
            breakFlag = false
            decimalFlag = true
            interruptDisableFlag = false
            zeroFlag = true
            carryFlag = false

            irq()
        }

        testCPU.also {
            Assertions.assertEquals(result, it.programCounter)
            Assertions.assertEquals(true, it.interruptDisableFlag)
            Assertions.assertEquals((0xFAu).toUByte(), it.stackPointer)
            Assertions.assertEquals((0x01u).toUByte(), it.bus.ram[(0xFDu).toInt()])
            Assertions.assertEquals((0x06u).toUByte(), it.bus.ram[(0xFCu).toInt()])
            Assertions.assertEquals((0xAAu).toUByte(), it.bus.ram[(0xFBu).toInt()])
        }
    }

}