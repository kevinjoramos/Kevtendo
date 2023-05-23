import CPU.CPU6502
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class BusTests {

    private val smallTestBus: Bus
    private val fullTestBus: Bus

    init {
        val testCPU1 = CPU6502()
        val testCPU2 = CPU6502()
        this.smallTestBus = Bus(testCPU1, UByteArray(64))
        this.fullTestBus = Bus(testCPU2, UByteArray((0x2000u).toInt()))
    }



    @Test
    fun `read address from memory happy path`() {
        val address: UShort = 0x000Fu
        assertEquals((0x0Fu).toUByte(), smallTestBus.readAddress(address))
    }

    @Test
    fun `read address from memory nasty path`() {
        val address: UShort = 0x000Fu
        assertNotEquals((0x1Fu).toUByte(), smallTestBus.readAddress(address))
    }

    @Test
    fun `write to address in memory happy path`() {
        val decimalAddressValue = 15

        val address: UShort = 0x000Fu
        val data: UByte = 0xFFu
        smallTestBus.writeToAddress(address, data)
        assertEquals(smallTestBus.ram[decimalAddressValue], data)
    }

    @Test
    fun `clear all memory in ram`() {

        smallTestBus.clearRam()
        smallTestBus.ram.forEach { assertEquals((0u).toUByte(), it) }
    }


    @Test
    fun `test address mirroring from 0x0000 to 0x1FFF`() {
        fullTestBus.writeToAddress((0x0000u).toUShort(), (0xFFu).toUByte())
        fullTestBus.writeToAddress((0x17FFu).toUShort(), (0xCCu).toUByte())

        fullTestBus.also {
            assertEquals((0xFFu).toUByte(), it.ram[(0x0000u).toInt()])
            assertEquals((0xFFu).toUByte(), it.ram[(0x0800u).toInt()])
            assertEquals((0xFFu).toUByte(), it.ram[(0x1000u).toInt()])
            assertEquals((0xFFu).toUByte(), it.ram[(0x1800u).toInt()])

            assertEquals((0xCCu).toUByte(), it.ram[(0x07FFu).toInt()])
            assertEquals((0xCCu).toUByte(), it.ram[(0x0FFFu).toInt()])
            assertEquals((0xCCu).toUByte(), it.ram[(0x17FFu).toInt()])
            assertEquals((0xCCu).toUByte(), it.ram[(0x1FFFu).toInt()])
        }
    }
}