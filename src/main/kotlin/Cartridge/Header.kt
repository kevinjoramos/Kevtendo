package Cartridge

@ExperimentalUnsignedTypes
class INESHeader(val headerByteCode: UByteArray)  {
    val headerConstant: String = headerByteCode.sliceArray(0..3).toString()

    val sizeOfProgramRom: Int = (headerByteCode[4] * 16384u).toInt()
    val sizeOfCharacterRom: Int = (headerByteCode[5] * 8192u).toInt()
    private val flag6 = headerByteCode[6]
    val hasVerticalMirroring: Boolean
        get() = this.flag6 and (0x01u).toUByte() == (0x01u).toUByte()
    val hasOtherPersistentMemory: Boolean
        get() = this.flag6 and (0x02u).toUByte() == (0x02u).toUByte()

    val hasTrainer: Boolean
        get() = this.flag6 and (0x04).toUByte() == (0x04u).toUByte()

    val ignoreMirroringControl: Boolean
        get() = this.flag6 and (0x08).toUByte() == (0x08u).toUByte()

    private val flag7 = headerByteCode[7]

    val mapperId: UByte
        get() = (this.flag7 and 0xF0u) or (this.flag6.toUInt() shr 8).toUByte()


    private val flag8 = headerByteCode[8]
    private val flag9 = headerByteCode[9]
    private val flag10 = headerByteCode[10]
}