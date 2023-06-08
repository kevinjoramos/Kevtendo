import bus.Bus
import cpu.CPU6502
import cartridge.Cartridge
import cartridge.MapperZero
import ppu.PPU2C02
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
        val testPPU = PPU2C02()
        val cartridge = Cartridge("src/main/kotlin/games/Donkey Kong.nes")

        this.smallTestBus = Bus(testCPU1, UByteArray(64), testPPU, MapperZero(cartridge))
        this.fullTestBus = Bus(testCPU2, UByteArray((0x2000u).toInt()), testPPU, MapperZero(cartridge))
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

    @Test
    fun `test address mirroring from 0x2000 to 0x3FFF`() {
        val testPPU = PPU2C02()
        fullTestBus.writeToAddress((0x2008u).toUShort(), (0x11u).toUByte())
        fullTestBus.writeToAddress((0x2009u).toUShort(), (0x22u).toUByte())
        fullTestBus.writeToAddress((0x200Au).toUShort(), (0x33u).toUByte())
        fullTestBus.writeToAddress((0x200Bu).toUShort(), (0x44u).toUByte())
        fullTestBus.writeToAddress((0x200Cu).toUShort(), (0x55u).toUByte())
        fullTestBus.writeToAddress((0x200Du).toUShort(), (0x66u).toUByte())
        fullTestBus.writeToAddress((0x200Eu).toUShort(), (0x77u).toUByte())
        fullTestBus.writeToAddress((0x200Fu).toUShort(), (0x88u).toUByte())

        fullTestBus.also {
            assertEquals((0x11u).toUByte(), it.ppu.controlRegister)
            assertEquals((0x22u).toUByte(), it.ppu.maskRegister)
            assertEquals((0x33u).toUByte(), it.ppu.statusRegister)
            assertEquals((0x44u).toUByte(), it.ppu.oamAddressRegister)
            assertEquals((0x55u).toUByte(), it.ppu.oamDataRegister)
            assertEquals((0x66u).toUByte(), it.ppu.scrollRegister)
            assertEquals((0x77u).toUByte(), it.ppu.addressRegister)
            assertEquals((0x88u).toUByte(), it.ppu.dataRegister)
        }
    }
}