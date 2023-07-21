import bus.Bus
import cpu.CPU6502
import ppu.PPU2C02
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class BranchInstructionTests {
    private lateinit var testCPU: CPU6502
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_536)
        val testPPU = PPU2C02()
        testCPU = CPU6502()
        testBus = Bus(testCPU, testRam, testPPU)
    }

    /**
     * BCC
     */

    @Test
    fun `test BCC branch on carry clear, branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            carryFlag = false
            BCC().execute(targetAddress)
        }

        assertEquals(targetAddress, testCPU.programCounter)
    }

    @Test
    fun `test BCC branch on carry clear, no branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            carryFlag = true
            BCC().execute(targetAddress)
        }

        assertEquals(currentAddress, testCPU.programCounter)
    }

    /**
     * BCS
     */

    @Test
    fun `test BCS branch on carry set, branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            carryFlag = true
            BCS().execute(targetAddress)
        }

        assertEquals(targetAddress, testCPU.programCounter)
    }

    @Test
    fun `test BCS branch on carry set, no branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            carryFlag = false
            BCS().execute(targetAddress)
        }

        assertEquals(currentAddress, testCPU.programCounter)
    }

    /**
     * BEQ
     */

    @Test
    fun `test BEQ branch on result zero, branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            zeroFlag = true
            BEQ().execute(targetAddress)
        }

        assertEquals(targetAddress, testCPU.programCounter)
    }

    @Test
    fun `test BEQ branch on result zero , no branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            zeroFlag = false
            BEQ().execute(targetAddress)
        }

        assertEquals(currentAddress, testCPU.programCounter)
    }

    /**
     * BMI
     */

    @Test
    fun `test BMI branch on result minus, branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            negativeFlag = true
            BMI().execute(targetAddress)
        }

        assertEquals(targetAddress, testCPU.programCounter)
    }

    @Test
    fun `test BMI branch on result minus , no branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            negativeFlag = false
            BMI().execute(targetAddress)
        }

        assertEquals(currentAddress, testCPU.programCounter)
    }

    /**
     * BNE
     */

    @Test
    fun `test BNE branch on not zero, branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            zeroFlag = false
            BNE().execute(targetAddress)
        }

        assertEquals(targetAddress, testCPU.programCounter)
    }

    @Test
    fun `test BNE branch on not zero , no branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            zeroFlag = true
            BNE().execute(targetAddress)
        }

        assertEquals(currentAddress, testCPU.programCounter)
    }

    /**
     * BPL
     */

    @Test
    fun `test BPL branch on not negative, branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            negativeFlag = false
            BPL().execute(targetAddress)
        }

        assertEquals(targetAddress, testCPU.programCounter)
    }

    @Test
    fun `test BPL branch on not negative , no branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            negativeFlag = true
            BPL().execute(targetAddress)
        }

        assertEquals(currentAddress, testCPU.programCounter)
    }

    /**
     * BVC
     */

    @Test
    fun `test BVC branch on overflow clear, branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            overflowFlag = false
            BVC().execute(targetAddress)
        }

        assertEquals(targetAddress, testCPU.programCounter)
    }

    @Test
    fun `test BVC branch on overflow clear , no branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            overflowFlag = true
            BVC().execute(targetAddress)
        }

        assertEquals(currentAddress, testCPU.programCounter)
    }

    /**
     * BVS
     */

    @Test
    fun `test BVS branch on overflow set, branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            overflowFlag = true
            BVS().execute(targetAddress)
        }

        assertEquals(targetAddress, testCPU.programCounter)
    }

    @Test
    fun `test BVS branch on overflow set , no branching`() {
        val targetAddress: UShort = 0x0105u
        val currentAddress: UShort = 0x0000u

        testCPU.apply {
            programCounter = currentAddress
            overflowFlag = false
            BVS().execute(targetAddress)
        }

        assertEquals(currentAddress, testCPU.programCounter)
    }
}