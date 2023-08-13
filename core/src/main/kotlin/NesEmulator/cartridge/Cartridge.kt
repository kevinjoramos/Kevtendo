package cartridge

import java.io.File

@ExperimentalUnsignedTypes
class Cartridge(
    cartridgeFilePath: String
) {
    private val header: NesFileHeader
    var programRom: UByteArray
    var characterRom: UByteArray

    companion object {
        const val HEADER_SIZE = 16
        const val TRAINER_SIZE = 512
    }

    /**
     * Parses the .nes file for program rom and character rom.
     */
    init {

        // Get the file contents as a byte array.
        val gameByteCode = File(cartridgeFilePath).readBytes().toUByteArray()

        // Extract the INES header for cartridge metadata.
        var leftPointer = 0
        var rightPointer = HEADER_SIZE - 1
        header = INesHeader(gameByteCode.sliceArray(leftPointer..rightPointer))

        // If cartridge has a trainer section, throw away.
        if (header.hasTrainer) {
            leftPointer = rightPointer + 1
            rightPointer += TRAINER_SIZE
        }

        // Extract the program rom section
        leftPointer = rightPointer + 1
        rightPointer += header.sizeOfProgramRom
        programRom = gameByteCode.sliceArray(leftPointer..rightPointer)

        // Extract the character rom section.
        leftPointer = rightPointer + 1
        rightPointer += header.sizeOfCharacterRom
        characterRom = gameByteCode.sliceArray(leftPointer..rightPointer)
    }


}
