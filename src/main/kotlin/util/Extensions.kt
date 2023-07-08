package util

fun UByte.to2DigitHexString(): String {
    return Integer.toHexString(this.toInt())
}

fun UByte.to4DigitHexString(): String {
    return Integer.toHexString(this.toInt())
}

fun UShort.to2DigitHexString(): String {
    //return String.format("%02x", this.toInt())
    return Integer.toHexString(this.toInt())
}

fun UShort.to4DigitHexString(): String {
    //return String.format("%04x", this.toInt())
    return Integer.toHexString(this.toInt())
}

fun UInt.to2DigitHexString(): String {
    if (this < 0x10u) return "0${Integer.toHexString(this.toInt())}"
    return Integer.toHexString(this.toInt())
}

fun UInt.to4DigitHexString(): String {
    if (this < 0x10u) return "000${Integer.toHexString(this.toInt())}"
    if (this < 0x100u) return "00${Integer.toHexString(this.toInt())}"
    if (this < 0x1000u) return "0${Integer.toHexString(this.toInt())}"
    return Integer.toHexString(this.toInt())
}