import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class BusTests {

    private fun generateTestBus(size: Int): Bus {
        val testRam = UByteArray(size)
        for (index in testRam.indices) {
            testRam[index] = index.toUByte()
        }

        return Bus(testRam)
    }

    @Test
    fun `read address from memory happy path`() {
        val testBus = generateTestBus(64)
        val address: UShort = 0x000Fu
        assertEquals((0x0Fu).toUByte(), testBus.readAddress(address))
    }

    @Test
    fun `read address from memory nasty path`() {
        val testBus = generateTestBus(64)
        val address: UShort = 0x000Fu
        assertNotEquals((0x1Fu).toUByte(), testBus.readAddress(address))
    }

    @Test
    fun `write to address in memory happy path`() {
        val decimalAddressValue = 15
        val testBus = generateTestBus(16)
        val address: UShort = 0x000Fu
        val data: UByte = 0xFFu
        testBus.writeToAddress(address, data)
        assertEquals(testBus.ram[decimalAddressValue], data)
    }

    @Test
    fun `clear all memory in ram`() {
        val testBus = generateTestBus(64)
        testBus.clearRam()
        testBus.ram.forEach { assertEquals((0u).toUByte(), it) }
    }
}