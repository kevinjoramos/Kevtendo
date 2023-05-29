package util

fun UByte.to2DigitHexString(): String {
    return String.format("%02x", this.toInt())
}

fun UByte.to4DigitHexString(): String {
    return String.format("%04x", this.toInt())
}

fun UShort.to2DigitHexString(): String {
    return String.format("%02x", this.toInt())
}

fun UShort.to4DigitHexString(): String {
    return String.format("%04x", this.toInt())
}