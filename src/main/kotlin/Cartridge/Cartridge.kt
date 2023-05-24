package Cartridge

import Bus.Bus
import java.io.File

@ExperimentalUnsignedTypes
class Cartridge(
    cartridgeFilePath: String
) {
    val header: INESHeader
    val dataPrgRom: UByteArray
    val dataChrRom: UByteArray

    /**
     * Parses the .nes file for program rom and character rom.
     */
    init {
        val gameByteCode = File(cartridgeFilePath).readBytes().toUByteArray()
        val headerSize = 16
        val trainerSize = 512

        var leftPointer = 0
        var rightPointer = headerSize - 1
        header = INESHeader(gameByteCode.sliceArray(leftPointer..rightPointer))

        if (header.hasTrainer) {
            leftPointer = trainerSize + 1
            rightPointer += trainerSize - 1
        }

        leftPointer = rightPointer + 1
        rightPointer += header.sizeOfProgramRom - 1
        dataPrgRom = gameByteCode.sliceArray(leftPointer..rightPointer)

        leftPointer = rightPointer + 1
        rightPointer += header.sizeOfCharacterRom - 1
        dataChrRom = gameByteCode.sliceArray(leftPointer..rightPointer)
    }


}