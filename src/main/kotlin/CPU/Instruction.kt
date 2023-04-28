package CPU

abstract class Instruction {
    abstract fun run(targetAddress: UShort): UShort
}