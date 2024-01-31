package mediator

interface Mediator {
    fun notify(sender: Sender, event: Event)
    fun readAddress(address: UShort): UByte
    fun writeToAddress(address:UShort, data: UByte)
}