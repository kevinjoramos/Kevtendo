package mediator

interface Component {
    var bus: Mediator

    fun setMediator(mediator: Mediator) {
        bus = mediator
    }

    fun notify(event: Event) {
        bus.notify(event)
    }

    fun readAddress(address: UShort): UByte {
        return bus.readAddress(address)
    }

    fun writeToAddress(address:UShort, data: UByte) {
        bus.writeToAddress(address, data)
    }
}