package ppu

class AddressRegister {

    /**
     * Behaves like address latch
     */
    private var value = 0u

    fun readAddressFromLatch(): UInt = value.toUShort().toUInt()

    fun writeToAddressLatch(data: UInt) { value = (value shl 8) or data }

    fun incrementAddressLatch(increment: UInt) {

    }

    fun clearAddressLatch() {
        value = 0u
    }


}