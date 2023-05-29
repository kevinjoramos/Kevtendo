package mediator

interface Mediator {
    fun notify(event: Event)
    fun readAddress(address: UShort): UByte
    fun writeToAddress(address:UShort, data: UByte)
}