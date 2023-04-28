package CPU

abstract class Instruction {
    abstract val name: String
    abstract val addressingModeName: String
    abstract val cycleCount: Int
    abstract fun run()
}