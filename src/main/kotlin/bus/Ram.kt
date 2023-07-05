package bus

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class Ram {
    val memory = Array(0x2000) { (0x00u).toUByte() }

    fun writeToMemory(address: UShort, data: UByte) {
        memory[address.toInt()] = data
        when (address.toUInt()) {
            in FIRST_SIXTEEN_UBYTES -> { _zeroPageRow1StateFlow.value[(address % 15u).toInt()] = data }
            in SECOND_SIXTEEN_UBYTES -> { _zeroPageRow2StateFlow.value[(address % 15u).toInt()] = data }
            in THIRD_SIXTEEN_UBYTES -> { _zeroPageRow3StateFlow.value[(address % 15u).toInt()] = data }
            in FOURTH_SIXTEEN_UBYTES -> { _zeroPageRow4StateFlow.value[(address % 15u).toInt()] = data }
            in FIFTH_SIXTEEN_UBYTES -> { _zeroPageRow5StateFlow.value[(address % 15u).toInt()] = data }
            in SIXTH_SIXTEEN_UBYTES -> { _zeroPageRow6StateFlow.value[(address % 15u).toInt()] = data }
            in SEVENTH_SIXTEEN_UBYTES -> { _zeroPageRow7StateFlow.value[(address % 15u).toInt()] = data }
            in EIGHTH_SIXTEEN_UBYTES -> { _zeroPageRow8StateFlow.value[(address % 15u).toInt()] = data }
            in NINTH_SIXTEEN_UBYTES -> { _zeroPageRow9StateFlow.value[(address % 15u).toInt()] = data }
            in TENTH_SIXTEEN_UBYTES -> { _zeroPageRow10StateFlow.value[(address % 15u).toInt()] = data }
            in ELEVENTH_SIXTEEN_UBYTES -> { _zeroPageRow11StateFlow.value[(address % 15u).toInt()] = data }
            in TWELFTH_SIXTEEN_UBYTES -> { _zeroPageRow12StateFlow.value[(address % 15u).toInt()] = data }
            in THIRTEENTH_SIXTEEN_UBYTES -> { _zeroPageRow13StateFlow.value[(address % 15u).toInt()] = data }
            in FOURTEENTH_SIXTEEN_UBYTES -> { _zeroPageRow14StateFlow.value[(address % 15u).toInt()] = data }
            in FIFTEENTH_SIXTEEN_UBYTES -> { _zeroPageRow15StateFlow.value[(address % 15u).toInt()] = data }
            in SIXTEENTH_SIXTEEN_UBYTES -> { _zeroPageRow16StateFlow.value[(address % 15u).toInt()] = data }
        }
    }

    private val _zeroPageRow1StateFlow = MutableStateFlow(memory.slice(FIRST_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow1StateFlow = _zeroPageRow1StateFlow.asStateFlow()


    private val _zeroPageRow2StateFlow = MutableStateFlow(memory.slice(SECOND_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow2StateFlow = _zeroPageRow2StateFlow.asStateFlow()

    private val _zeroPageRow3StateFlow = MutableStateFlow(memory.slice(THIRD_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow3StateFlow = _zeroPageRow3StateFlow.asStateFlow()

    private val _zeroPageRow4StateFlow = MutableStateFlow(memory.slice(FOURTH_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow4StateFlow = _zeroPageRow4StateFlow.asStateFlow()

    private val _zeroPageRow5StateFlow = MutableStateFlow(memory.slice(FIFTH_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow5StateFlow = _zeroPageRow5StateFlow.asStateFlow()

    private val _zeroPageRow6StateFlow = MutableStateFlow(memory.slice(SIXTH_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow6StateFlow = _zeroPageRow6StateFlow.asStateFlow()

    private val _zeroPageRow7StateFlow = MutableStateFlow(memory.slice(SEVENTH_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow7StateFlow = _zeroPageRow7StateFlow.asStateFlow()

    private val _zeroPageRow8StateFlow = MutableStateFlow(memory.slice(EIGHTH_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow8StateFlow = _zeroPageRow8StateFlow.asStateFlow()

    private val _zeroPageRow9StateFlow = MutableStateFlow(memory.slice(NINTH_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow9StateFlow = _zeroPageRow9StateFlow.asStateFlow()

    private val _zeroPageRow10StateFlow = MutableStateFlow(memory.slice(TENTH_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow10StateFlow = _zeroPageRow10StateFlow.asStateFlow()

    private val _zeroPageRow11StateFlow = MutableStateFlow(memory.slice(ELEVENTH_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow11StateFlow = _zeroPageRow11StateFlow.asStateFlow()

    private val _zeroPageRow12StateFlow = MutableStateFlow(memory.slice(TWELFTH_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow12StateFlow = _zeroPageRow12StateFlow.asStateFlow()

    private val _zeroPageRow13StateFlow = MutableStateFlow(memory.slice(THIRTEENTH_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow13StateFlow = _zeroPageRow13StateFlow.asStateFlow()

    private val _zeroPageRow14StateFlow = MutableStateFlow(memory.slice(FOURTEENTH_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow14StateFlow = _zeroPageRow14StateFlow.asStateFlow()

    private val _zeroPageRow15StateFlow = MutableStateFlow(memory.slice(FIFTEENTH_SIXTEEN_BYTES).toMutableList())
    val zeroPageRow15StateFlow = _zeroPageRow15StateFlow.asStateFlow()

    private val _zeroPageRow16StateFlow = MutableStateFlow(memory.slice(SIXTEENTH_SIXTEEN_BYTES).toMutableList())
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