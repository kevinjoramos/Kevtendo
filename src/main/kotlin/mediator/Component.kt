package mediator

interface Component {
    var bus: Mediator

    fun setMediator(mediator: Mediator) {
        bus = mediator
    }

    fun notify(sender: Sender, event: Event) {
        bus.notify(sender, event)
    }

    fun readAddress(address: UShort): UByte {
        return bus.readAddress(address)
    }

    fun writeToAddress(address:UShort, data: UByte) {
        bus.writeToAddress(address, data)
    }
}