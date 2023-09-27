package controller

class GameController {

    var controllerState = 0x00u

    var buttonA: Boolean
        get() = when (controllerState and 0x80u) {
            0u -> false
            else -> true
        }
        set(value) {
            controllerState = when (value) {
                false -> {
                    controllerState and 0x80u.inv()
                }

                true -> {
                    controllerState or 0x80u
                }
            }
        }

    var buttonB: Boolean
        get() = when (controllerState and 0x40u) {
            0u -> false
            else -> true
        }
        set(value) {
            controllerState = when (value) {
                false -> {
                    controllerState and 0x40u.inv()
                }

                true -> {
                    controllerState or 0x40u
                }
            }
        }

    var buttonSelect: Boolean
        get() = when (controllerState and 0x20u) {
            0u -> false
            else -> true
        }
        set(value) {
            controllerState = when (value) {
                false -> {
                    controllerState and 0x20u.inv()
                }

                true -> {
                    controllerState or 0x20u
                }
            }
        }

    var buttonStart: Boolean
        get() = when (controllerState and 0x010u) {
            0u -> false
            else -> true
        }
        set(value) {
            controllerState = when (value) {
                false -> {
                    controllerState and 0x10u.inv()
                }

                true -> {
                    controllerState or 0x10u
                }
            }
        }

    var buttonUp: Boolean
        get() = when (controllerState and 0x08u) {
            0u -> false
            else -> true
        }
        set(value) {
            controllerState = when (value) {
                false -> {
                    controllerState and 0x08u.inv()
                }

                true -> {
                    controllerState or 0x08u
                }
            }
        }

    var buttonDown: Boolean
        get() = when (controllerState and 0x04u) {
            0u -> false
            else -> true
        }
        set(value) {
            controllerState = when (value) {
                false -> {
                    controllerState and 0x04u.inv()
                }

                true -> {
                    controllerState or 0x04u
                }
            }
        }

    var buttonLeft: Boolean
        get() = when (controllerState and 0x02u) {
            0u -> false
            else -> true
        }
        set(value) {
            controllerState = when (value) {
                false -> {
                    controllerState and 0x02u.inv()
                }

                true -> {
                    controllerState or 0x02u
                }
            }
        }

    var buttonRight: Boolean
        get() = when (controllerState and 0x01u) {
            0u -> false
            else -> true
        }
        set(value) {
            controllerState = when (value) {
                false -> {
                    controllerState and 0x01u.inv()
                }

                true -> {
                    controllerState or 0x01u
                }
            }
        }
}