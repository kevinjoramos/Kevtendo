import CPU.CPU6502
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

@ExperimentalUnsignedTypes
class ShiftInstructionTests {
    private lateinit var testCPU: CPU6502
    private lateinit var testBus: Bus

    @BeforeEach
    fun setup() {
        val testRam = UByteArray(65_535)
        testBus = Bus(testRam)
        testCPU = CPU6502(testBus)
    }


    /*@Test
    fun `test ASL arithmetic shift left on accumulator`() {
        val data: UByte = 0x01u
        val result: UByte = 0x02u

        testCPU.apply {
            accumulator = data
            carryFlag = true
            negativeFlag = true
            zeroFlag = true
            ASLA().execute(0x0000u)
        }

        assertEquals(result, testCPU.accumulator)
        assertEquals(false, testCPU.carryFlag)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }*/

    /*@Test
    fun `test ASL arithmetic shift left on accumulator with carry`() {
        val data: UByte = 0x80u
        val result: UByte = 0x00u

        testCPU.apply {
            accumulator = data
            carryFlag = false
            negativeFlag = true
            zeroFlag = false
            ASLA().execute(0x0000u)
        }

        assertEquals(result, testCPU.accumulator)
        assertEquals(true, testCPU.carryFlag)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }*/

    /*@Test
    fun `test ASL arithmetic shift left on accumulator with negative`() {
        val data: UByte = 0x40u
        val result: UByte = 0x80u

        testCPU.apply {
            accumulator = data
            carryFlag = true
            negativeFlag = false
            zeroFlag = true
            ASLA().execute(0x0000u)
        }

        assertEquals(result, testCPU.accumulator)
        assertEquals(false, testCPU.carryFlag)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }*/

    @Test
    fun `test ASL arithmetic shift left`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x01u
        val result: UByte = 0x02u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = true
            negativeFlag = true
            zeroFlag = true
            ASL().execute(targetAddress)
        }

        assertEquals(result, testCPU.bus.ram[targetAddress.toInt()])
        assertEquals(false, testCPU.carryFlag)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test ASL arithmetic shift left with carry`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x80u
        val result: UByte = 0x00u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = false
            negativeFlag = true
            zeroFlag = false
            ASL().execute(targetAddress)
        }

        assertEquals(result, testCPU.bus.ram[targetAddress.toInt()])
        assertEquals(true, testCPU.carryFlag)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test ASL arithmetic shift left with negative`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x40u
        val result: UByte = 0x80u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = true
            negativeFlag = false
            zeroFlag = true
            ASL().execute(targetAddress)
        }

        assertEquals(result, testCPU.bus.ram[targetAddress.toInt()])
        assertEquals(false, testCPU.carryFlag)
        assertEquals(true, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    /**
     * LSR
     */

    @Test
    fun `test LSR logical shift right on accumulator`() {
        val data: UByte = 0x04u
        val result: UByte = 0x02u

        testCPU.apply {
            accumulator = data
            carryFlag = true
            negativeFlag = true
            zeroFlag = true
            LSRA().execute(0x0000u)
        }

        assertEquals(result, testCPU.accumulator)
        assertEquals(false, testCPU.carryFlag)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test LSR logical shift right on accumulator with carry and zero`() {
        val data: UByte = 0x01u
        val result: UByte = 0x00u

        testCPU.apply {
            accumulator = data
            carryFlag = false
            negativeFlag = true
            zeroFlag = false
            LSRA().execute(0x0000u)
        }

        assertEquals(result, testCPU.accumulator)
        assertEquals(true, testCPU.carryFlag)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    @Test
    fun `test LSR logical shift right`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x04u
        val result: UByte = 0x02u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = true
            negativeFlag = true
            zeroFlag = true
            LSR().execute(targetAddress)
        }

        assertEquals(result, testCPU.bus.ram[targetAddress.toInt()])
        assertEquals(false, testCPU.carryFlag)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(false, testCPU.zeroFlag)
    }

    @Test
    fun `test LSR logical shift right with carry and zero`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x01u
        val result: UByte = 0x00u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = false
            negativeFlag = true
            zeroFlag = false
            LSR().execute(targetAddress)
        }

        assertEquals(result, testCPU.bus.ram[targetAddress.toInt()])
        assertEquals(true, testCPU.carryFlag)
        assertEquals(false, testCPU.negativeFlag)
        assertEquals(true, testCPU.zeroFlag)
    }

    /**
     * ROL
     */

    /*@Test
    fun `test ROL rotate left on accumulator`() {
        val data: UByte = 0x01u
        val result: UByte = 0x02u

        testCPU.apply {
            accumulator = data
            carryFlag = false
            negativeFlag = true
            zeroFlag = true
            ROLA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }*/

    /*@Test
    fun `test ROL rotate left on accumulator with carry in`() {
        val data: UByte = 0x01u
        val result: UByte = 0x03u

        testCPU.apply {
            accumulator = data
            carryFlag = true
            negativeFlag = true
            zeroFlag = true
            ROLA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }*/

    /*@Test
    fun `test ROL rotate left on accumulator with carry in and out`() {
        val data: UByte = 0x81u
        val result: UByte = 0x03u

        testCPU.apply {
            accumulator = data
            carryFlag = true
            negativeFlag = true
            zeroFlag = true
            ROLA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }*/

    /*@Test
    fun `test ROL rotate left on accumulator with negative`() {
        val data: UByte = 0x40u
        val result: UByte = 0x80u

        testCPU.apply {
            accumulator = data
            carryFlag = false
            negativeFlag = true
            zeroFlag = true
            ROLA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }*/

   /* @Test
    fun `test ROL rotate left on accumulator with zero`() {
        val data: UByte = 0x80u
        val result: UByte = 0x00u

        testCPU.apply {
            accumulator = data
            carryFlag = false
            negativeFlag = true
            zeroFlag = true
            ROLA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }*/

    @Test
    fun `test ROL rotate left`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x01u
        val result: UByte = 0x02u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = false
            negativeFlag = true
            zeroFlag = true
            ROL().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.bus.ram[targetAddress.toInt()])
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ROL rotate left with carry in`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x01u
        val result: UByte = 0x03u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = true
            negativeFlag = true
            zeroFlag = true
            ROL().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.bus.ram[targetAddress.toInt()])
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ROL rotate left with carry in and out`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x81u
        val result: UByte = 0x03u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = true
            negativeFlag = true
            zeroFlag = true
            ROL().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.bus.ram[targetAddress.toInt()])
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ROL rotate left with negative`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x40u
        val result: UByte = 0x80u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = false
            negativeFlag = true
            zeroFlag = true
            ROL().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.bus.ram[targetAddress.toInt()])
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ROL rotate left with zero`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x80u
        val result: UByte = 0x00u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = false
            negativeFlag = true
            zeroFlag = true
            ROL().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.bus.ram[targetAddress.toInt()])
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    /**
     * ROR
     */

    @Test
    fun `test ROR rotate right on Accumulator`() {
        val data: UByte = 0x04u
        val result: UByte = 0x02u

        testCPU.apply {
            accumulator = data
            carryFlag = false
            negativeFlag = false
            zeroFlag = false
            RORA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ROR rotate right on Accumulator with carry in`() {
        val data: UByte = 0x00u
        val result: UByte = 0x80u

        testCPU.apply {
            accumulator = data
            carryFlag = true
            negativeFlag = false
            zeroFlag = false
            RORA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ROR rotate right on Accumulator with carry in and out and negative`() {
        val data: UByte = 0x01u
        val result: UByte = 0x80u

        testCPU.apply {
            accumulator = data
            carryFlag = true
            negativeFlag = false
            zeroFlag = false
            RORA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.carryFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ROR rotate right on Accumulator with carry out and zero`() {
        val data: UByte = 0x01u
        val result: UByte = 0x00u

        testCPU.apply {
            accumulator = data
            carryFlag = false
            negativeFlag = false
            zeroFlag = false
            RORA().execute(0x0000u)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    @Test
    fun `test ROR rotate right`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x04u
        val result: UByte = 0x02u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = false
            negativeFlag = false
            zeroFlag = false
            ROR().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.bus.ram[targetAddress.toInt()])
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ROR rotate right with carry in with negative`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x00u
        val result: UByte = 0x80u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = true
            negativeFlag = false
            zeroFlag = false
            ROR().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.bus.ram[targetAddress.toInt()])
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ROR rotate right with carry in and out and negative`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x01u
        val result: UByte = 0x80u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = true
            negativeFlag = false
            zeroFlag = false
            ROR().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.bus.ram[targetAddress.toInt()])
            assertEquals(true, it.carryFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ROR rotate right with carry out and zero`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x01u
        val result: UByte = 0x00u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            carryFlag = false
            negativeFlag = false
            zeroFlag = false
            ROR().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.bus.ram[targetAddress.toInt()])
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }
}