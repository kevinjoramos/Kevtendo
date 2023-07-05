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