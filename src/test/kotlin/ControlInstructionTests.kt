import CPU.CPU6502
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class ControlInstructionTests {
    private lateinit var testCPU: CPU6502
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_536)
        testCPU = CPU6502()
        testBus = Bus(testCPU, testRam)
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

            BRK().execute()
        }

        testCPU.also {
            assertEquals(result, it.programCounter)
            assertEquals(true, it.interruptDisableFlag)
            assertEquals((0xFAu).toUByte(), it.stackPointer)
            assertEquals((0x01u).toUByte(), it.bus.ram[(0xFDu).toInt()])
            assertEquals((0x06u).toUByte(), it.bus.ram[(0xFCu).toInt()])
            assertEquals((0xAAu).toUByte(), it.bus.ram[(0xFBu).toInt()])
        }
    }

    /**
     * JMP
     */

    @Test
    fun `test JMP changes program counter`() {
        val currentAddress: UShort = 0x0105u
        val targetAddress: UShort = 0x3132u

        testCPU.apply {
            programCounter = currentAddress
            JMP().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(targetAddress, it.programCounter)
        }
    }

    /**
     * JSR
     */

    @Test
    fun `test JSR jump to subroutine`() {
        val targetAddress: UShort = 0x3132u
        val currentAddress: UShort = 0x0105u

        testCPU.apply {
            programCounter = currentAddress
            JSR().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(targetAddress, it.programCounter)
            assertEquals((0xFBu).toUByte(), it.stackPointer)
            assertEquals((0x01u).toUByte(), it.bus.ram[(0xFDu).toInt()])
            assertEquals((0x05u).toUByte(), it.bus.ram[(0xFCu).toInt()])
        }
    }

    /**
     * RTI
     */

    @Test
    fun `test RTI return from interrupt`() {
        val currentAddress: UShort = 0x3132u
        val result: UShort = 0x0105u

        testCPU.apply {
            programCounter = currentAddress
            bus.ram[(0xFDu).toInt()] = 0x01u
            bus.ram[(0xFCu).toInt()] = 0x05u
            bus.ram[(0xFBu).toInt()] = 0xAAu
            stackPointer = 0xFAu

            RTI().execute()
        }

        testCPU.also {
            assertEquals(result, it.programCounter)
            assertEquals((0xFDu).toUByte(), it.stackPointer)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(true, it.extraFlag)
            assertEquals(false, it.breakFlag)
            assertEquals(true, it.decimalFlag)
            assertEquals(false, it.interruptDisableFlag)
            assertEquals(true, it.zeroFlag)
            assertEquals(false, it.carryFlag)
        }
    }

    /**
     * RTS
     */

    @Test
    fun `test RTS return from subroutine`() {
        val currentAddress: UShort = 0x3132u
        val result: UShort = 0x0106u

        testCPU.apply {
            programCounter = currentAddress
            bus.ram[(0xFDu).toInt()] = 0x01u
            bus.ram[(0xFCu).toInt()] = 0x05u
            stackPointer = 0xFBu

            RTS().execute()
        }

        testCPU.also {
            assertEquals(result, it.programCounter)
            assertEquals((0xFDu).toUByte(), it.stackPointer)
        }
    }


}