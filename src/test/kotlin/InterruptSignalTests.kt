import CPU.CPU6502
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

@ExperimentalUnsignedTypes
class InterruptSignalTests {
    private lateinit var testCPU: CPU6502
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_536)
        testBus = Bus(testRam)
        testCPU = CPU6502(testBus)
    }

    @Test
    fun `test NMI interrupt signal`() {
        val currentAddress: UShort = 0x0105u
        val vectorMostSignificantByte: UByte = 0x31u
        val vectorLeastSignificantByte: UByte = 0x32u
        val result: UShort = 0x3132u

        testCPU.apply {
            programCounter = currentAddress

            bus.ram[(0xFFFAu).toInt()] = vectorLeastSignificantByte
            bus.ram[(0xFFFBu).toInt()] = vectorMostSignificantByte

            negativeFlag = true
            overflowFlag = false
            extraFlag = true
            breakFlag = false
            decimalFlag = true
            interruptDisableFlag = false
            zeroFlag = true
            carryFlag = false

            nmi()
        }

        testCPU.also {
            assertEquals(result, it.programCounter)
            assertEquals(true, it.interruptDisableFlag)
            assertEquals((0xFAu).toUByte(), it.stackPointer)
            assertEquals((0x01u).toUByte(), it.bus.ram[(0xFDu).toInt()])
            assertEquals((0x05u).toUByte(), it.bus.ram[(0xFCu).toInt()])
            assertEquals((0xAAu).toUByte(), it.bus.ram[(0xFBu).toInt()])
        }
    }

    @Test
    fun `test IRQ interrupt signal branching`() {
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
            assertEquals(result, it.programCounter)
            assertEquals(true, it.interruptDisableFlag)
            assertEquals((0xFAu).toUByte(), it.stackPointer)
            assertEquals((0x01u).toUByte(), it.bus.ram[(0xFDu).toInt()])
            assertEquals((0x05u).toUByte(), it.bus.ram[(0xFCu).toInt()])
            assertEquals((0xAAu).toUByte(), it.bus.ram[(0xFBu).toInt()])
        }
    }

    @Test
    fun `test IRQ interrupt signal non branching`() {
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
            interruptDisableFlag = true
            zeroFlag = true
            carryFlag = false

            irq()
        }

        testCPU.also {
            assertEquals(currentAddress, it.programCounter)
            assertEquals((0xFDu).toUByte(), it.stackPointer)
        }
    }

    @Test
    fun `test RESET interrupt signal`() {
        val currentAddress: UShort = 0x0105u
        val vectorMostSignificantByte: UByte = 0x31u
        val vectorLeastSignificantByte: UByte = 0x32u
        val result: UShort = 0x3132u

        testCPU.apply {
            programCounter = currentAddress
            bus.ram[0xFFFC] = vectorLeastSignificantByte
            bus.ram[0xFFFD] = vectorMostSignificantByte
            reset()
        }

        testCPU.also {
            assertEquals(result, it.programCounter)
        }
    }

}