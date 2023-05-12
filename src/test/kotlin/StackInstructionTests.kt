import CPU.CPU6502
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class StackInstructionTests {
    private lateinit var testCPU: CPU6502
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_535)
        testBus = Bus(testRam)
        testCPU = CPU6502(testBus)
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
            PHA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(0xFEu, it.stackPointer)
            assertEquals(data, it.bus.ram[(0xFFu).toInt()])
        }
    }

    /**
     * PHP
     */

    @Test
    fun `test PHP push processor status on stack FF`() {
        val result: UByte = 0xFFu
        val startingLocation: UByte = 0xFFu

        testCPU.apply {
            stackPointer = startingLocation

            negativeFlag = true
            overflowFlag = true
            extraFlag = true
            breakFlag = true
            decimalFlag = true
            interruptDisableFlag = true
            zeroFlag = true
            carryFlag = true

            PHP().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(result, it.bus.ram[startingLocation.toInt()])
            assertEquals(0xFEu, it.stackPointer)
        }
    }

    @Test
    fun `test PHP push processor status on stack AA`() {
        val result: UByte = 0xAAu
        val startingLocation: UByte = 0xFFu

        testCPU.apply {
            stackPointer = startingLocation

            negativeFlag = true
            overflowFlag = false
            extraFlag = true
            breakFlag = false
            decimalFlag = true
            interruptDisableFlag = false
            zeroFlag = true
            carryFlag = false

            PHP().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(result, it.bus.ram[startingLocation.toInt()])
            assertEquals(0xFEu, it.stackPointer)
        }
    }

    /**
     * PLA
     */

    @Test
    fun `test PLA pull accumulator from stack`() {
        val data: UByte = 0x44u
        val startingLocation: UByte = 0xFEu

        testCPU.apply {
            bus.ram[(startingLocation + 1u).toInt()] = data
            stackPointer = startingLocation
            negativeFlag = true
            zeroFlag = true
            PLA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(data, it.accumulator)
            assertEquals(0xFFu, it.stackPointer)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test PLA pull accumulator from stack with negative`() {
        val data: UByte = 0x80u
        val startingLocation: UByte = 0xFEu

        testCPU.apply {
            bus.ram[(startingLocation + 1u).toInt()] = data
            stackPointer = startingLocation
            negativeFlag = false
            zeroFlag = true
            PLA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(data, it.accumulator)
            assertEquals(0xFFu, it.stackPointer)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test PLA pull accumulator from stack with zero`() {
        val data: UByte = 0x00u
        val startingLocation: UByte = 0xFEu

        testCPU.apply {
            bus.ram[(startingLocation + 1u).toInt()] = data
            stackPointer = startingLocation
            negativeFlag = true
            zeroFlag = false
            PLA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(data, it.accumulator)
            assertEquals(0xFFu, it.stackPointer)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    /**
     * PLP
     */

    @Test
    fun `test PLP pull processor status from stack 0x00`() {
        val data: UByte = 0x00u
        val startingLocation: UByte = 0xFEu

        testCPU.apply {
            bus.ram[(startingLocation + 1u).toInt()] = data
            stackPointer = startingLocation

            negativeFlag = true
            overflowFlag = true
            extraFlag = true
            breakFlag = true
            decimalFlag = true
            interruptDisableFlag = true
            zeroFlag = true
            carryFlag = true
            PLP().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(0xFFu, it.stackPointer)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.extraFlag)
            assertEquals(false, it.breakFlag)
            assertEquals(false, it.decimalFlag)
            assertEquals(false, it.interruptDisableFlag)
            assertEquals(false, it.zeroFlag)
            assertEquals(false, it.carryFlag)
        }
    }

    @Test
    fun `test PLP pull processor status from stack 0xAA`() {
        val data: UByte = 0xAAu
        val startingLocation: UByte = 0xFEu

        testCPU.apply {
            bus.ram[(startingLocation + 1u).toInt()] = data
            stackPointer = startingLocation

            negativeFlag = true
            overflowFlag = true
            extraFlag = true
            breakFlag = true
            decimalFlag = true
            interruptDisableFlag = true
            zeroFlag = true
            carryFlag = true
            PLP().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(0xFFu, it.stackPointer)
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

    @Test
    fun `test PLP pull processor status from stack 0xC3`() {
        val data: UByte = 0xC3u
        val startingLocation: UByte = 0xFEu

        testCPU.apply {
            bus.ram[(startingLocation + 1u).toInt()] = data
            stackPointer = startingLocation

            negativeFlag = true
            overflowFlag = true
            extraFlag = true
            breakFlag = true
            decimalFlag = true
            interruptDisableFlag = true
            zeroFlag = true
            carryFlag = true
            PLP().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(0xFFu, it.stackPointer)
            assertEquals(true, it.negativeFlag)
            assertEquals(true, it.overflowFlag)
            assertEquals(false, it.extraFlag)
            assertEquals(false, it.breakFlag)
            assertEquals(false, it.decimalFlag)
            assertEquals(false, it.interruptDisableFlag)
            assertEquals(true, it.zeroFlag)
            assertEquals(true, it.carryFlag)
        }
    }
}