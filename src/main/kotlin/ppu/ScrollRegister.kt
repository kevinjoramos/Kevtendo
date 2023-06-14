package ppu


/**
 * Scroll Register
 * TODO -> What happens on third right to register after x and y are filled? Clear or Overwrite?
 */
class ScrollRegister {

    private var xValue: UInt = 0u
        set(value) { field = value.toUByte().toUInt() }

    private var yValue: UInt = 0u
        set(value) { field = value.toUByte().toUInt() }

    private var xValueIsFilled: Boolean = false

    fun getXScrollValue(): UInt = xValue
    fun getXYScrollValue(): UInt = yValue

    fun writeToScrollRegister(data: UInt) {
        if (xValueIsFilled) {
            yValue = data
            return
        }

        xValue = data
        xValueIsFilled = true
    }

    fun clear() {
        xValue = 0u
        yValue = 0u
        xValueIsFilled = false
    }

}
