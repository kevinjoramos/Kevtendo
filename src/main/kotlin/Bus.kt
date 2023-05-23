import CPU.CPU6502

/**
 * The connection to all other pieces of the system.
 * @param ram is an array of memory addresses.
 */
@ExperimentalUnsignedTypes
class Bus (
    val cpu: CPU6502,
    var ram: UByteArray
) {

    init {
        cpu.bus = this
    }

    fun readAddress(address: UShort): UByte {
        if (address < 0x2000u) return ram[address.toInt()]

        return 0x01u
    }

    fun writeToAddress(address: UShort, data: UByte) {
        if (address < 0x2000u) {
            ram[address.toInt()] = data
            ram[(address + 0x0800u).mod(0x2000u).toInt()] = data
            ram[(address + 0x1000u).mod(0x2000u).toInt()] = data
            ram[(address + 0x1800u).mod(0x2000u).toInt()] = data
        }
    }


    fun clearRam() {
        val size = this.ram.size
        this.ram = UByteArray(size)
    }
}