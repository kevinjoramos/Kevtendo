import Bus.Bus
import CPU.CPU6502
import PPU.PPU2C02
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class ArithmeticInstructionTests {
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
     * ADC
     */

    @Test
    fun `test ADC add immediate operand to accumulator`() {
        val data: UByte = 0x23u
        val accumulatorValue: UByte = 0x32u
        val result: UByte = 0x55u

        testCPU.apply {
            accumulator = accumulatorValue
            carryFlag = false
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add immediate operand to accumulator with carry in`() {
        val data: UByte = 0x23u
        val accumulatorValue: UByte = 0x32u
        val result: UByte = 0x56u

        testCPU.apply {
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add immediate operand to accumulator with carry out and zero`() {
        val data: UByte = 0xFFu
        val accumulatorValue: UByte = 0x01u
        val result: UByte = 0x00u

        testCPU.apply {
            accumulator = accumulatorValue
            carryFlag = false
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add immediate operand to accumulator with overflow`() {
        val data: UByte = 0x7Fu
        val accumulatorValue: UByte = 0x01u
        val result: UByte = 0x80u

        testCPU.apply {
            accumulator = accumulatorValue
            carryFlag = false
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.overflowFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add immediate operand to accumulator with overflow and carry in`() {
        val data: UByte = 0x7Fu
        val accumulatorValue: UByte = 0x00u
        val result: UByte = 0x80u

        testCPU.apply {
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.overflowFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add immediate operand to accumulator with overflow negative to positive`() {
        val data: UByte = 0x80u
        val accumulatorValue: UByte = 0x80u
        val result: UByte = 0x00u

        testCPU.apply {
            accumulator = accumulatorValue
            carryFlag = false
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.carryFlag)
            assertEquals(true, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add immediate operand to accumulator with overflow no overflow with carry in`() {
        val data: UByte = 0x80u
        val accumulatorValue: UByte = 0x7Fu
        val result: UByte = 0x00u

        testCPU.apply {
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add immediate operand to accumulator with overflow positive to negative with carry in`() {
        val data: UByte = 0x7Fu
        val accumulatorValue: UByte = 0x7Fu
        val result: UByte = 0xFFu

        testCPU.apply {
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.overflowFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    ///

    @Test
    fun `test ADC add memory to accumulator`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x23u
        val accumulatorValue: UByte = 0x32u
        val result: UByte = 0x55u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            carryFlag = false
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add memory to accumulator with carry in`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x23u
        val accumulatorValue: UByte = 0x32u
        val result: UByte = 0x56u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add memory to accumulator with carry out and zero`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0xFFu
        val accumulatorValue: UByte = 0x01u
        val result: UByte = 0x00u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            carryFlag = false
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add memory to accumulator with overflow`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x7Fu
        val accumulatorValue: UByte = 0x01u
        val result: UByte = 0x80u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            carryFlag = false
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.overflowFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add memory to accumulator with overflow and carry in`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x7Fu
        val accumulatorValue: UByte = 0x00u
        val result: UByte = 0x80u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.overflowFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add memory to accumulator with overflow negative to positive`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x80u
        val accumulatorValue: UByte = 0x80u
        val result: UByte = 0x00u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            carryFlag = false
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.carryFlag)
            assertEquals(true, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add memory to accumulator with overflow no overflow with carry in`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x80u
        val accumulatorValue: UByte = 0x7Fu
        val result: UByte = 0x00u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(true, it.zeroFlag)
        }
    }

    @Test
    fun `test ADC add memory to accumulator with overflow positive to negative with carry in`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x7Fu
        val accumulatorValue: UByte = 0x7Fu
        val result: UByte = 0xFFu

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            ADC().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.overflowFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    /**
     * CMP
     */

    @Test
    fun `test CMP compare immediate value with accumulator, accumulator equals`() {
        val data: UByte = 0x7Fu
        val accumulatorValue: UByte = 0x7Fu

        testCPU.apply {
            accumulator = accumulatorValue
            zeroFlag = false
            negativeFlag = true
            carryFlag = false
            CMP().execute(data)
        }

        testCPU.also {
            assertEquals(true, it.zeroFlag)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.negativeFlag)
        }
    }

    @Test
    fun `test CMP compare immediate value with accumulator, accumulator less than`() {
        val data: UByte = 0x7Fu
        val accumulatorValue: UByte = 0x70u

        testCPU.apply {
            accumulator = accumulatorValue
            zeroFlag = true
            negativeFlag = false
            carryFlag = true
            CMP().execute(data)
        }

        testCPU.also {
            assertEquals(false, it.zeroFlag)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.negativeFlag)
        }
    }

    @Test
    fun `test CMP compare immediate value with accumulator, accumulator greater than`() {
        val data: UByte = 0x10u
        val accumulatorValue: UByte = 0x90u

        testCPU.apply {
            accumulator = accumulatorValue
            zeroFlag = true
            negativeFlag = false
            carryFlag = false
            CMP().execute(data)
        }

        testCPU.also {
            assertEquals(false, it.zeroFlag)
            assertEquals(true, it.carryFlag)
            assertEquals(true, it.negativeFlag)
        }
    }

    ///

    @Test
    fun `test CMP compare memory with accumulator, accumulator equals`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x7Fu
        val accumulatorValue: UByte = 0x7Fu

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            zeroFlag = false
            negativeFlag = true
            carryFlag = false
            CMP().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(true, it.zeroFlag)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.negativeFlag)
        }
    }

    @Test
    fun `test CMP compare memory with accumulator, accumulator less than`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x7Fu
        val accumulatorValue: UByte = 0x70u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            zeroFlag = true
            negativeFlag = false
            carryFlag = true
            CMP().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(false, it.zeroFlag)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.negativeFlag)
        }
    }

    @Test
    fun `test CMP compare memory with accumulator, accumulator greater than`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x10u
        val accumulatorValue: UByte = 0x90u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            zeroFlag = true
            negativeFlag = false
            carryFlag = false
            CMP().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(false, it.zeroFlag)
            assertEquals(true, it.carryFlag)
            assertEquals(true, it.negativeFlag)
        }
    }

    /**
     * CPX
     */

    @Test
    fun `test CPX compare immediate value with x register, x register equals`() {
        val data: UByte = 0x7Fu
        val xValue: UByte = 0x7Fu

        testCPU.apply {
            xRegister = xValue
            zeroFlag = false
            negativeFlag = true
            carryFlag = false
            CPX().execute(data)
        }

        testCPU.also {
            assertEquals(true, it.zeroFlag)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.negativeFlag)
        }
    }

    @Test
    fun `test CPX compare immediate value with x register, x register less than`() {
        val data: UByte = 0x7Fu
        val xValue: UByte = 0x70u

        testCPU.apply {
            xRegister = xValue
            zeroFlag = true
            negativeFlag = false
            carryFlag = true
            CPX().execute(data)
        }

        testCPU.also {
            assertEquals(false, it.zeroFlag)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.negativeFlag)
        }
    }

    @Test
    fun `test CPX compare immediate value with x register, x register greater than`() {
        val data: UByte = 0x10u
        val xValue: UByte = 0x90u

        testCPU.apply {
            xRegister = xValue
            zeroFlag = true
            negativeFlag = false
            carryFlag = false
            CPX().execute(data)
        }

        testCPU.also {
            assertEquals(false, it.zeroFlag)
            assertEquals(true, it.carryFlag)
            assertEquals(true, it.negativeFlag)
        }
    }

    ///

    @Test
    fun `test CPX compare memory with x register, x register equals`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x7Fu
        val xValue: UByte = 0x7Fu

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            xRegister = xValue
            zeroFlag = false
            negativeFlag = true
            carryFlag = false
            CPX().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(true, it.zeroFlag)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.negativeFlag)
        }
    }

    @Test
    fun `test CPX compare memory with x register, x register less than`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x7Fu
        val xValue: UByte = 0x70u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            xRegister = xValue
            zeroFlag = true
            negativeFlag = false
            carryFlag = true
            CPX().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(false, it.zeroFlag)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.negativeFlag)
        }
    }

    @Test
    fun `test CPX compare memory with x register, x register greater than`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x10u
        val xValue: UByte = 0x90u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            xRegister = xValue
            zeroFlag = true
            negativeFlag = false
            carryFlag = false
            CPX().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(false, it.zeroFlag)
            assertEquals(true, it.carryFlag)
            assertEquals(true, it.negativeFlag)
        }
    }

    /**
     * CPY
     */

    @Test
    fun `test CPY compare immediate value with y register, y register equals`() {
        val data: UByte = 0x7Fu
        val yValue: UByte = 0x7Fu

        testCPU.apply {
            yRegister = yValue
            zeroFlag = false
            negativeFlag = true
            carryFlag = false
            CPY().execute(data)
        }

        testCPU.also {
            assertEquals(true, it.zeroFlag)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.negativeFlag)
        }
    }

    @Test
    fun `test CPY compare immediate value with y register, y register less than`() {
        val data: UByte = 0x7Fu
        val yValue: UByte = 0x70u

        testCPU.apply {
            yRegister = yValue
            zeroFlag = true
            negativeFlag = false
            carryFlag = true
            CPY().execute(data)
        }

        testCPU.also {
            assertEquals(false, it.zeroFlag)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.negativeFlag)
        }
    }

    @Test
    fun `test CPY compare immediate value with y register, y register greater than`() {
        val data: UByte = 0x10u
        val yValue: UByte = 0x90u

        testCPU.apply {
            yRegister = yValue
            zeroFlag = true
            negativeFlag = false
            carryFlag = false
            CPY().execute(data)
        }

        testCPU.also {
            assertEquals(false, it.zeroFlag)
            assertEquals(true, it.carryFlag)
            assertEquals(true, it.negativeFlag)
        }
    }

    ///

    @Test
    fun `test CPY compare memory with y register, y register equals`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x7Fu
        val yValue: UByte = 0x7Fu

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            yRegister = yValue
            zeroFlag = false
            negativeFlag = true
            carryFlag = false
            CPY().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(true, it.zeroFlag)
            assertEquals(true, it.carryFlag)
            assertEquals(false, it.negativeFlag)
        }
    }

    @Test
    fun `test CPY compare memory with y register, y register less than`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x7Fu
        val yValue: UByte = 0x70u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            yRegister = yValue
            zeroFlag = true
            negativeFlag = false
            carryFlag = true
            CPY().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(false, it.zeroFlag)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.negativeFlag)
        }
    }

    @Test
    fun `test CPY compare memory with y register, y register greater than`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x10u
        val yValue: UByte = 0x90u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            yRegister = yValue
            zeroFlag = true
            negativeFlag = false
            carryFlag = false
            CPY().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(false, it.zeroFlag)
            assertEquals(true, it.carryFlag)
            assertEquals(true, it.negativeFlag)
        }
    }

    /**
     * SBC
     */

    @Test
        fun `test SBC subtract immediate value from accumulator unsigned borrow but no signed overflow`() {
        val data: UByte = 0xF0u
        val accumulatorValue: UByte = 0x50u
        val result: UByte = 0x60u

        testCPU.apply {
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            SBC().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test SBC subtract immediate value from accumulator unsigned borrow and unsigned overflow`() {
        val data: UByte = 0xB0u
        val accumulatorValue: UByte = 0x50u
        val result: UByte = 0xA0u

        testCPU.apply {
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = false
            negativeFlag = false
            zeroFlag = true
            SBC().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.overflowFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test SBC subtract immediate value from accumulator unsigned borrow but no signed overflow 2`() {
        val data: UByte = 0x70u
        val accumulatorValue: UByte = 0x50u
        val result: UByte = 0xE0u

        testCPU.apply {
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = false
            negativeFlag = false
            zeroFlag = true
            SBC().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test SBC subtract immediate value from accumulator with borrow`() {
        val data: UByte = 0xF0u
        val accumulatorValue: UByte = 0x50u
        val result: UByte = 0x5Fu

        testCPU.apply {
            accumulator = accumulatorValue
            carryFlag = false
            overflowFlag = false
            negativeFlag = false
            zeroFlag = true
            SBC().execute(data)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    //

    @Test
    fun `test SBC subtract memory from accumulator unsigned borrow but no signed overflow`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0xF0u
        val accumulatorValue: UByte = 0x50u
        val result: UByte = 0x60u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = true
            negativeFlag = true
            zeroFlag = true
            SBC().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test SBC subtract memory from accumulator unsigned borrow and unsigned overflow`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0xB0u
        val accumulatorValue: UByte = 0x50u
        val result: UByte = 0xA0u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = false
            negativeFlag = false
            zeroFlag = true
            SBC().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(true, it.overflowFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test SBC subtract memory from accumulator unsigned borrow but no signed overflow 2`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0x70u
        val accumulatorValue: UByte = 0x50u
        val result: UByte = 0xE0u

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            carryFlag = true
            overflowFlag = false
            negativeFlag = false
            zeroFlag = true
            SBC().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(true, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }
    }

    @Test
    fun `test SBC subtract memory from accumulator with borrow`() {
        val targetAddress: UShort = 0x0105u
        val data: UByte = 0xF0u
        val accumulatorValue: UByte = 0x50u
        val result: UByte = 0x5Fu

        testCPU.apply {
            bus.ram[targetAddress.toInt()] = data
            accumulator = accumulatorValue
            carryFlag = false
            overflowFlag = false
            negativeFlag = false
            zeroFlag = true
            SBC().execute(targetAddress)
        }

        testCPU.also {
            assertEquals(result, it.accumulator)
            assertEquals(false, it.carryFlag)
            assertEquals(false, it.overflowFlag)
            assertEquals(false, it.negativeFlag)
            assertEquals(false, it.zeroFlag)
        }

    }
}