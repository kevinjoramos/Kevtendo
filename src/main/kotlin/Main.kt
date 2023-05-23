import CPU.CPU6502

@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    val cpu = CPU6502()
    Bus(cpu, UByteArray(2000))
}

