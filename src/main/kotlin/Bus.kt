
/**
 * The connection to all other pieces of the system.
 * @param ram is an array of memory addresses.
 */
@ExperimentalUnsignedTypes
class Bus (var ram: UByteArray) {

    fun readAddress(address: UShort): UByte = ram[address.toInt()]

    fun writeToAddress(address: UShort, data: UByte) = ram.set(address.toInt(), data)

    fun clearRam() {

        val size = this.ram.size
        this.ram = UByteArray(size)
    }
}