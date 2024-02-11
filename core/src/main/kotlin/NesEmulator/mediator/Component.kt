package mediator

interface Component {
    val bus: Mediator
    fun notify(sender: Sender, event: Event) {
        bus.notify(sender, event)
    }
    fun readAddressFromBus(address: UShort): UByte = 0x00u
    fun writeToAddressInBus(address:UShort, data: UByte) = Unit
}
