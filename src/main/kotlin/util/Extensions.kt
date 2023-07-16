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

fun UByte.reverse(): UByte {
    val byte = this.toUInt()
    return (((byte and 0x80u) shr 7) or ((byte and 0x40u) shr 5) or ((byte and 0x20u) shr 3) or ((byte and 0x10u) shr 1) or
            ((byte and 0x08u) shl 1) or ((byte and 0x04u) shl 3) or ((byte and 0x02u) shl 5) or ((byte and 0x01u) shl 7)).toUByte()
}