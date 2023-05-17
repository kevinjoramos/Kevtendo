import CPU.CPU6502
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class LogicInstructionTests {
    private lateinit var testCPU: CPU6502
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_535)
        testBus = Bus(testRam)
        testCPU = CPU6502(testBus)
    }

    /**
     * AND
     */

    @Test
    fun `test AND and immediate operand with accumulator`() {
        val accumulatorValue: UByte = 0x77u
        val data: UByte = 0xAAu
        val result: UByte = 0x22u

        testCPU.apply {
            accumulator = accumulatorValue
            negativeFlag = true
            zeroFlag = true
            AND().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test AND and immediate operand with accumulator with negative`() {
        val accumulatorValue: UByte = 0xFFu
        val data: UByte = 0x80u
        val result: UByte = 0x80u

        testCPU.apply {
            accumulator = accumulatorValue
            negativeFlag = false
            zeroFlag = true
            AND().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test AND  and immediate operand with accumulator with zero`() {
        val accumulatorValue: UByte = 0x55u
        val data: UByte = 0xAAu
        val result: UByte = 0x00u

        testCPU.apply {
            accumulator = accumulatorValue
            negativeFlag = true
            zeroFlag = false
            AND().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
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
            AND().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
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
            AND().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
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
            AND().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    /**
     * BIT
     */

    @Test
    fun `test BIT test bits in memory with accumulator`() {
        val targetAddress: UShort = 0x0105u
        val operand: UByte = 0x22u
        val accumulatorValue: UByte = 0x21u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = operand
            accumulator = accumulatorValue

            negativeFlag = true
            overflowFlag = true
            zeroFlag = true
            BIT().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(accumulatorValue, it.accumulator)
            assertEquals(operand, it.bus.ram[targetAddress.toInt()])
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test BIT test bits in memory with accumulator with negative, overflow, and zero`() {
        val targetAddress: UShort = 0x0105u
        val operand: UByte = 0xF2u
        val accumulatorValue: UByte = 0x00u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = operand
            accumulator = accumulatorValue

            negativeFlag = false
            overflowFlag = false
            zeroFlag = false

            BIT().execute(targetAddress)
        }



        testCPU.also {
            assertEquals(accumulatorValue, it.accumulator)
            assertEquals(operand, it.bus.ram[targetAddress.toInt()])
            assertEquals(true, it.negativeFlag)
            assertEquals(true, it.overflowFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    /**
     * EOR
     */

    @Test
    fun `test EOR xor immediate operand with accumulator`() {
        val operand: UByte = 0x72u
        val accumulatorValue: UByte = 0x23u
        val result: UByte = 0x51u

        testCPU.apply {
            accumulator = accumulatorValue
            negativeFlag = true
            zeroFlag = true
            EOR().execute(operand)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test EOR xor immediate operand with accumulator with negative`() {
        val operand: UByte = 0xF2u
        val accumulatorValue: UByte = 0x23u
        val result: UByte = 0xD1u

        testCPU.apply {
            accumulator = accumulatorValue
            negativeFlag = false
            zeroFlag = true
            EOR().execute(operand)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test EOR xor immediate operand with accumulator with zero`() {
        val operand: UByte = 0xF2u
        val accumulatorValue: UByte = 0xF2u
        val result: UByte = 0x00u

        testCPU.apply {
            accumulator = accumulatorValue
            negativeFlag = true
            zeroFlag = false
            EOR().execute(operand)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }
    @Test
    fun `test EOR exclusive memory with accumulator`() {
        val targetAddress: UShort = 0x0105u
        val operand: UByte = 0x72u
        val accumulatorValue: UByte = 0x23u
        val result: UByte = 0x51u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = operand
            accumulator = accumulatorValue
            negativeFlag = true
            zeroFlag = true
            EOR().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test EOR exclusive memory with accumulator with negative`() {
        val targetAddress: UShort = 0x0105u
        val operand: UByte = 0xF2u
        val accumulatorValue: UByte = 0x23u
        val result: UByte = 0xD1u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = operand
            accumulator = accumulatorValue
            negativeFlag = false
            zeroFlag = true
            EOR().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test EOR exclusive memory with accumulator with zero`() {
        val targetAddress: UShort = 0x0105u
        val operand: UByte = 0xF2u
        val accumulatorValue: UByte = 0xF2u
        val result: UByte = 0x00u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = operand
            accumulator = accumulatorValue
            negativeFlag = true
            zeroFlag = false
            EOR().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    /**
     * ORA
     */
    @Test
    fun `test ORA or immediate operand with accumulator`() {
        val operand: UByte = 0x72u
        val accumulatorValue: UByte = 0x23u
        val result: UByte = 0x73u

        testCPU.apply {
            accumulator = accumulatorValue
            negativeFlag = true
            zeroFlag = true
            ORA().execute(operand)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ORA or immediate operand with accumulator with negative`() {
        val operand: UByte = 0xF2u
        val accumulatorValue: UByte = 0x23u
        val result: UByte = 0xF3u

        testCPU.apply {
            accumulator = accumulatorValue
            negativeFlag = false
            zeroFlag = true
            ORA().execute(operand)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ORA or immediate operand with accumulator with zero`() {
        val operand: UByte = 0x00u
        val accumulatorValue: UByte = 0x00u
        val result: UByte = 0x00u

        testCPU.apply {
            accumulator = accumulatorValue
            negativeFlag = true
            zeroFlag = false
            ORA().execute(operand)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    @Test
    fun `test ORA or memory with accumulator`() {
        val targetAddress: UShort = 0x0105u
        val operand: UByte = 0x72u
        val accumulatorValue: UByte = 0x23u
        val result: UByte = 0x73u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = operand
            accumulator = accumulatorValue
            negativeFlag = true
            zeroFlag = true
            ORA().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ORA or memory with accumulator with negative`() {
        val targetAddress: UShort = 0x0105u
        val operand: UByte = 0xF2u
        val accumulatorValue: UByte = 0x23u
        val result: UByte = 0xF3u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = operand
            accumulator = accumulatorValue
            negativeFlag = false
            zeroFlag = true
            ORA().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ORA or memory with accumulator with zero`() {
        val targetAddress: UShort = 0x0105u
        val operand: UByte = 0x00u
        val accumulatorValue: UByte = 0x00u
        val result: UByte = 0x00u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = operand
            accumulator = accumulatorValue
            negativeFlag = true
            zeroFlag = false
            ORA().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }
}