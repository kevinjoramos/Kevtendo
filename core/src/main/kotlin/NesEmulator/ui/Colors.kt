package ui

object NesColors {
    //val DarkGreen100 = Color(0xff1c4645)
    //val LightGreen100 = Color(0xff1f5150)


    private const val COLOR_00 = 0x626262FFu
    private const val COLOR_01 = 0x0D226BFFu
    private const val COLOR_02 = 0x241476FFu
    private const val COLOR_03 = 0x3B0A6BFFu
    private const val COLOR_04 = 0x4C074DFFu
    private const val COLOR_05 = 0x520C24FFu
    private const val COLOR_06 = 0x4C1700FFu
    private const val COLOR_07 = 0x3B2600FFu
    private const val COLOR_08 = 0x243400FFu
    private const val COLOR_09 = 0x0D3D00FFu
    private const val COLOR_0A = 0x227C15FFu
    private const val COLOR_0B = 0x003B24FFu
    private const val COLOR_0C = 0x00304DFFu
    private const val COLOR_0D = 0x000000FFu
    private const val COLOR_0E = 0x000000FFu
    private const val COLOR_0F = 0x000000FFu
    private const val COLOR_10 = 0xABABABFFu
    private const val COLOR_11 = 0x3156B1FFu
    private const val COLOR_12 = 0x5043C5FFu
    private const val COLOR_13 = 0x7034BBFFu
    private const val COLOR_14 = 0x892F95FFu
    private const val COLOR_15 = 0x94345FFFu
    private const val COLOR_16 = 0x8E4226FFu
    private const val COLOR_17 = 0x795500FFu
    private const val COLOR_18 = 0x5B6800FFu
    private const val COLOR_19 = 0x3B7700FFu
    private const val COLOR_1A = 0x227C15FFu
    private const val COLOR_1B = 0x17774CFFu
    private const val COLOR_1C = 0x1D6985FFu
    private const val COLOR_1D = 0x000000FFu
    private const val COLOR_1E = 0x000000FFu
    private const val COLOR_1F = 0x000000FFu
    private const val COLOR_20 = 0xFFFFFFFFu
    private const val COLOR_21 = 0x7CAAFFFFu
    private const val COLOR_22 = 0x9B96FFFFu
    private const val COLOR_23 = 0xBD86FFFFu
    private const val COLOR_24 = 0xD87EF1FFu
    private const val COLOR_25 = 0xE682BAFFu
    private const val COLOR_26 = 0xE38F7FFFu
    private const val COLOR_27 = 0xD0A24EFFu
    private const val COLOR_28 = 0xB2B734FFu
    private const val COLOR_29 = 0x90C739FFu
    private const val COLOR_2A = 0x74CE5CFFu
    private const val COLOR_2B = 0x66CB92FFu
    private const val COLOR_2C = 0x69BECEFFu
    private const val COLOR_2D = 0x4E4E4EFFu
    private const val COLOR_2E = 0x000000FFu
    private const val COLOR_2F = 0x000000FFu
    private const val COLOR_30 = 0xFFFFFFFFu
    private const val COLOR_31 = 0xC9DEFCFFu
    private const val COLOR_32 = 0xD5D6FFFFu
    private const val COLOR_33 = 0xE2CFFFFFu
    private const val COLOR_34 = 0xEECCFCFFu
    private const val COLOR_35 = 0xF5CCE7FFu
    private const val COLOR_36 = 0xF5D1CFFFu
    private const val COLOR_37 = 0xEED8BBFFu
    private const val COLOR_38 = 0xE2E1AEFFu
    private const val COLOR_39 = 0xD5E8AEFFu
    private const val COLOR_3A = 0xC9EBBBFFu
    private const val COLOR_3B = 0xC2EBCFFFu
    private const val COLOR_3C = 0xC2E6E7FFu
    private const val COLOR_3D = 0xB8B8B8FFu
    private const val COLOR_3E = 0x000000FFu
    private const val COLOR_3F = 0x000000FFu

    fun decodeNesColor(encodedColorHex: Int): Int =
        when (encodedColorHex) {
            0x00 -> COLOR_00
            0x01 -> COLOR_01
            0x02 -> COLOR_02
            0x03 -> COLOR_03
            0x04 -> COLOR_04
            0x05 -> COLOR_05
            0x06 -> COLOR_06
            0x07 -> COLOR_07
            0x08 -> COLOR_08
            0x09 -> COLOR_09
            0x0A -> COLOR_0A
            0x0B -> COLOR_0B
            0x0C -> COLOR_0C
            0x0D -> COLOR_0D
            0x0E -> COLOR_0E
            0x0F -> COLOR_0F
            0x10 -> COLOR_10
            0x11 -> COLOR_11
            0x12 -> COLOR_12
            0x13 -> COLOR_13
            0x14 -> COLOR_14
            0x15 -> COLOR_15
            0x16 -> COLOR_16
            0x17 -> COLOR_17
            0x18 -> COLOR_18
            0x19 -> COLOR_19
            0x1A -> COLOR_1A
            0x1B -> COLOR_1B
            0x1C -> COLOR_1C
            0x1D -> COLOR_1D
            0x1E -> COLOR_1E
            0x1F -> COLOR_1F
            0x20 -> COLOR_20
            0x21 -> COLOR_21
            0x22 -> COLOR_22
            0x23 -> COLOR_23
            0x24 -> COLOR_24
            0x25 -> COLOR_25
            0x26 -> COLOR_26
            0x27 -> COLOR_27
            0x28 -> COLOR_28
            0x29 -> COLOR_29
            0x2A -> COLOR_2A
            0x2B -> COLOR_2B
            0x2C -> COLOR_2C
            0x2D -> COLOR_2D
            0x2E -> COLOR_2E
            0x2F -> COLOR_1F
            0x30 -> COLOR_30
            0x31 -> COLOR_31
            0x32 -> COLOR_32
            0x33 -> COLOR_33
            0x34 -> COLOR_34
            0x35 -> COLOR_35
            0x36 -> COLOR_36
            0x37 -> COLOR_37
            0x38 -> COLOR_38
            0x39 -> COLOR_39
            0x3A -> COLOR_3A
            0x3B -> COLOR_3B
            0x3C -> COLOR_3C
            0x3D -> COLOR_3D
            0x3E -> COLOR_3E
            else -> COLOR_3F
        }.toInt()
}
