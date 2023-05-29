package Cartridge

import java.io.File

@ExperimentalUnsignedTypes
class Cartridge(
    cartridgeFilePath: String
) {
    val header: INESHeader
    var programRom: UByteArray
    var characterRom: UByteArray

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
            leftPointer = rightPointer + 1
            rightPointer += trainerSize
        }

        leftPointer = rightPointer + 1
        rightPointer += header.sizeOfProgramRom
        programRom = gameByteCode.sliceArray(leftPointer..rightPointer)

        leftPointer = rightPointer + 1
        rightPointer += header.sizeOfCharacterRom
        characterRom = gameByteCode.sliceArray(leftPointer..rightPointer)
    }


}