package mediator

interface Mediator {
    fun notify(sender: Sender, event: Event)
}
