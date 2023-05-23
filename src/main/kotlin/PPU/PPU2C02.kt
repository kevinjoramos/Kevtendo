package PPU

import Bus.Bus

@ExperimentalUnsignedTypes
class PPU2C02 {
    lateinit var bus: Bus

    var ppuCtrlRegister: UByte = 0x00u
    var ppuMaskRegister: UByte = 0x00u
    var ppuStatusRegister: UByte = 0x00u
    var oamAddrRegister: UByte = 0x00u
    var oamDataRegister: UByte = 0x00u
    var ppuScrollRegister: UByte = 0x00u
    var ppuAddrRegister: UByte = 0x00u
    var ppuDataRegister: UByte = 0x00u
    var oamDMARegister: UByte = 0x00u

    val leftPatternTable: UByteArray = UByteArray((0x1000u).toInt())
    val rightPatternTable: UByteArray = UByteArray((0x1000u).toInt())
    val nameTable: UByteArray = UByteArray((0x2000u).toInt())
    val paletteTable: UByteArray = UByteArray((0x0100u).toInt())
    val objectAtrributeMemory: UByteArray = UByteArray(256)

}