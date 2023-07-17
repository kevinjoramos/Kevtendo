package bus

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import util.to2DigitHexString

class Ram {
    val memory = Array(0x2000) { (0x00u).toUByte() }

    fun writeToMemory(address: UShort, data: UByte) {
        memory[address.toInt()] = data
        /*when (address.toUInt()) {
            in FIRST_SIXTEEN_UBYTES -> {
                val row = memory.slice(FIRST_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in SECOND_SIXTEEN_UBYTES -> {
                val row = memory.slice(SECOND_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in THIRD_SIXTEEN_UBYTES -> {
                val row = memory.slice(THIRD_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"

            }
            in FOURTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(FOURTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in FIFTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(FIFTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in SIXTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(SIXTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in SEVENTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(SEVENTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in EIGHTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(EIGHTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in NINTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(NINTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in TENTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(TENTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in ELEVENTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(ELEVENTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in TWELFTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(TWELFTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in THIRTEENTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(THIRTEENTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in FOURTEENTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(FOURTEENTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in FIFTEENTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(FIFTEENTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
            in SIXTEENTH_SIXTEEN_UBYTES -> {
                val row = memory.slice(SIXTEENTH_SIXTEEN_BYTES)
                _zeroPageRow1StateFlow.value = "${row[0].to2DigitHexString()} ${row[1].to2DigitHexString()} ${row[2].to2DigitHexString()} ${row[3].to2DigitHexString()} ${row[4].to2DigitHexString()} ${row[5].to2DigitHexString()} ${row[6].to2DigitHexString()} ${row[7].to2DigitHexString()} ${row[8].to2DigitHexString()} ${row[9].to2DigitHexString()} ${row[10].to2DigitHexString()} ${row[11].to2DigitHexString()} ${row[12].to2DigitHexString()} ${row[13].to2DigitHexString()} ${row[14].to2DigitHexString()} ${row[15].to2DigitHexString()}"
            }
        }*/
    }

    private val _zeroPageRow1StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow1StateFlow = _zeroPageRow1StateFlow.asStateFlow()


    private val _zeroPageRow2StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow2StateFlow = _zeroPageRow2StateFlow.asStateFlow()

    private val _zeroPageRow3StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow3StateFlow = _zeroPageRow3StateFlow.asStateFlow()

    private val _zeroPageRow4StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow4StateFlow = _zeroPageRow4StateFlow.asStateFlow()

    private val _zeroPageRow5StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow5StateFlow = _zeroPageRow5StateFlow.asStateFlow()

    private val _zeroPageRow6StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow6StateFlow = _zeroPageRow6StateFlow.asStateFlow()

    private val _zeroPageRow7StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow7StateFlow = _zeroPageRow7StateFlow.asStateFlow()

    private val _zeroPageRow8StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow8StateFlow = _zeroPageRow8StateFlow.asStateFlow()

    private val _zeroPageRow9StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow9StateFlow = _zeroPageRow9StateFlow.asStateFlow()

    private val _zeroPageRow10StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow10StateFlow = _zeroPageRow10StateFlow.asStateFlow()

    private val _zeroPageRow11StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow11StateFlow = _zeroPageRow11StateFlow.asStateFlow()

    private val _zeroPageRow12StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow12StateFlow = _zeroPageRow12StateFlow.asStateFlow()

    private val _zeroPageRow13StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow13StateFlow = _zeroPageRow13StateFlow.asStateFlow()

    private val _zeroPageRow14StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow14StateFlow = _zeroPageRow14StateFlow.asStateFlow()

    private val _zeroPageRow15StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow15StateFlow = _zeroPageRow15StateFlow.asStateFlow()

    private val _zeroPageRow16StateFlow = MutableStateFlow("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
    val zeroPageRow16StateFlow = _zeroPageRow16StateFlow.asStateFlow()

    companion object {

        private val FIRST_SIXTEEN_UBYTES = 0u..15u
        private val SECOND_SIXTEEN_UBYTES = 16u..31u
        private val THIRD_SIXTEEN_UBYTES = 32u..47u
        private val FOURTH_SIXTEEN_UBYTES = 48u..63u
        private val FIFTH_SIXTEEN_UBYTES = 64u..79u
        private val SIXTH_SIXTEEN_UBYTES = 80u..95u
        private val SEVENTH_SIXTEEN_UBYTES = 96u..111u
        private val EIGHTH_SIXTEEN_UBYTES = 112u..127u
        private val NINTH_SIXTEEN_UBYTES = 128u..143u
        private val TENTH_SIXTEEN_UBYTES = 144u..159u
        private val ELEVENTH_SIXTEEN_UBYTES = 160u..175u
        private val TWELFTH_SIXTEEN_UBYTES = 176u..191u
        private val THIRTEENTH_SIXTEEN_UBYTES = 192u..207u
        private val FOURTEENTH_SIXTEEN_UBYTES = 208u..223u
        private val FIFTEENTH_SIXTEEN_UBYTES = 224u..239u
        private val SIXTEENTH_SIXTEEN_UBYTES = 240u..255u

        private val FIRST_SIXTEEN_BYTES = 0..15
        private val SECOND_SIXTEEN_BYTES = 16..31
        private val THIRD_SIXTEEN_BYTES = 32..47
        private val FOURTH_SIXTEEN_BYTES = 48..63
        private val FIFTH_SIXTEEN_BYTES = 64..79
        private val SIXTH_SIXTEEN_BYTES = 80..95
        private val SEVENTH_SIXTEEN_BYTES = 96..111
        private val EIGHTH_SIXTEEN_BYTES = 112..127
        private val NINTH_SIXTEEN_BYTES = 128..143
        private val TENTH_SIXTEEN_BYTES = 144..159
        private val ELEVENTH_SIXTEEN_BYTES = 160..175
        private val TWELFTH_SIXTEEN_BYTES = 176..191
        private val THIRTEENTH_SIXTEEN_BYTES = 192..207
        private val FOURTEENTH_SIXTEEN_BYTES = 208..223
        private val FIFTEENTH_SIXTEEN_BYTES = 224..239
        private val SIXTEENTH_SIXTEEN_BYTES = 240..255


    }
}